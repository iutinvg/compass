package com.iutinvg.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class Compass implements SensorEventListener {
	private static final String TAG = "Compass";

	private SensorManager sensorManager;
	private Sensor gsensor;
	private Sensor msensor;
	private float[] mGravity = new float[3];
	private float[] mGeomagnetic = new float[3];
	private float azimuth = 0f;
	private float currectAzimuth = 0;
	private int count = 0;

	// compass arrow to rotate
	public ImageView arrowView = null;

	public Compass(Context context) {
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public void start() {
		sensorManager.registerListener(this, gsensor,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, msensor,
				SensorManager.SENSOR_DELAY_GAME);
	}

	public void stop() {
		sensorManager.unregisterListener(this);
	}

	private void adjustArrow() {
		if (arrowView == null) {
			Log.i(TAG, "arrow view is not set");
			return;
		}
		Log.i(TAG, "will set rotation from " + currectAzimuth + " to "
				+ azimuth);

		Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		currectAzimuth = azimuth;

		an.setDuration(500);
		an.setRepeatCount(0);
		an.setFillAfter(true);

		arrowView.startAnimation(an);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				mGravity = event.values;
				// Log.e(TAG, Float.toString(event.values[0]));
			}

			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mGeomagnetic = event.values;
				// Log.e(TAG, Float.toString(event.values[0]));
			}

			if (count < 50) {
				count++;
				return;
			}

			count = 0;

			if (mGravity != null && mGeomagnetic != null) {
				float R[] = new float[9];
				float I[] = new float[9];
				boolean success = SensorManager.getRotationMatrix(R, I,
						mGravity, mGeomagnetic);
				if (success) {
					float orientation[] = new float[3];
					SensorManager.getOrientation(R, orientation);
					// Log.d(TAG, "azimuth (rad): " + azimuth);
					azimuth = (float) Math.toDegrees(orientation[0]); // orientation
					azimuth = (azimuth + 360) % 360;
					// Log.d(TAG, "azimuth (deg): " + azimuth);
					adjustArrow();
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
