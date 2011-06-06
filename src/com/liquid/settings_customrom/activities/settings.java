package com.liquid.settings_customrom.activities;

import com.liquid.settings_customrom.*;
import com.liquid.settings_customrom.components.BottomLED_service;
import com.liquid.settings_customrom.components.Eula;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class settings extends PreferenceActivity { 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.inflatedmenu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_help:
			startActivity(new Intent (Intent.ACTION_VIEW).setClassName(this, InfoPreferenceActivity.class.getName()));
			return true;
	    case R.id.menu_close:
	        this.finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public boolean ROOT = false;
	public boolean isFirstTime = false;
	public SharedPreferences prefs;
	public String noiseValue, sensitivityValue;
	public int SDCacheSize;
	EditTextPreference editNoise, editSensitivity;
    
	@Override
	public void onCreate(Bundle savedInstanceState) { 

		super.onCreate(savedInstanceState);
		
		
		if (!LSystem.checkInitFolder()){
			Toast.makeText(this, "Can't make init.d folder, your system must be rooted", 2000);
			this.finish(); //Exit app
		}		
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if(prefs.getBoolean("bottomled", false)){                
        	Intent bottomledservice = new Intent(this, BottomLED_service.class);
        	this.startService(bottomledservice);
    	}
  		
		Eula.show(this);
		addPreferencesFromResource(R.menu.menu); 
		final Context context = getApplicationContext();
		final CheckBoxPreference hf = (CheckBoxPreference)findPreference("hf");
		final EditTextPreference sdcache = (EditTextPreference)findPreference("sdcache");
		final CheckBoxPreference powerled = (CheckBoxPreference)findPreference("powerled");
		final CheckBoxPreference bottomled = (CheckBoxPreference)findPreference("bottomled");
		final Preference menu_info = findPreference("menu_info");
		
		editNoise = (EditTextPreference)findPreference("noise");
		editSensitivity = (EditTextPreference)findPreference("sensitivity");
		
		if (!LSystem.hapticAvailable())
			hf.setEnabled(false);
		else
			hf.setChecked(LSystem.vibrStatus());
		
		
		if (!SdCache.isCachePathAvailable())
			sdcache.setEnabled(false);
		if ((SDCacheSize=SdCache.getSdCacheSize()) >= 128){
			sdcache.setText(Integer.toString(SDCacheSize));
		}
		
		ROOT = LiquidSettings.isRoot();
		noiseValue = editNoise.getText();
		sensitivityValue = editSensitivity.getText();
        
        updateValues();

		hf.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if(ROOT){
						if(LSystem.RemountRW()) {
							LiquidSettings.runRootCommand("echo " + ((hf.isChecked()) ? Strings.getvibr() : Strings.getnovibr()) + " > /system/etc/init.d/06vibrate");
							LiquidSettings.runRootCommand("echo " + ((hf.isChecked()==true) ? "1": "0") +" > /sys/module/avr/parameters/vibr");
							LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
							LSystem.RemountROnly();
							Toast.makeText(context, "Haptic set on " + Boolean.toString(hf.isChecked()), 1500).show();
						} else {
							Toast.makeText(context, "Error: unable to mount partition", 2000);
							hf.setChecked(false);
						}
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 2000).show();
					hf.setChecked(false);
				}
				return true;
			}
		
		});
		
		editNoise.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 2000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to noiseValue
				noiseValue = newValue.toString();
				int noiseValueInt = Integer.parseInt(noiseValue);
				if(noiseValueInt < 20) 
					noiseValue = "20";
				else if(noiseValueInt > 75) 
					noiseValue = "75";

				if(ROOT) {
					if(ROOT && LSystem.RemountRW()) {
							LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue)+" > /system/etc/init.d/06sensitivity");
							LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
							LSystem.RemountROnly();
							if (LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity"))
								Toast.makeText(context, "Sensitivity set correctly", 1750).show();
							else 
								Toast.makeText(context, "Error, unable to set noise", 2000).show();
						}
					updateValues();
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 2000).show();
				}
				return true;
			}
		});
		
		
		editSensitivity.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 2000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to sensitivityValue
				sensitivityValue = newValue.toString();
				int sensitivityValueInt = Integer.parseInt(sensitivityValue);
				if(sensitivityValueInt < (20))
					sensitivityValue = ("20");
				else if (sensitivityValueInt>(75))
					sensitivityValue=("75");
				
				if(ROOT) {
					if(ROOT && LSystem.RemountRW()) {
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue)+" > /system/etc/init.d/06sensitivity");
						LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
						LSystem.RemountROnly();
						if (LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity"))
							Toast.makeText(context, "Sensitivity set correctly", 1750).show();
						else 
							Toast.makeText(context, "Error, unable to set sensitivity", 2000).show();
					}
					updateValues();
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 2000).show();
				}
				return true;
			}
		});
		
		menu_info.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent myintent = new Intent (Intent.ACTION_VIEW);
				myintent.setClassName(context, InfoPreferenceActivity.class.getName());
				startActivity(myintent);
				return true;
			}
		});
		
		
		sdcache.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 2000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to sensitivityValue
				String newValueString = newValue.toString();
				int newValueInt = Integer.parseInt(newValueString);
				if(newValueInt < 128)
					newValueInt = 128;
				else if (newValueInt > 4096)
					newValueInt = 4096;
				if (ROOT){
					if (SdCache.setSDCache(newValueInt)){
						Toast.makeText(context, "SD cache size set to " + newValueInt, 1500).show();
						return true;
					}else{
						Toast.makeText(context, "Error while setting SD Cache", 1500).show();
						return false;
					}
				} else 
					Toast.makeText(context, "Sorry you need root permissions", 2000);
				return false;
			}
		});
		
		powerled.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				if (ROOT){
					if (powerled.isChecked()) {						
						LiquidSettings.runRootCommand("echo '0' > /sys/class/leds2/power");
						LiquidSettings.runRootCommand("chmod 000 /sys/class/leds2/power");
					}else{
						LiquidSettings.runRootCommand("chmod 222 /sys/class/leds2/power");
					}
					if (BatteryLED.setdisable(powerled.isChecked())){						
						return true;
					} else{
						Toast.makeText(context, "Error while set Power LED disable", 2000).show();
						return false;
					}
				}else {
						Toast.makeText(context, "Sorry, you need ROOT permissions", 2000);
						return false;
				}	
			}
		});
		
		bottomled.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
			if (bottomled.isChecked()) {						
	        	Intent bottomledservice = new Intent(getBaseContext(), BottomLED_service.class);
	        	getBaseContext().startService(bottomledservice);
				}
			return true;
			}
		});
}

	
	
	private void updateValues() {
		editNoise.setSummary("noise is set to " + noiseValue);
	}
	
}
