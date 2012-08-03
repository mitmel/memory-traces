package edu.mit.mobile.android.locast.sync;

import android.net.Uri;

/**
 * Callbacks from {@link LocastSyncStatusObserver} which let an Activity monitor sync progress.
 *
 */
public interface LocastSyncObserver {
	/**
	 * Called just before the sync takes place.
	 *
	 * @param uri
	 *            the content item being sync'd
	 */
	public void onLocastSyncStarted(Uri uri);

	/**
	 * Called just after the sync takes place.
	 *
	 * @param uri
	 *            the content item being sync'd
	 */
	public void onLocastSyncStopped(Uri uri);
}