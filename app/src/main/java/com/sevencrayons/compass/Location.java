package com.sevencrayons.compass;


public class Location {
    public interface LocationListener {
        void onAzimuthChange(float azimuthFix);
    }
    private LocationListener listener;

    private float azimuth;

    public Location(LocationListener l) {
        listener = l;
    }

    public void start() {
        azimuth = 0.1f;
    }

    public void stop() {
        azimuth = 0;
        onChangeAzimuth();
    }

    private void onChangeAzimuth() {
        listener.onAzimuthChange(azimuth);
    }
}
