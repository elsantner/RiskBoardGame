package edu.aau.se2;

import android.os.Build;

import edu.aau.se2.view.DefaultNameProvider;

public class DeviceNameDisplay implements DefaultNameProvider {

    public DeviceNameDisplay() {
        //empty method
    }

    public String getDeviceName(){
        //Alternative for MODEL: BRAND
        return Build.MODEL;
    }
}
