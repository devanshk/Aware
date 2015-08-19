package com.test.devanshk.proximitytest;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by devanshk on 7/3/15.
 */
public class AwareService extends Service implements SensorEventListener{
    private static final boolean selfish = false;
    private static final int buffer = 50;
    private static final int vibrationDuration = 75;

    enum Action {Wave, Shake, PulledOutOfPocket, Thrown, None}
    enum Reactions {None, WakeUp, StartCamera, ToggleFlashlight}
    Sensor mProximity, mAccelerometer, mMagnetic;
    SensorManager mSensorManager;
    public static int waveTime = 85;
    public static int pocketTime = 4000;
    public static int inclinationThreshold = 60;
    static boolean faceUp = true;
    static Service instance;
    static AccelListener acelListener;
    static MagnetListener magListener;
    static Toast startCameraToast;
    static PowerManager.WakeLock wakeLock;
    static PowerManager pm;
    static Vibrator vab;

    public static Reactions[] reactions = new Reactions[Action.values().length-1];
    static ArrayList<Trigger> triggers = new ArrayList<Trigger>();
    static ArrayList<Trigger> faceUpTriggers = new ArrayList<Trigger>();
    static float[] rotationMatrix = new float[9];
    static float[] inclinationMatrix = new float[9];
    static float[] accelerometer; // values from sensor
    static float[] magnetic; // values from sensor

    static SharedPreferences prefs;
    static SharedPreferences.Editor editor;
    static String defaultReactions="";

    private static Camera camera;
    private static boolean isFlashOn;
    static Camera.Parameters params;

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        instance = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        startCameraToast = Toast.makeText(AwareService.instance, "Starting Camera", Toast.LENGTH_SHORT);

        loadReactions();
        MainActivity.configureViews();
        Log.e("AWARE_APP", "Configured Views from the Aware Service.");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        acelListener = new AccelListener();
        magListener = new MagnetListener();

        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(acelListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(magListener, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);

        for (int i=0;i<reactions.length;i++) {
            reactions[i] = Reactions.None;
        }
        for (int i=0; i<reactions.length;i++){
            defaultReactions+="None ";
        }
        defaultReactions+="None";
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        loadReactions();
        triggers.add(new Trigger(event.values[0], new Date()));
        Log.e("AWARE_App", "Detected " + getAction());
        String s = "";
        for (Reactions r : reactions){
            s += ", "+r;
        }
        Log.e("AWARE_APP", "Reactions are..." + s);
        Action[] actionEnums = Action.values();
        Action detected = getAction();
        if (WaveTimePreference.calibrating && detected == Action.Wave){ //If we're calibrating wave time and they wave
            WaveTimePreference.logWaveTime(triggerTimePassed());
        }
        if (!WaveTimePreference.calibrating){ //If we're not calibrating, then execute the reaction
            for (int i = 0; i < actionEnums.length - 1; i++) //-1 so it doesn't trigger Action.None
                if (actionEnums[i] == detected) {
                    executeReaction(reactions[i]);
                    i = actionEnums.length;
                }
        }
    }

