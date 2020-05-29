package edu.aau.se2.desktop;

import edu.aau.se2.view.DefaultNameProvider;

public class DummyDeviceNameDisplay implements DefaultNameProvider {

    @Override
    public String getDeviceName() {
        return "HardCodedDevice";
    }
}
