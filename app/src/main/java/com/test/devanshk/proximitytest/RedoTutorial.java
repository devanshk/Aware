package com.test.devanshk.proximitytest;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;

/**
 * Created by devanshk on 7/24/15.
 */
public class RedoTutorial extends Preference{
    public RedoTutorial(Context context) {
        super(context);
    }

    @Override
    public void onClick(){
        Intent i = new Intent(getContext(),DemoActivity.class);
        getContext().startActivity(i);
    }
}
