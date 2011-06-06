package com.liquid.settings_customrom.activities;

import com.liquid.settings_customrom.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class InfoPreferenceActivity extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.menu.menu_info);
	}
}
