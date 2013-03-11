package edu.mit.mobile.android.locast.itineraries;

/*
 * Copyright (C) 2011  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.imagecache.SimpleThumbnailCursorAdapter;
import edu.mit.mobile.android.locast.Constants;
import edu.mit.mobile.android.locast.data.Itinerary;
import edu.mit.mobile.android.locast.data.MediaProvider;
import edu.mit.mobile.android.locast.memorytraces.R;
import edu.mit.mobile.android.locast.sync.LocastSync;
import edu.mit.mobile.android.locast.sync.LocastSyncObserver;
import edu.mit.mobile.android.locast.sync.LocastSyncStatusObserver;
import edu.mit.mobile.android.widget.NotificationProgressBar;
import edu.mit.mobile.android.widget.RefreshButton;

public class ItineraryList extends FragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener,
		LocastSyncObserver {

	private static final String TAG = ItineraryList.class.getSimpleName();
	private CursorAdapter mAdapter;
	private GridView mListView;
	private Uri mUri;

	private ImageCache mImageCache;

	/**
	 * If true, checks to ensure that there's an account before showing activity.
	 */
	private static final boolean CHECK_FOR_ACCOUNT = true && Constants.USE_ACCOUNT_FRAMEWORK;

	/**
	 * If true, uses an alternate layout itinerary_item_with_description and loads the itinerary
	 * description in it.
	 */
	private static final boolean SHOW_DESCRIPTION = true;

	private final String[] ITINERARY_DISPLAY = SHOW_DESCRIPTION ? new String[] { Itinerary._TITLE,
			Itinerary._THUMBNAIL, Itinerary._DESCRIPTION } : new String[] { Itinerary._TITLE,
			Itinerary._THUMBNAIL, Itinerary._CASTS_COUNT, Itinerary._FAVORITES_COUNT };

	private final int[] ITINERARY_LAYOUT_IDS = SHOW_DESCRIPTION ? new int[] { android.R.id.text1,
			R.id.media_thumbnail, android.R.id.text2 } : new int[] { android.R.id.text1,
			R.id.media_thumbnail, R.id.casts, R.id.favorites };

	private static String LOADER_DATA = "edu.mit.mobile.android.locast.LOADER_DATA";

	private boolean mSyncWhenLoaded = true;

	private RefreshButton mRefresh;

	private Object mSyncHandle;
	private NotificationProgressBar mProgressBar;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
			case LocastSyncStatusObserver.MSG_SET_REFRESHING:
				if (Constants.DEBUG){
					Log.d(TAG, "refreshing...");
				}
				mProgressBar.showProgressBar(true);
				mRefresh.setRefreshing(true);
				break;

			case LocastSyncStatusObserver.MSG_SET_NOT_REFRESHING:
				if (Constants.DEBUG){
					Log.d(TAG, "done loading.");
				}
				mProgressBar.showProgressBar(false);
				mRefresh.setRefreshing(false);
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_grid_activity);
		mProgressBar =(NotificationProgressBar) (findViewById(R.id.progressNotification));

		findViewById(R.id.refresh).setOnClickListener(this);
		findViewById(R.id.home).setOnClickListener(this);

		mListView = (GridView) findViewById(android.R.id.list);
		mListView.setOnItemClickListener(this);
		mListView.setEmptyView(findViewById(R.id.progressNotification));
		mRefresh = (RefreshButton) findViewById(R.id.refresh);
		mRefresh.setOnClickListener(this);

		final Intent intent = getIntent();
		final String action = intent.getAction();

		mImageCache = ImageCache.getInstance(this);

		if (Intent.ACTION_VIEW.equals(action)) {
			loadData(intent.getData());

		} else if (Intent.ACTION_MAIN.equals(action)) {
			loadData(Itinerary.CONTENT_URI);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSyncWhenLoaded = true;
		mSyncHandle = LocastSyncStatusObserver.registerSyncListener(this, mUri, this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		LocastSyncStatusObserver.unregisterSyncListener(this, mSyncHandle);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * Override this if you wish to show alternate columns.
	 *
	 * @return the list of Itinerary columns to display
	 */
	public String[] getItineraryDisplay() {
		return ITINERARY_DISPLAY;
	}

	/**
	 * Override this if you wish to use IDs other than {@link #ITINERARY_LAYOUT_IDS}.
	 *
	 * @return the list of view ids to map the {@link #getItineraryDisplay()} to
	 */
	public int[] getItineraryLayoutIds() {
		return ITINERARY_LAYOUT_IDS;
	}

	/**
	 * By default, returns {@link #getItineraryDisplay()} with {@link BaseColumns#_ID} added.
	 *
	 * @return the projection to use to select the items in the display.
	 */
	public String[] getItineraryProjection() {
		return ArrayUtils.concat(new String[] { Itinerary._ID }, getItineraryDisplay());
	}

	public int getItineraryItemLayout() {
		return SHOW_DESCRIPTION ? R.layout.itinerary_item_with_description
				: R.layout.itinerary_item;
	}

	private void loadData(Uri data) {
		final String type = getContentResolver().getType(data);

		if (!MediaProvider.TYPE_ITINERARY_DIR.equals(type)) {
			throw new IllegalArgumentException("cannot load type: " + type);
		}
		mAdapter = new SimpleThumbnailCursorAdapter(this, getItineraryItemLayout(), null,
				getItineraryDisplay(), getItineraryLayoutIds(),
				new int[] { R.id.media_thumbnail }, 0);

		mListView.setAdapter(new ImageLoaderAdapter(this, mAdapter, mImageCache,
				new int[] { R.id.media_thumbnail }, 48, 48, ImageLoaderAdapter.UNIT_DIP));

		final LoaderManager lm = getSupportLoaderManager();
		final Bundle loaderArgs = new Bundle();
		loaderArgs.putParcelable(LOADER_DATA, data);
		lm.initLoader(0, loaderArgs, this);
		setTitle(R.string.itineraries);
		mUri = data;

	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		((TextView) findViewById(android.R.id.title)).setText(title);
	}

	@Override
	public void setTitle(int title) {
		super.setTitle(title);
		((TextView) findViewById(android.R.id.title)).setText(title);
	}

	private void refresh(boolean explicitSync) {
		LocastSync.startSync(this, mUri, explicitSync);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri data = args.getParcelable(LOADER_DATA);

		final CursorLoader cl = new CursorLoader(this, data, getItineraryProjection(), null, null,
				Itinerary.SORT_DEFAULT);
		cl.setUpdateThrottle(Constants.UPDATE_THROTTLE);
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mAdapter.swapCursor(c);
		if (mSyncWhenLoaded) {
			mSyncWhenLoaded = false;
			if (mListView.getAdapter().isEmpty()) {
				LocastSync.startExpeditedAutomaticSync(this, mUri);
			} else {
				refresh(false);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUri, id)));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.refresh:
				refresh(true);
				break;

			case R.id.home:
				startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
				break;
		}
	}

	@Override
	public void onLocastSyncStarted(Uri uri) {
		if (Constants.DEBUG) {
			Log.d(TAG, "refreshing...");
		}
		mProgressBar.showProgressBar(true);
		mRefresh.setRefreshing(true);
	}

	@Override
	public void onLocastSyncStopped(Uri uri) {
		if (Constants.DEBUG) {
			Log.d(TAG, "done loading.");
		}
		mProgressBar.showProgressBar(false);
		mRefresh.setRefreshing(false);

	}
}
