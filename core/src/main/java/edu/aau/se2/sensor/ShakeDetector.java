package edu.aau.se2.sensor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShakeDetector {
    private static final float CONST_ACCELERATION = 9.8f;

    private static boolean availableA;
    private static boolean availableV;

    static {
        availableA = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
        availableV = Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator);
    }

    private ShakeDetector() {
        // defeat instantiation
    }

    public static boolean isAvailable() {
        return availableA;
    }
    //the method is checking if the accelerometer is supported and if yes, returning the signal that the phone is being shaked
    public static boolean isShaking() {
        if (availableA) {
            float xAxis = Gdx.input.getAccelerometerX() / CONST_ACCELERATION;
            float yAxis = Gdx.input.getAccelerometerY() / CONST_ACCELERATION;
            float zAxis = Gdx.input.getAccelerometerZ() / CONST_ACCELERATION;

            float totalAcceleration = (float) Math.sqrt(xAxis * xAxis) + (yAxis * yAxis) + (zAxis * zAxis);
            return totalAcceleration > 1.5f;

        } else{
            throw new UnsupportedOperationException("The Accelerometer is not available on your device");
        }
    }

    //the method is checking if the vibrator is supported, and if yes, the phone is going to vibrate
    public static void vibrate(){
        if(availableV){
            Gdx.input.vibrate(2000);
        }else {
            throw new UnsupportedOperationException("The Vibration is not available on your device");
        }
    }

}

