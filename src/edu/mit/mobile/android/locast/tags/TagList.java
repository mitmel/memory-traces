package edu.mit.mobile.android.locast.tags;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.locast.data.Cast;
import edu.mit.mobile.android.locast.data.Tag;
import edu.mit.mobile.android.locast.data.TaggableItem;
import edu.mit.mobile.android.locast.memorytraces.R;
import edu.mit.mobile.android.locast.sync.LocastSync;
import edu.mit.mobile.android.locast.sync.LocastSyncObserver;
import edu.mit.mobile.android.locast.sync.LocastSyncStatusObserver;
import edu.mit.mobile.android.widget.NotificationProgressBar;

public class TagList extends FragmentActivity implements LoaderCallbacks<Cursor>,
		OnItemClickListener, LocastSyncObserver {

	private static final String[] FROM = new String[] { Tag._NAME };

	private static final int[] TO = new int[] { android.R.id.text1 };

	private SimpleCursorAdapter mAdapter;

	private Uri mUri;

	private NotificationProgressBar mProgressNotification;

	private GridView mList;

	private Object mSyncHandle;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.simple_grid_activity);

		setTitle(getTitle());

		mList = (GridView) findViewById(android.R.id.list);
		mProgressNotification = (NotificationProgressBar) findViewById(R.id.progressNotification);

		mAdapter = new SimpleCursorAdapter(this, getTagItemLayout(), null, getTagDisplay(),
				getTagLayoutIds(), 0);

		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);

		mUri = getIntent().getData();

		if (mUri == null) {
			mUri = Tag.CONTENT_URI;
		}

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocastSyncStatusObserver.unregisterSyncListener(this, mSyncHandle);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// it's not currently possible to sync tag URIs.
		mSyncHandle = LocastSyncStatusObserver.registerSyncListener(this, Cast.CONTENT_URI, this);
		LocastSync.startSync(this, Cast.CONTENT_URI);
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		final TextView titleView = (TextView) findViewById(android.R.id.title);
		if (titleView != null) {
			titleView.setText(title);
		}
	}

	@Override
	public void setTitle(int titleId) {

		super.setTitle(titleId);
		final TextView titleView = (TextView) findViewById(android.R.id.title);
		if (titleView != null) {
			titleView.setText(titleId);
		}
	}

	public String[] getTagDisplay() {
		return FROM;
	}

	public String[] getTagProjection() {
		return ArrayUtils.concat(new String[] { Tag._ID }, getTagDisplay());
	}

	public int[] getTagLayoutIds() {
		return TO;
	}

	public int getTagItemLayout() {
		return android.R.layout.simple_list_item_1;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		return new CursorLoader(this, mUri, getTagProjection(), null, null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mAdapter.swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Cursor c = mAdapter.getCursor();
		c.moveToPosition(position);
		final String tag = c.getString(c.getColumnIndex(Tag._NAME));

		startActivity(new Intent(Intent.ACTION_VIEW, TaggableItem.getTagUri(mUri, tag)));

	}

	@Override
	public void onLocastSyncStarted(Uri uri) {

		mProgressNotification.showProgressBar(true);

		mProgressNotification.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLocastSyncStopped(Uri uri) {
		mProgressNotification.showProgressBar(false);

		mProgressNotification.setVisibility(View.GONE);

	}
}
