package com.test.devanshk.proximitytest;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.test.devanshk.proximitytest.AwareService.Action;
import com.test.devanshk.proximitytest.AwareService.Reactions;

import java.util.Date;

/**
 * Created by devanshk on 7/21/15.
 */
public class AccelListener implements SensorEventListener {
    private final int shakesNeeded = 5;
    private static SharedPreferences prefs;
    private static float averageDelta = 4f;

    private Date lastShake;
    private int shakeCount = 0;
    private long shakeTimeThreshold = 130;

    /* Here we store the current values of acceleration, one for each axis */
    private float xAccel;
    private float yAccel;
    private float zAccel;

    /* And here the previous ones */
    private float xPreviousAccel;
    private float yPreviousAccel;
    private float zPreviousAccel;

    /* Used to suppress the first shaking */
    private boolean firstUpdate = true;

    /*What acceleration difference would we assume as a rapid movement? */
    public static float shakeThreshold = 4f;

    /* Has a shaking motion been started (one direction) */
    private boolean shakeInitiated = false;

    public AccelListener() {
        prefs = PreferenceManager.getDefaultSharedPreferences(AwareService.instance);
    }

    private void executeShakeAction() {
        System.out.println("Calibrating = "+ShakePreference.calibrating);

        if (ShakePreference.calibrating)
            ShakePreference.logShakeDif(averageDelta);
        else if (AwareService.triggers.get(AwareService.triggers.size()-1).isClear()){ //Triggers shake only if the phone is clear, i.e. not in the pocket
            Log.e("AWARE_App", "Detected " + Action.Shake);
            Date now = new Date();
            if (lastShake != null && now.getTime() - lastShake.getTime() < shakeTimeThreshold)
                shakeCount++;
            else {
                Log.e("AWARE_APP", "Logged " + shakeCount + " shakes.");
                shakeCount = 1;
            }

            lastShake = new Date();

            if (shakeCount == shakesNeeded) {
                Action[] actionEnums = Action.values();
                for (int i = 0; i < actionEnums.length - 1; i++) //-1 so it doesn't trigger Action.None
                    if (actionEnums[i] == Action.Shake) {
                        AwareService.executeReaction(AwareService.reactions[i]);
                        i = actionEnums.length;
                    }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        AwareService.accelerometer = se.values;

        if (ShakePreference.calibrating)
            shakeThreshold = 1.7f;
        else
            shakeThreshold = prefs.getFloat("pref_key_shake_dif", 4f);

        updateAccelParameters(se.values[0], se.values[1], se.values[2]);
        if ((!shakeInitiated) && isAccelerationChanged()) {
            shakeInitiated = true;
        } else if ((shakeInitiated) && isAccelerationChanged()) {
            executeShakeAction();
        } else if ((shakeInitiated) && (!isAccelerationChanged())) {
            shakeInitiated = false;
        }
    }

    /* Store the acceleration values given by the sensor */
    private void updateAccelParameters(float xNewAccel, float yNewAccel,
                                       float zNewAccel) {
        /* we have to suppress the first change of acceleration, it results from first values being initialized with 0 */
        if (firstUpdate) {
            xPreviousAccel = xNewAccel;
            yPreviousAccel = yNewAccel;
            zPreviousAccel = zNewAccel;
            firstUpdate = false;
        } else {
            xPreviousAccel = xAccel;
            yPreviousAccel = yAccel;
            zPreviousAccel = zAccel;
        }
        xAccel = xNewAccel;
        yAccel = yNewAccel;
        zAccel = zNewAccel;
    }

    /* If the values of acceleration have changed on at least two axises, we are probably in a shake motion */
    private boolean isAccelerationChanged() {
        float deltaX = Math.abs(xPreviousAccel - xAccel);
        float deltaY = Math.abs(yPreviousAccel - yAccel);
        float deltaZ = Math.abs(zPreviousAccel - zAccel);

        averageDelta = (deltaX+deltaY+deltaZ)/3;
        return (deltaX > shakeThreshold && deltaY > shakeThreshold)
                || (deltaX > shakeThreshold && deltaZ > shakeThreshold)
                || (deltaY > shakeThreshold && deltaZ > shakeThreshold);
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {}
}
