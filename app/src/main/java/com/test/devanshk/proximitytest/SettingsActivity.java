package com.test.devanshk.proximitytest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by devanshk on 7/22/15.
 */
public class SettingsActivity extends PreferenceActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Preference pWaveTime = findPreference("pref_key_wave_time");
        pWaveTime.setSummary(""+prefs.getInt("pref_key_wave_time", 85)+" ms");

        Preference pShakeDif = findPreference("pref_key_shake_dif");
        pShakeDif.setSummary(""+
                ShakePreference.formatSpeed(prefs.getFloat("pref_key_shake_dif", 4f))+
                " meters per second^2");

        Preference vibrationTime = findPreference("vibration_time");
        vibrationTime.setSummary(""+prefs.getString("vibration_time","75")+ "ms");

        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {
                    case "pref_key_wave_time":
                        AwareService.waveTime = sharedPreferences.getInt(key, 0);
                        break;
                    case "pref_key_inclination_threshold":
                        AwareService.inclinationThreshold = sharedPreferences.getInt(key, 0);
                        break;
                    case "pref_key_shake_threshold":
                        AccelListener.shakeThreshold = sharedPreferences.getFloat(key, 4.5f);
                        break;
                    case "vibration_time":
                        findPreference(key).setSummary("" + sharedPreferences.getString(key, "75") + " ms");
                        break;
                }
            }
        });
    }
}