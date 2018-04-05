package com.mobinius.sharedatademo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by prajna on 23/8/17.
 */

public class ItemSyncPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
