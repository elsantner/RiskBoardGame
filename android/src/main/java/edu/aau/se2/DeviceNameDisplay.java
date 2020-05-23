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

    @Override
    public void setDefaultName(String message) {
        Player player = new Player();
        player.setNickname(Build.MODEL);
        System.out.println("###MODEL " + Build.MODEL);
        //System.out.println("###BRAND " + Build.BRAND);
        //System.out.println("###DEVICE" + Build.DEVICE);
        //System.out.println("###MANUFACTURER" + Build.MANUFACTURER);
        //System.out.println("###USER " + Build.USER);
        //System.out.println("###HARDWARE "+ Build.HARDWARE);
    }
}
