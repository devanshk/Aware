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
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Preference pWaveTime = findPreference("pref_key_wave_time");
        pWaveTime.setSummary(""+prefs.getInt("pref_key_wave_time",85)+" ms");

        Preference pShakeDif = findPreference("pref_key_shake_dif");
        pShakeDif.setSummary(""+
                ShakePreference.formatSpeed(prefs.getFloat("pref_key_shake_dif", 4f))+
                " meters per second^2");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        Log.e("AWARE_APP", "Logged Preference Change.");

        Preference connectionPref = findPreference(key);
        // Set summary to be the user-description for the selected value
        String value = (String) newValue;
        connectionPref.setSummary(value);

        switch (key) {
            case "pref_key_wave_time":
                AwareService.waveTime = (int) Math.round(Double.parseDouble(value));
                break;
            case "pref_key_inclination_threshold":
                AwareService.inclinationThreshold = (int) Math.round(Double.parseDouble(value));
                break;
            case "pref_key_shake_threshold":
                AccelListener.shakeThreshold = (float)Double.parseDouble(value);
                break;
        }
        return false;
    }
}