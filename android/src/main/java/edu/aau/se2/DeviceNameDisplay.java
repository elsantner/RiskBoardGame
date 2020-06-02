package edu.aau.se2;

import android.os.Build;

import edu.aau.se2.view.DefaultNameProvider;

public class DeviceNameDisplay implements DefaultNameProvider {

    public DeviceNameDisplay() {
    }

    public String getDeviceName(){
        String deviceName = Build.MODEL;
        //Alternative for MODEL: BRAND
        return deviceName;
    }
}
