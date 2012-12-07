package com.iutinvg.compass;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;

public class CompassActivity extends FragmentActivity {

	private static final String TAG = "CompassActivity";

	private Compass compass;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compass);

		compass = new Compass(this);
		compass.arrowView = (ImageView) findViewById(R.id.main_image_arrow);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "start compass");
		compass.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		compass.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		compass.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "stop compass");
		compass.stop();
	}

}
