package edu.mit.mobile.android.locast.casts;

/*
 * Copyright (C) 2011-2012  MIT Mobile Experience Lab
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4_map.app.LoaderManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.TextView;

import com.google.android.maps.MapView;
import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.locast.Constants;
import edu.mit.mobile.android.locast.data.Cast;
import edu.mit.mobile.android.locast.data.CastMedia;
import edu.mit.mobile.android.locast.itineraries.LocatableItemOverlay;
import edu.mit.mobile.android.locast.maps.CastsOverlay;
import edu.mit.mobile.android.locast.memorytraces.R;
import edu.mit.mobile.android.locast.sync.LocastSync;
import edu.mit.mobile.android.locast.sync.LocastSyncObserver;
import edu.mit.mobile.android.locast.sync.LocastSyncStatusObserver;
import edu.mit.mobile.android.widget.NotificationProgressBar;
import edu.mit.mobile.android.widget.RefreshButton;
import edu.mit.mobile.android.widget.ValidatingCheckBox;

public class CastDetail extends LocatableDetail implements LoaderManager.LoaderCallbacks<Cursor>,
		OnItemClickListener, OnClickListener, LocastSyncObserver {
	private static final String TAG = CastDetail.class.getSimpleName();

	/**
	 * Enable/disable the use of a map in this activity. If the map is disabled, the layout can
	 * fully remove the map or simply let this activity hide it.
	 */
	private static final boolean USE_MAP = true;

	private LoaderManager mLoaderManager;
	private CastsOverlay mCastsOverlay;
	private SimpleCursorAdapter mCastMedia;

	private ValidatingCheckBox vcb;

	private static final int LOADER_CAST = 0, LOADER_CAST_MEDIA = 1;

	private Uri mCastMediaUri;

	private static final int REQUEST_SIGNIN = 0;

	private static final String[] CAST_PROJECTION = ArrayUtils.concat(new String[] { Cast._ID,
			Cast._TITLE, Cast._AUTHOR, Cast._DESCRIPTION, Cast._FAVORITED },
			CastsOverlay.CASTS_OVERLAY_PROJECTION);

	private Object mSyncHandle;

	private NotificationProgressBar mProgressBar;

	private boolean mFirstLoad = true;

	private RefreshButton mRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.cast_detail);

		mProgressBar =(NotificationProgressBar) (findViewById(R.id.progressNotification));
		final Uri data = getIntent().getData();

		mCastMediaUri = Cast.getCastMediaUri(data);

		if (USE_MAP) {
			initOverlays();
			// mMapController = ((MapView) findViewById(R.id.map)).getController();
		} else {
			final View mapContainer = findViewById(R.id.pointer_shadow);
			if (mapContainer != null) {
				mapContainer.setVisibility(View.GONE);
			}
		}

		mLoaderManager = getSupportLoaderManager();
		mLoaderManager.initLoader(LOADER_CAST, null, this);
		mLoaderManager.initLoader(LOADER_CAST_MEDIA, null, this);
		findViewById(R.id.home).setOnClickListener(this);
		findViewById(R.id.refresh).setOnClickListener(this);
		mRefresh = (RefreshButton) findViewById(R.id.refresh);
		mRefresh.setOnClickListener(this);

		final Gallery castMediaView = (Gallery) findViewById(R.id.cast_media);

		mCastMedia = new CastMediaAdapter(this);

		castMediaView.setEmptyView(findViewById(R.id.progressNotification));
		castMediaView.setAdapter(new ImageLoaderAdapter(this, mCastMedia, ImageCache
				.getInstance(this), new int[] { R.id.media_thumbnail }, 480, 360,
				ImageLoaderAdapter.UNIT_DIP));

		castMediaView.setOnItemClickListener(this);

		castMediaView.setEnabled(true);

		final String action = getIntent().getAction();
		if (Intent.ACTION_DELETE.equals(action)) {
			showDialog(DIALOG_CONFIRM_DELETE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocastSyncStatusObserver.unregisterSyncListener(this, mSyncHandle);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSyncHandle = LocastSyncStatusObserver.registerSyncListener(this, mCastMediaUri, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.home:
				startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
				break;

			case R.id.refresh:
				LocastSync.startSync(this, getIntent().getData(), true);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

		final Cursor c = (Cursor) adapter.getItemAtPosition(position);
		CastMedia.showMedia(this, c, mCastMediaUri);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri data = getIntent().getData();
		CursorLoader cl = null;
		switch (id) {
			case LOADER_CAST:
				cl = new CursorLoader(this, data, CAST_PROJECTION, null, null, null);
				break;
			case LOADER_CAST_MEDIA:

				cl = new CursorLoader(this, mCastMediaUri, CastMediaAdapter.CAST_MEDIA_PROJECTION,
						null, null, null);
				break;
		}
		cl.setUpdateThrottle(Constants.UPDATE_THROTTLE);
		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch (loader.getId()) {
			case LOADER_CAST:
				if (mCastsOverlay != null) {
					mCastsOverlay.swapCursor(c);
				}
				if (c.moveToFirst()) {
					setTitle(c.getString(c.getColumnIndex(Cast._TITLE)));
					final TextView author = ((TextView) findViewById(R.id.author));
					if (author != null) {
						author.setText(c.getString(c.getColumnIndex(Cast._AUTHOR)));
					}

					((TextView) findViewById(R.id.description)).setText(c.getString(c
							.getColumnIndex(Cast._DESCRIPTION)));
					((CheckBox) findViewById(R.id.favorite)).setChecked(c.getInt(c
							.getColumnIndex(Cast._FAVORITED)) != 0);

					if (USE_MAP) {
						setPointerFromCursor(c);
					}

				}
				break;

			case LOADER_CAST_MEDIA:
				mCastMedia.swapCursor(c);
				/*
				 * for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
				 * MediaProvider.dumpCursorToLog(c, CastMedia.PROJECTION); }
				 */
				if (mFirstLoad && c.getCount() == 0) {
					LocastSync.startExpeditedAutomaticSync(this, mCastMediaUri);
				}
				mFirstLoad = false;

				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
			case LOADER_CAST:
				if (mCastsOverlay != null) {
					mCastsOverlay.swapCursor(null);
				}
				break;

			case LOADER_CAST_MEDIA:
				mCastMedia.swapCursor(null);
				break;
		}
	}

	@Override
	protected LocatableItemOverlay createItemOverlay() {
		mCastsOverlay = new CastsOverlay(this, (MapView) findViewById(R.id.map));
		mCastsOverlay.setShowBalloon(false);
		return mCastsOverlay;
	}

	private Runnable mDoAfterAuthentication;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SIGNIN:
				if (resultCode == RESULT_OK) {
					runOnUiThread(mDoAfterAuthentication);
				}
				mDoAfterAuthentication = null;
				break;
		}
	}

	private static final int DIALOG_CONFIRM_DELETE = 100;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CONFIRM_DELETE:
				return new AlertDialog.Builder(this)
						.setPositiveButton(R.string.dialog_button_delete,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										final Uri cast = getIntent().getData();
										final ContentResolver cr = getContentResolver();
										cr.delete(Cast.getCastMediaUri(cast), null, null);
										final int count = cr.delete(cast, null, null);
										if (Constants.DEBUG) {
											if (count != 1) {
												Log.w(TAG, "got non-1 count from delete()");
											}
										}
										finish();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}
								}).setCancelable(true).setTitle(R.string.cast_delete_title)
						.setMessage(R.string.cast_delete_message)

						.create();

			default:
				return super.onCreateDialog(id);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		((TextView) findViewById(R.id.title)).setText(title);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
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