    @Override
    public void onDestroy(){
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    long triggerTimePassed(){
        Trigger now = triggers.get(triggers.size()-1);
        Trigger last = triggers.get(triggers.size()-2);
        return now.getTime()-last.getTime();
    }

    Action getAction(){
        //Do all the proximity sensor gesture detection here
        if (WaveTimePreference.calibrating)
            waveTime=300;
        else if (prefs!=null)
            waveTime = prefs.getInt("pref_key_wave_time", 85);

        if (triggers.size()<2)
            return Action.None;
        Trigger now = triggers.get(triggers.size()-1);
        Trigger last = triggers.get(triggers.size()-2);
        long millisPassed = now.getTime()-last.getTime();
        if (WaveTimePreference.calibrating)
            if (millisPassed<waveTime && now.isClear()) //If the time passed is less than the max time taken to wave and the screen is uncovered
                return Action.Wave;
        if (millisPassed<waveTime+buffer && millisPassed>waveTime-buffer*1.15 && now.isClear()) //If the time passed is less than the max time taken to wave and the screen is uncovered
            return Action.Wave;

        //If it wasn't a wave, let's figure out if it entered or exited a pocket.
        if (triggers.size()<3)
            return Action.None;

        if (millisPassed>pocketTime && now.isClear()) { //If it was in the pocket for a long time and it was just pulled out
                return Action.PulledOutOfPocket;
        }

        return Action.None;
    }

    public static void executeReaction(Reactions r){
        if (r == null)
            return;

        switch (r){
            case WakeUp:
                if (faceUp) {
                    if (pm==null){
                        pm = (PowerManager) instance.getSystemService(Context.POWER_SERVICE);
                    }
                    if (!pm.isScreenOn()) { //If the screen is currently off, let's turn it on

                        vibrate();

                        wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                        wakeLock.acquire();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(750);
                                    wakeLock.release();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    try {
                                        wakeLock.release();
                                    } catch (Exception f) {
                                        f.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    }
                }
                break;
            case StartCamera:
                vibrate();

                //First first, let them know we're starting the camera
                startCameraToast.show();
                //...and also wake it up
                PowerManager pm = (PowerManager) instance.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                wakeLock.acquire();
                wakeLock.release();

                //Then start the camera
                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                instance.startActivity(intent);
                break;
            case ToggleFlashlight:
                vibrate();

                if (!isFlashOn) { //Let's turn the flash on if it's off
                    getCamera();
                    if (camera == null || params == null) {
                        return;
                    }

                    params = camera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(params);
                    camera.startPreview();
                    isFlashOn = true;
                } else { //Let's turn the flash off it it's on
                    if (camera == null || params == null) {
                        return;
                    }

                    params = camera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                    camera.stopPreview();
                    isFlashOn = false;

                    if (!selfish) {
                        camera.release();
                        camera = null;
                    }
                }
                break;
        }
    }

    public static void saveReactions(){ //Save the reaction list to preferences
        String str = "";
        for (int i=0;i<reactions.length-1;i++){
            str +=reactions[i]+" ";
        }
        str+=reactions[reactions.length-1];
        editor.putString("ReactionArray",str);
        editor.commit();
    }

    public static void loadReactions(){ //Load the reaction list from preferences
        int index = 0;
        String string = prefs.getString("ReactionArray", defaultReactions);
        if (string.length()==0) {
            string = defaultReactions;
        }
        while (string.contains(" ")){
            String sub = string.substring(0,string.indexOf(" "));
            try{
            reactions[index] = Reactions.valueOf(sub);} catch(Exception e){e.printStackTrace();}
            string = string.substring(string.indexOf(" ")+1);
            index++;
        }
        try{
        reactions[index] = Reactions.valueOf(string);} catch (Exception e){e.printStackTrace();}
    }

    public static void printRotationCoordinates(){
        SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometer, magnetic);
        int inclination = (int) Math.round(Math.toDegrees(Math.acos(rotationMatrix[8])));
        System.out.println("inclination = " + inclination);
    }

    // getting camera parameters
    private static void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
            }
        }
    }

    private static void vibrate(){
        if (prefs.getBoolean("vibrate", true)) {
            vab = (Vibrator) instance.getSystemService(VIBRATOR_SERVICE);
            vab.vibrate(Integer.parseInt(prefs.getString("vibration_time", "" + vibrationDuration)));
        }
    }

    class MagnetListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            magnetic = event.values;
            try {
                SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometer, magnetic);
                int inclination = (int) Math.round(Math.toDegrees(Math.acos(rotationMatrix[8])));

                if (inclination < inclinationThreshold)
                    faceUp = true;
                else
                    faceUp = false;
            } catch(Exception e){e.printStackTrace();}
        }

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {}
    }

    class Trigger{
        private Date time;
        private float value;
        public Trigger (float f, Date d){
            time = d;
            value = f;
        }

        public long getTime() {
            return time.getTime();
        }

        public boolean isClear(){
            return this.value>3;
        }

        @Override
        public String toString() {
            return "Trigger{" +
                    "time=" + time +
                    ", value=" + value +
                    '}';
        }
    }
}
