package com.reuvenab.compass;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

//import com.google.android.gms.location;


public class CompassActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "CompassActivity";
    private static final String PERM_TAG = "Permissions";

    private Compass compass;
    private ImageView arrowView;
    private ImageView templeArrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"
    private TextView gpsLabel;  //


    private int currentAzimuth;
    private int currentTempleMountDegree;

    private SOTWFormatter sotwFormatter;

    private static final double TEMPLE_MOUNT_LATITUDE = 31.77765;
    private static final double TEMPLE_MOUNT_LONGITUDE = 35.23547;

    // 31.7780, 35.2354

    private int gpsPermissionGrantedMask;


    private static final int ACCESS_FINE_LOCATION_PERMISSION_CODE = 0x400 + 0x1;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_CODE = 0x400 + 0x2;
    private static final int ALL_PERMISSION_GRANTED = ACCESS_FINE_LOCATION_PERMISSION_CODE | ACCESS_COARSE_LOCATION_PERMISSION_CODE;


    private void enqueueGpsPermissionCheck() {
        enqueuePermissionCheck(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION_PERMISSION_CODE);
        enqueuePermissionCheck(Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_COARSE_LOCATION_PERMISSION_CODE);
    }

    /***
     *
     * @param permission
     * @param requestCode
     * @return true if permission check was enqueued
     */
    private boolean enqueuePermissionCheck(String permission, int requestCode) {
        Log.d(PERM_TAG, "enqueuePermissionCheck " + permission);
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return true;
        }
        gpsPermissionGrantedMask |= requestCode;
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        sotwFormatter = new SOTWFormatter(this);

        arrowView = findViewById(R.id.main_image_hands);
        templeArrowView = findViewById(R.id.main_image_jerusalem_hands);
        sotwLabel = findViewById(R.id.sotw_label);
        gpsLabel = findViewById(R.id.gps_label);

        gpsPermissionGrantedMask = 0;

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        enqueueGpsPermissionCheck();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException ex) {
            Log.d(PERM_TAG, "requestLocationUpdates failed: " + ex.getMessage());
        }

        setupCompass();
        gpsLabel.setText(getString(R.string.no_location_available));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start compass and gps");
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

    private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }

    private void adjustArrow(int azimuth) {
        Log.d(TAG, "will set rotation from " + currentAzimuth + " to "
                + azimuth);

        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }

    private void adjustSotwLabel(float azimuth) {
        sotwLabel.setText(sotwFormatter.format(azimuth));
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        Log.d(PERM_TAG, "onRequestPermissionsResult for " + requestCode);
        if (grantResults.length > 0) {
            Log.d(PERM_TAG, "Granted result is " + grantResults[0]);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switch (requestCode) {
                    case ACCESS_COARSE_LOCATION_PERMISSION_CODE:
                        gpsPermissionGrantedMask |= ACCESS_COARSE_LOCATION_PERMISSION_CODE;
                        break;
                    case ACCESS_FINE_LOCATION_PERMISSION_CODE:
                        gpsPermissionGrantedMask |= ACCESS_FINE_LOCATION_PERMISSION_CODE;
                        break;
                }
            }
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            enqueueGpsPermissionCheck();
            return;
        }
        final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adjustTempleArrow(location);
            }
        });

    }

    private void adjustGpsLabel(Location location) {

        Log.d(TAG, "Location Latitude: " + location.getLatitude() + " Longitude "
                + location.getLongitude());

        gpsLabel.setText("Location Latitude: " + location.getLatitude() + " Longitude "
                + location.getLongitude());
    }

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final int azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);
                        adjustSotwLabel(azimuth);
                    }
                });
            }
        };
    }

    @Override
    public void onLocationChanged(final Location location) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adjustTempleArrow(location);
            }
        });

    }

    private int calcNewTempleMountDegree(Location location) {
        double adjacent = location.getLongitude() - TEMPLE_MOUNT_LONGITUDE;
        double opposite  = location.getLatitude() - TEMPLE_MOUNT_LATITUDE;

        float degrees = (float)
                Math.toDegrees(Math.atan2(Math.abs(opposite), Math.abs(adjacent))
                );

        Log.d(TAG, "Abs ang " + degrees);

        float abs_ang = degrees;

        // at the GUI control `0 degree is at 12 o'clock and degrees go round a clock
        // [1, -1]
        if (adjacent > 0 && opposite < 0)
        {
            degrees += 90;
        }
        else
        {
            // [-1, -1]
            if (adjacent < 0 && opposite < 0)
            {
                degrees = 90 - degrees;
                degrees += 180;
            } else {
                // [-1, 1]
                if (adjacent < 0 && opposite > 0) {
                    degrees += 270;
                }
                // [1, 1]
                else {
                    degrees = 90 - degrees;
                }
            }

        }
        Log.d(TAG, "Ang " + degrees);

        gpsLabel.setText("Location opposite: " + opposite + " adjacent " + adjacent + " abs ang: " + abs_ang +  " ang: " + degrees);

        // now arrow is pointing at the opposite direction to the Temple Mount
        degrees = (degrees + 180 ) % 360;

        int azimuthInDegrees = (360 - currentAzimuth);

        // now arrow is pointing at the Temple Mount under assumption that person phone is facing North ( degree 0)
        degrees = (degrees + azimuthInDegrees) % 360;

        return (int)degrees;
    }

    private void adjustTempleArrow(Location location) {

        int newTempleMountDegree = calcNewTempleMountDegree(location);

        Log.d(TAG, "will set Temple Mount direction from using azimuth " + currentTempleMountDegree);

        //if (Math.abs(newTempleMountDegree - currentTempleMountDegree) > 3)
        {
            float rotateFrom = currentTempleMountDegree;
            float rotateTo = newTempleMountDegree;

            // rotate using shortest direction
            if (Math.abs(rotateFrom - rotateTo)>180)
            {
                if (rotateFrom > rotateTo)
                    rotateTo += 360;
                else
                    rotateFrom += 360;
            }
            Animation an = new RotateAnimation(rotateFrom, rotateTo,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);

            currentTempleMountDegree = newTempleMountDegree;

            an.setDuration(500);
            an.setRepeatCount(0);
            an.setFillAfter(true);

            templeArrowView.startAnimation(an);
        }

    }




    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        gpsLabel.setText("Getting your location");
    }

    @Override
    public void onProviderDisabled(String s) {
        gpsLabel.setText("No location information available");
    }
}
