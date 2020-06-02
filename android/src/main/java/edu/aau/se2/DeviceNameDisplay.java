package edu.aau.se2;

import android.os.Build;
import com.badlogic.gdx.backends.android.AndroidApplication;

import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.DefaultNameProvider;

public class DeviceNameDisplay implements DefaultNameProvider {

    private AndroidApplication appl;

    public DeviceNameDisplay(AndroidApplication appl) {
        this.appl = appl;
    }

    public String getDeviceName(){
        String deviceName = Build.MODEL;
        //String deviceName = Build.BRAND;
        return deviceName;
    }
}
