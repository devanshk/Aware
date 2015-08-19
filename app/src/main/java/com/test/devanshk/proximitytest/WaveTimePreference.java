package com.test.devanshk.proximitytest;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

/**
 * Created by devanshk on 7/24/15.
 */
public class WaveTimePreference extends DialogPreference {
    public static boolean calibrating = false;

    static LinearLayout root;
    static Integer waves = 4;
    static Integer waveNumber = 0;
    static long totalTime;
    static View[] waveItems = new View[waves];
    static float averageTime;
    static DialogPreference instance;

    public WaveTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.calibrate_wave_dialog);
        setDialogTitle("");
        setPositiveButtonText("");
        setDialogIcon(null);
    }

    @Override
    protected View onCreateDialogView() {
        //Reset things every time the dialog is opened
        waveNumber = 0;
        totalTime = 0;
        calibrating = true;
        instance = this;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.calibrate_wave_dialog,null);

        root = (LinearLayout)v.findViewById(R.id.calibrate_wave_dialog_root);
        for (int i=0;i < waves;i++){
            View item = inflater.inflate(R.layout.item_detect_wave,null);
            TextView waveTV = (TextView)item.findViewById(R.id.item_wave_number);
            waveTV.setText("Wave "+(i+1));
            waveItems[i]=item;

            //Find the progress bar and set its color to match
            ProgressBar curPB = (ProgressBar)item.findViewById(R.id.item_progress_bar);
            curPB.getIndeterminateDrawable().setColorFilter(getContext().getResources().getColor(R.color.primary),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            if (i!=0)
                curPB.setVisibility(View.INVISIBLE);
            YoYo.with(Techniques.RotateOut).duration(1)
                    .playOn(item.findViewById(R.id.item_wave_time));
            root.addView(item);
        }

        return v;
    }

    public static void logWaveTime(long time){
        totalTime+=time;

        try { //Safety to avoid exceeding max index incase you wave too fast
            //First let's complete the current wave.
            View cur = waveItems[waveNumber];
            ProgressBar curProgress = (ProgressBar) cur.findViewById(R.id.item_progress_bar);
            TextView curTime = (TextView) cur.findViewById(R.id.item_wave_time);
            curTime.setText("" + time);
            if (waveNumber + 1 == waves) //If it's the last one, wait for it to finish animating and then set the new time
                YoYo.with(Techniques.RotateOut).duration(500)
                        .withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                getTime();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .playOn(curProgress);
            else //Otherwise, just make the current progress bar disappear
                YoYo.with(Techniques.RotateOut).duration(500)
                        .playOn(curProgress);

            YoYo.with(Techniques.RotateIn).duration(500)
                    .playOn(curTime);
            waveNumber++;
            if (waveNumber < waves) { //If there's still another wave to be tracked, then let's set it up
                //Second let's start the incoming wave
                View next = waveItems[waveNumber];
                View nextProgress = next.findViewById(R.id.item_progress_bar);
                nextProgress.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.RotateIn).duration(500)
                        .playOn(nextProgress);
            }
        } catch(Exception e){e.printStackTrace();}
    }

    static void getTime(){
        calibrating = false;
        averageTime = totalTime/waves;

        SharedPreferences prefs = instance.getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("pref_key_wave_time", Math.round(averageTime));
        editor.commit();
        instance.setSummary(""+Math.round(averageTime)+" ms");

        instance.getDialog().dismiss();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }
}
