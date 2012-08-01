package edu.mit.mobile.android.locast.memorytraces;

import android.os.Bundle;
import edu.mit.mobile.android.locast.data.Itinerary;
import edu.mit.mobile.android.locast.itineraries.ItineraryList;
import edu.mit.mobile.android.locast.memorytraces.R;
import edu.mit.mobile.android.widget.TypefaceSwitcher;

public class StoryList extends ItineraryList {

	public static final String[] STORY_DISPLAY = new String[] { Itinerary._THUMBNAIL,
			Itinerary._TITLE, Itinerary._JOB_TITLE };

	public static final int[] STORY_IDS = new int[] { R.id.media_thumbnail, android.R.id.text1,
			android.R.id.text2 };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TypefaceSwitcher.setTypeface(this, TracesConstants.TYPEFACE_TITLE, android.R.id.title);
	}

	@Override
	public String[] getItineraryDisplay() {
		return STORY_DISPLAY;
	}

	@Override
	public int[] getItineraryLayoutIds() {
		return STORY_IDS;
	}

	@Override
	public int getItineraryItemLayout() {
		return R.layout.mt_stories_item;
	}

	@Override
	public void setTitle(int title) {
		setTitle(getString(title).toUpperCase());
	}

}
