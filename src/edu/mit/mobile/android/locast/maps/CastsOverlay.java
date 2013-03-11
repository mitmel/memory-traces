package edu.mit.mobile.android.locast.maps;
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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.locast.data.Cast;
import edu.mit.mobile.android.locast.itineraries.LocatableItemOverlay;
import edu.mit.mobile.android.locast.memorytraces.R;

public class CastsOverlay extends LocatableItemOverlay {
	private int mOfficialCol, mTitleCol, mDescriptionCol;
	private final Drawable mOfficialCastDrawable;
	private final Context mContext;
	private int mIdCol;

	public static final String[] CASTS_OVERLAY_PROJECTION = ArrayUtils.concat(LOCATABLE_ITEM_PROJECTION,
 new String[] { Cast._ID, Cast._TITLE, Cast._DESCRIPTION,
					Cast._OFFICIAL });

	public CastsOverlay(Context context, MapView mapview) {
        super(boundCenter(context.getResources().getDrawable(R.drawable.ic_map_official)),
				mapview);
		final Resources res = context.getResources();
		mOfficialCastDrawable = boundCenter(res.getDrawable(R.drawable.ic_map_official));
		mContext = context;
	}

	@Override
	protected void updateCursorCols() {
		super.updateCursorCols();
		if (mLocatableItems != null){
			mIdCol = mLocatableItems.getColumnIndex(Cast._ID);
			mTitleCol = mLocatableItems.getColumnIndex(Cast._TITLE);
			mDescriptionCol = mLocatableItems.getColumnIndex(Cast._DESCRIPTION);
			mOfficialCol =  mLocatableItems.getColumnIndex(Cast._OFFICIAL);
		}
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		mLocatableItems.moveToPosition(index);
		final Cast cast = new Cast(mLocatableItems);
		mContext.startActivity(new Intent(Intent.ACTION_VIEW, cast.getCanonicalUri()));

		return true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean drawShadow) {
		if (!drawShadow) {
			super.draw(canvas, mapView, drawShadow);
		}
	}

	@Override
	protected OverlayItem createItem(int i){
		mLocatableItems.moveToPosition(i);

		final ComparableOverlayItem item = new ComparableOverlayItem(
				getItemLocation(mLocatableItems),
				mLocatableItems.getString(mTitleCol), mLocatableItems.getString(mDescriptionCol),
				mLocatableItems.getLong(mIdCol));

        item.setMarker(mOfficialCastDrawable);

		onCreateItem(item);
		return item;
	}
}
