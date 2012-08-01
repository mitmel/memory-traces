package edu.mit.mobile.android.locast.memorytraces;

import android.os.Bundle;
import edu.mit.mobile.android.locast.memorytraces.R;
import edu.mit.mobile.android.widget.TypefaceSwitcher;

public class CastDetail extends edu.mit.mobile.android.locast.casts.CastDetail {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TypefaceSwitcher.setTypeface(this, TracesConstants.TYPEFACE_TITLE, R.id.title);
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title.toString().toUpperCase());
	}
}
