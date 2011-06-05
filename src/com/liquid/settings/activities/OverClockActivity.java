package com.liquid.settings.activities;

import com.liquid.settings.OverClockStrings;
import com.liquid.settings.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import com.liquid.settings.components.FastDictionary;

public class OverClockActivity extends PreferenceActivity {
	
	ListPreference maxcpulist = (ListPreference)findPreference("maxcpu");
	ListPreference governorlist = (ListPreference)findPreference("governor");
	CheckBoxPreference onboot = (CheckBoxPreference) findPreference ("onboot");
	FastDictionary FD_gov,FD_freq;
	String [] governors,frequencies;
	int n_of_governors, n_of_freqs; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.menu.overclock_menu);
		Log.d("LS_DBG","lol");
		governors = OverClockStrings.getAvailableGovernors();
		frequencies = OverClockStrings.getAvailableFrequencies();
		governorlist.setEntries(governors);
		//maxcpulist.setEntries(frequencies);
		Log.d("LS_DBG","lol 2");
		n_of_governors=governors.length;
		n_of_freqs=frequencies.length;
		for (int i=0; i < n_of_governors; i++){
			governors[i]=governors[i]+":"+Integer.toString(i);
		}
		for (int i=0; i < n_of_freqs; i++){
			frequencies[i]=frequencies[i]+":"+Integer.toString(i);
		}
		FD_gov=new FastDictionary(governors);
		FD_freq=new FastDictionary(frequencies);
		
		governorlist.setEntryValues(FD_gov.getSeconds());
		maxcpulist.setEntryValues(FD_freq.getSeconds());
	}
}
