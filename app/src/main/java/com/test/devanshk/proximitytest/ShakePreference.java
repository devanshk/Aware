package com.test.devanshk.proximitytest;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by devanshk on 7/24/15.
 */
public class ShakePreference extends DialogPreference {
    public static boolean calibrating = false;
    static final float buffer = 1f;

    static Integer shakes = 50;
    static int shakeCounter = 0;
    static float totalShakeDif;
    static float averageShakeDif;
    static DialogPreference instance;
    static TextView tv;
    static ProgressBar pb;

    public ShakePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.calibrate_wave_dialog);
        setDialogTitle("");
        setPositiveButtonText("");
        setDialogIcon(null);
    }

    @Override
    protected View onCreateDialogView() {
        //Reset things every time the dialog is opened
        shakeCounter = 0;
        totalShakeDif = 0;
        calibrating = true;
        instance = this;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.calibrate_shake_dialog, null);

        //Find the progress bar and set its color to match
        pb = (ProgressBar)v.findViewById(R.id.shake_progressbar);
        pb.getProgressDrawable().setColorFilter(getContext().getResources().getColor(R.color.primary),
                android.graphics.PorterDuff.Mode.SRC_IN);
        pb.setProgress(0);
        pb.setMax(shakes);

        tv = (TextView)v.findViewById(R.id.shake_detected_textview);

        return v;
    }

    public static void logShakeDif(float dif){
        totalShakeDif+=dif;
        shakeCounter++;
        tv.setText("Detected\n" + shakeCounter + " Shakes");
        pb.setProgress(shakeCounter);
        if (shakeCounter >= shakes){
            calibrating=false;
            getTime();
        }
    }

    static void getTime(){
        calibrating = false;
        averageShakeDif = totalShakeDif/shakeCounter - buffer;
        if (averageShakeDif < 1.8) averageShakeDif = 1.8f;

        SharedPreferences prefs = instance.getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("pref_key_shake_dif", averageShakeDif);
        editor.commit();
        instance.setSummary(""+formatSpeed(averageShakeDif) + " meters per second^2");

        instance.getDialog().dismiss();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        calibrating=false;
        super.onDialogClosed(positiveResult);
    }

    public static float formatSpeed(float speed){
        int temp = Math.round(speed*100);
        return temp/100f;
    }
}
