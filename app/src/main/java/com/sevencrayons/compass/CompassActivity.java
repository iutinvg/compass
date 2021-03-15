package com.sevencrayons.compass;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;


public class CompassActivity extends AppCompatActivity {

    private static final String TAG = "CompassActivity";

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"

    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;
    public String NMEA_Str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        sotwFormatter = new SOTWFormatter(this);

        arrowView = findViewById(R.id.main_image_hands);
        sotwLabel = findViewById(R.id.sotw_label);
        setupCompass();
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

    private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }

    private void adjustArrow(float azimuth) {
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

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
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

    private void SendCompass() throws IOException {

        DatagramSocket localSocket = new DatagramSocket();

        String message = NMEA_Str;

        String remoteServerAddr = "192.168.2.255";

        InetAddress remoteServerInetAddr = InetAddress.getByName(remoteServerAddr);
        int remoteServerPort = 12345;
        DatagramPacket dataGramPacket = new DatagramPacket(message.getBytes(), message.length(), remoteServerInetAddr, remoteServerPort);

        localSocket.send(dataGramPacket);
        localSocket.close();


    }
    int getCheckSum(String s) {
// Checksum berechnen und als int ausgeben
// wird als HEX benötigt im NMEA Datensatz
// zwischen $ oder ! und * rechnen
//
// Matthias Busse 18.05.2014 Version 1.1

        int i, XOR, c;

        for (XOR = 0, i = 0; i < s.length(); i++) {
            c = (byte)s.charAt(i);
            if (c == '*') break;
            if ((c!='$') && (c!='!')) XOR ^= c;
        }
        return XOR;
    }

    class MyTask1 extends TimerTask {
        public MyTask1() {
        }

        @Override
        public void run() {

            // System.out.println(new Date());
            try {

                SendCompass();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void doTimerTask() throws InterruptedException {
        Timer timer = new Timer(true); // true = daemon !
        timer.schedule(new MyTask1(), 1000, 500);

    }
}