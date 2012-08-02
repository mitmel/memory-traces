package edu.mit.mobile.android.locast.memorytraces;

import android.database.Cursor;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.locast.data.Itinerary;
import edu.mit.mobile.android.locast.itineraries.ItineraryDetail;

public class StoryDetail extends ItineraryDetail {

	private final String[] STORY_DISPLAY = ArrayUtils.concat(super.getItineraryDisplay(),
			new String[] { Itinerary._JOB_TITLE });

	@Override
	protected String[] getItineraryDisplay() {
		return STORY_DISPLAY;
	}

	@Override
	protected int getContentView() {

		return R.layout.mt_story_detail;
	}

	@Override
	public void setTitle(CharSequence title) {

		super.setTitle(title.toString().trim());
	}

	@Override
	protected void displayItinerary(Loader<Cursor> loader, Cursor c) {
		super.displayItinerary(loader, c);

		((TextView) findViewById(R.id.job_title)).setText(c.getString(c
				.getColumnIndex(Itinerary._JOB_TITLE)));
	}
}
