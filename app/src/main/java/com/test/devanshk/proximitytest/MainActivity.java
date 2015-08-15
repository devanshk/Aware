package com.test.devanshk.proximitytest;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class MainActivity extends ActionBarActivity {
    final int demo_request_code=0;
    final String recommendedReactionsString = "WakeUp StartCamera None";

    public static CustomExpandableView[] expandableViews;
    private static ArrayList<View> reactionViews = new ArrayList<View>();
    private LinearLayout parentLayout;
    private Switch onOff;
    private static SharedPreferences prefs;
    public static SharedPreferences.Editor editor;

    public static Activity parent;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = this;

        Intent intent = new Intent(this, AwareService.class);
        this.startService(intent);

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        if (prefs.getBoolean("firstOpen",true)){
            Intent i = new Intent(this,DemoActivity.class);
            startActivityForResult(i,demo_request_code);
        }

        expandableViews = new CustomExpandableView[AwareService.Action.values().length]; //Initializes the array with a spot for each detectable action
        final AwareService.Action[] actions = AwareService.Action.values();
        final AwareService.Reactions[] reactions = AwareService.Reactions.values();

        onOff = (Switch)findViewById(R.id.onOffSwitch);
        parentLayout = (LinearLayout)findViewById(R.id.parent_layout);

        //Build views
        for (int i=0; i<actions.length-1;i++){
            CustomExpandableView v = new CustomExpandableView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(15, 20, 15, 0);
            v.setLayoutParams(lp);
            v.setBackgroundResource(R.color.primary);

            v.fillData(0, getActionName(actions[i]), true);

            for (int j=0; j<reactions.length;j++){
                View mini = generateReactionItem(getReactionName(reactions[j]));
                mini.setTag(R.id.TAG_ACTION_INDEX, i);
                mini.setTag(R.id.Tag_REACTION_INDEX, j);
                mini.setTag(R.id.TAG_REACTION, reactions[j]);

                mini.setOnClickListener(new View.OnClickListener() { //When a reaction item is clicked, do a few things
                    @Override
                    public void onClick(View v) {
                        Integer actionIndex = (Integer) v.getTag(R.id.TAG_ACTION_INDEX);
                        //Step 1: Make all the views white.
                        for (View rv : reactionViews) {
                            if (rv.getTag(R.id.TAG_ACTION_INDEX).equals(actionIndex)) { //If it's part of the same action
                                rv.setBackgroundColor(getResources().getColor(R.color.unselected_reaction));
                                TextView tv = (TextView) rv;
                                tv.setTextColor(getResources().getColor(R.color.primary_text));
                            }
                        }

                        //Step 2: Make this one red
                        v.setBackgroundColor(getResources().getColor(R.color.accent));
                        TextView tv = (TextView) v;
                        tv.setTextColor(Color.WHITE);

                        //Step 3: Save the change in the code
                        AwareService.reactions[actionIndex] = (AwareService.Reactions) v.getTag(R.id.TAG_REACTION);

                        //Step 4: Save them to preferences
                        AwareService.saveReactions();

                        /* Bonus warning when you tap a "Thrown Into the Air reaction */
                        if ( !prefs.getBoolean("warned",false) &&
                                actions[(Integer)v.getTag(R.id.TAG_ACTION_INDEX)] == AwareService.Action.Thrown &&
                                v.getTag(R.id.TAG_REACTION) != AwareService.Reactions.None) {
                            View d = View.inflate(parent, R.layout.psa_dialog, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                            builder.setView(d);
                            builder.setPositiveButton("Got it.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();

                            editor.putBoolean("warned",true);
                            editor.apply();
                        }
                    }
                });

                reactionViews.add(mini);
                v.addContentView(mini);
            }

            expandableViews[i] = v;
            parentLayout.addView(v);
        }

        //OnChange Listeners
        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    for (CustomExpandableView e : expandableViews)
                        try{
                            e.collapse();
                            YoYo.with(Techniques.FadeOutUp)
                                    .duration(500)
                                    .playOn(e);
                        } catch (Exception ex){ex.printStackTrace();}
                    editor.putBoolean("on", false);
                    editor.commit();
                }
                else {
                    for (CustomExpandableView e : expandableViews) {
                        try{
                            YoYo.with(Techniques.FadeInDown)
                                .duration(500)
                                .playOn(e);
                        } catch(Exception ex){ex.printStackTrace();}
                    }
                    editor.putBoolean("on", true);
                    editor.commit();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==demo_request_code){
            AwareService.loadReactions();
            configureViews();

            /* //Commented out for now until I reskin the dialog
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_default,null);

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setView(dialogView);
            b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Push default reactions string into preferences
                    editor.putString("ReactionArray",recommendedReactionsString);
                    editor.commit();
                    AwareService.loadReactions();
                    configureViews();
                }
            });
            b.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AwareService.loadReactions();
                    configureViews();
                }
            });
            b.show();
            */
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    TextView generateReactionItem(String name){
        TextView v = new TextView(this);
        v.setBackground(getResources().getDrawable(android.R.color.white));
        v.setPadding(0,30,20,30);
        v.setTextSize(17);
        v.setGravity(Gravity.CENTER);
        v.setText(name);
        return v;
    }

    private String getActionName(AwareService.Action a){
        switch (a){
            case Wave:
                return "Wave Hand";
            case PulledOutOfPocket:
                return "Pulled Out of Pocket";
            case Thrown:
                return "Thrown Into the Air";
        }
        return a.toString();
    }

    private String getReactionName(AwareService.Reactions r){
        switch(r){
            case None:
                return "Do Nothing";
            case WakeUp:
                return "Wake Up";
            case StartCamera:
                return "Start Camera";
            case ToggleFlashlight:
                return "Toggle Flashlight";
        }
        return r.toString();
    }

    public static void configureViews() {
        for (View mini : reactionViews) {
            Integer i = (Integer) mini.getTag(R.id.TAG_ACTION_INDEX);
            AwareService.Reactions thisReaction = (AwareService.Reactions) mini.getTag(R.id.TAG_REACTION);

            //Configure all the views at the start
            if (AwareService.reactions[i] == thisReaction) { //If the action in the reaction is this one, make it selected
                mini.setBackgroundColor(parent.getResources().getColor(R.color.accent));
                TextView tv = (TextView) mini;
                tv.setTextColor(Color.WHITE);
            } else {
                mini.setBackgroundColor(parent.getResources().getColor(R.color.unselected_reaction));
                TextView tv = (TextView) mini;
                tv.setTextColor(Color.BLACK);
            }
        }
    }

    public static void printIPAddress(){
        WifiManager wifiManager = (WifiManager) parent.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        System.out.println("IP Address = "+ipString);
    }
}