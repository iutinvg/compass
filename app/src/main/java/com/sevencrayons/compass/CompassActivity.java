package com.sevencrayons.compass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;


public class CompassActivity extends AppCompatActivity {

    private static final String TAG = "CompassActivity";

    private Button btnDestination;
    private ImageView arrowView;

    private Compass compass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        setupCompass();
        setupDestinationButton();
    }

    @Override
    protected void onStart() {
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

    private void setupDestinationButton() {
        btnDestination = (Button) findViewById(R.id.btn_destination);
        btnDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDestinationActivity();
            }
        });
    }

    private void startDestinationActivity() {
        Intent i = new Intent(this, DestinationActivity.class);
        startActivity(i);
    }

    private void setupCompass() {
        arrowView = (ImageView) findViewById(R.id.main_image_hands);
        compass = new Compass(this);
        Compass.CompassListener cl = new Compass.CompassListener(){
            private float currentAzimuth;

            @Override
            public void onNewAzimuth(float azimuth) {
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
        };
        compass.setListener(cl);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_compass, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
