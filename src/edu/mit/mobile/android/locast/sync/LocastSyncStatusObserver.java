package edu.mit.mobile.android.locast.sync;

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

import android.accounts.Account;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SyncStatusObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import edu.mit.mobile.android.locast.Constants;
import edu.mit.mobile.android.locast.accounts.Authenticator;
import edu.mit.mobile.android.locast.data.MediaProvider;

/**
 * <p>
 * A implementation of a SyncStatusObserver that will be used to monitor the current account
 * synchronization and notify to a specific handler when the process is ended
 * </p>
 *
 * <p>
 * To use, call {@link #registerSyncListener(Uri, Account)} and
 * {@link #unregisterSyncListener(Context, Object)} from your activity in the UI thread. Generally,
 * the best way to do this is to call {@link #registerSyncListener(Uri, Account)} in
 * {@link Activity#onResume()} and {@link #unregisterSyncListener(Context, Object)} in
 * {@link Activity#onPause()}.
 * </p>
 *
 * @author Cristian Piacente, Steve Pomeroy
 *
 */
public class LocastSyncStatusObserver implements SyncStatusObserver, ServiceConnection {

	private final Account mAccount;

	private final Uri mUri;

	private LocastSyncObserver mSyncObserver;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mSyncObserver == null) {
				return;
			}

			switch (msg.what) {
				case MSG_SET_REFRESHING:
					mSyncObserver.onLocastSyncStarted(mUri);
					break;

				case MSG_SET_NOT_REFRESHING:
					mSyncObserver.onLocastSyncStopped(mUri);
					break;
			}
		};
	};

	private ILocastSimpleSyncService mSyncService;

	private final ILocastSyncObserver mLocastSyncObserver = new ILocastSyncObserver.Stub() {

		@Override
		public void syncStarted(Uri uri) throws RemoteException {
			if (mUri.equals(uri)) {
				mHandler.obtainMessage(MSG_SET_REFRESHING, uri).sendToTarget();
			}
		}

		@Override
		public void syncFinished(Uri uri) throws RemoteException {
			if (mUri.equals(uri)) {
				mHandler.obtainMessage(MSG_SET_NOT_REFRESHING, uri).sendToTarget();
			}
		}
	};

	public static final int MSG_SET_REFRESHING = 100, MSG_SET_NOT_REFRESHING = 101;

	private static final String TAG = LocastSyncStatusObserver.class.getSimpleName();

	public LocastSyncStatusObserver(Account account) {
		this.mAccount = account;
		mUri = null;
	}

	public LocastSyncStatusObserver(Uri uri, LocastSyncObserver observer) {
		mAccount = null;
		mUri = uri;
		mSyncObserver = observer;
	}

	public Uri getUri() {
		return mUri;
	}

	@Override
	public void onStatusChanged(int which) {
		notifySyncStatusToHandler(mAccount);
	}

	/**
	 * @param uri
	 * @param msg
	 *            one of {@link MSG_SET_REFRESHING} or {@link MSG_SET_NOT_REFRESHING}
	 */
	public void onStatusChanged(Uri uri, int msg) {
		mHandler.sendEmptyMessage(msg);
	}

	private static Object registerSyncListener(Uri what, Account account) {
		final Object syncHandle = ContentResolver.addStatusChangeListener(0xff,
				new LocastSyncStatusObserver(account));

		return syncHandle;
	}

	/**
	 * This must be called from the UI thread, probably in onResume()
	 *
	 * @param context
	 * @param what
	 *            the URI of the item whose sync progress you wish to monitor.
	 * @param observer
	 *            the observer which will receive sync progress callbacks. These will be executed on
	 *            the UI thread.
	 * @return a sync handle, which needs to be passed to
	 *         {@link #unregisterSyncListener(Context, Object)}.
	 */
	public static Object registerSyncListener(Context context, Uri what, LocastSyncObserver observer) {
		if (Constants.USE_ACCOUNT_FRAMEWORK) {
			final Account a = Constants.USE_ACCOUNT_FRAMEWORK ? Authenticator
					.getFirstAccount(context) : null;
			return registerSyncListener(what, a);
		} else {
			final LocastSyncStatusObserver svcObserver = new LocastSyncStatusObserver(what,
					observer);

			context.bindService(new Intent(context, LocastSimpleSyncService.class), svcObserver,
					Context.BIND_AUTO_CREATE);
			return svcObserver;
		}
	}

	/**
	 * @param context
	 * @param syncHandle
	 *            the object returned by the earlier call to
	 *            {@link #registerSyncListener(Context, Uri, LocastSyncObserver)}.
	 */
	public static void unregisterSyncListener(Context context, Object syncHandle) {
		if (Constants.USE_ACCOUNT_FRAMEWORK) {
			if (syncHandle != null) {
				ContentResolver.removeStatusChangeListener(syncHandle);
			}
		} else {
			if (syncHandle != null) {
				final LocastSyncStatusObserver observer = (LocastSyncStatusObserver) syncHandle;
				context.unbindService(observer);
				observer.unregisterSelf();

			}
		}
	}

	private void unregisterSelf() {
		mSyncObserver = null;

		try {
			mSyncService.unregisterSyncObserver(mLocastSyncObserver);
		} catch (final RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mSyncService = null;
	}

	private void notifySyncStatusToHandler(Account account) {
		if (Constants.USE_ACCOUNT_FRAMEWORK) {
			if (!ContentResolver.isSyncActive(account, MediaProvider.AUTHORITY)
					&& !ContentResolver.isSyncPending(account, MediaProvider.AUTHORITY)) {
				if (Constants.DEBUG) {
					Log.d(TAG, "Sync finished, should refresh now!!");
				}
				mHandler.sendEmptyMessage(MSG_SET_NOT_REFRESHING);
			} else {
				if (Constants.DEBUG) {
					Log.d(TAG, "Sync Active or Pending!!");
				}
				mHandler.sendEmptyMessage(MSG_SET_REFRESHING);
			}
		} else {
			Log.e(TAG, "need to implement sync status listening");
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mSyncService = ILocastSimpleSyncService.Stub.asInterface(service);
		try {
			mSyncService.registerSyncObserver(mLocastSyncObserver);
		} catch (final RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		if (mSyncService != null) {
			try {
				mSyncService.unregisterSyncObserver(mLocastSyncObserver);
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSyncService = null;
		}

	}
}
