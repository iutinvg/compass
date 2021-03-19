package com.sevencrayons.compass;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class CompassActivity extends AppCompatActivity {

    private static final String TAG = "CompassActivity";

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"
    private Button SendButton;
    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;
    public String NMEA_Str = "";
    public float offset_azimuth = (float) -90.0;
    TimerTask mTimerTask;
    final Handler handler = new Handler();
    Timer t = new Timer();
    TextView hTextView;
    TableRow hTableRow;
    //Button hButton, hButtonStop;
    String Message;
    private TextView hGravity_X_View;
    private TextView hGravity_Y_View;
    private TextView hGravity_Z_View;
    private TextView hMagnet_X_View;
    private TextView hMagnet_Y_View;
    private TextView hMagnet_Z_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        sotwFormatter = new SOTWFormatter(this);

        arrowView = findViewById(R.id.main_image_hands);
        sotwLabel = findViewById(R.id.sotw_label);
        SendButton = findViewById(R.id.SendButton);
        hTextView = (TextView)findViewById(R.id.NMEA_View);

        setupCompass();
        hGravity_X_View = (TextView)findViewById(R.id.Gravity_X);
        hGravity_Y_View = (TextView)findViewById(R.id.Gravity_Y);
        hGravity_Z_View = (TextView)findViewById(R.id.Gravity_Z);


        hMagnet_X_View = (TextView)findViewById(R.id.Magnetic_X);
        hMagnet_Y_View = (TextView)findViewById(R.id.Magnetic_Y);
        hMagnet_Z_View = (TextView)findViewById(R.id.Magnetic_Z);


        setupCompass();


        findViewById(R.id.SendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SendCompass();
                            doTimerTask();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
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

    @Override
    public <T extends View> T findViewById(@IdRes int id) {
        return getDelegate().findViewById(id);
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

                        adjustArrow(azimuth+GetDisplayAngle()+offset_azimuth);
                        adjustSotwLabel(azimuth);
                        NMEA_Str = sotwFormatter.NMEA_format(azimuth);
                        hTextView.setText(NMEA_Str);

                    }
                });
            }

           /* @Override
            public void onNewPitch(float pitch) {

            }*/
        };
    }


    private int GetDisplayAngle() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenOrientation = display.getRotation();
        int angle;
        switch (screenOrientation) {
            default:
            case android.view.Surface.ROTATION_0:
                angle = 0;
                break;
            case android.view.Surface.ROTATION_90:
                angle = 90;
                break;
            case android.view.Surface.ROTATION_180:
                angle = 180;
                break;
            case android.view.Surface.ROTATION_270:
                angle = 270;
                break;
        }
        return angle;
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

    class MyTask1 extends TimerTask
    {
        public MyTask1()
        {
        }

        @Override
        public void run()
        {

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
        timer.schedule(new MyTask1(),1000, 500);
// Der Timer beendet sich nach dem Singleshot nicht sofort
    //    Thread.sleep(500);






     /*   mTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        //                      nCounter++;
                        // update TextView
                        hTextView.setText(NMEA_Str);
                        try {
                        SendCompass();
                        } catch (IOException e) {
                             e.printStackTrace();
                         }
                        Log.d("TIMER", "TimerTask run");
                    }
                });
            }};

        // public void schedule (TimerTask task, long delay, long period)
        t.schedule(mTimerTask, 500, 1000);
*/
    }

}
