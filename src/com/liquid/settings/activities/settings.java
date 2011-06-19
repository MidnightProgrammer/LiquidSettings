package com.liquid.settings.activities;

import com.liquid.settings.*;
import com.liquid.settings.components.Eula;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
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
	public boolean isMetal;
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
		isMetal = LSystem.isLiquidMetal(this);
  		
		Eula.show(this);
		addPreferencesFromResource(R.menu.menu); 
		final Context context = getApplicationContext();
		final CheckBoxPreference compcachestart = (CheckBoxPreference)findPreference("cc_run");
		final CheckBoxPreference compcacheauto = (CheckBoxPreference)findPreference("cc_auto");
		final CheckBoxPreference hf = (CheckBoxPreference)findPreference("hf");
		final CheckBoxPreference ads = (CheckBoxPreference)findPreference("ads_filter");
		final EditTextPreference sdcache = (EditTextPreference)findPreference("sdcache");
		final CheckBoxPreference powerled = (CheckBoxPreference) findPreference("powerled");
		final Preference menu_info = findPreference("menu_info");
		
		editNoise = (EditTextPreference)findPreference("noise");
		editSensitivity = (EditTextPreference)findPreference("sensitivity");
		
		if (isMetal){
			editNoise.setEnabled(false);
			editNoise.setSummary("Noise setting not available for Liquid Metal");
			editSensitivity.setSummary("set a value between 15 and 30");
			powerled.setEnabled(false);
		}
		if (!LSystem.hapticAvailable())
			hf.setEnabled(false);
		else
			hf.setChecked(LSystem.vibrStatus());
		
		compcachestart.setChecked(Compcache.isCompcacheRunning());
		compcacheauto.setChecked(Compcache.autoStart());
		ads.setChecked(Adsfilter.isFiltered());
		
		if (!SdCache.isCachePathAvailable())
			sdcache.setEnabled(false);
		if ((SDCacheSize=SdCache.getSdCacheSize()) >= 128){
			sdcache.setText(Integer.toString(SDCacheSize));
		}
		
		ROOT = LiquidSettings.isRoot();
		noiseValue = editNoise.getText();
		sensitivityValue = editSensitivity.getText();

		if(LSystem.getModVersion().contains("CyanogenMod"))  {
        	Log.i("*** DEBUG ***", "You're running CyanogenMod");
        	Toast.makeText(this,"You are using a CM ROM. We suggest you to use the CM settings app for the Compcache",6000).show();
        	compcacheauto.setEnabled(false);
        	compcachestart.setEnabled(false);
        }
		
		if (!Compcache.filesExist()){
			compcacheauto.setEnabled(false);
			compcachestart.setEnabled(false);
		}
		
		
        updateValues();
        
		compcachestart.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				if (ROOT){
					if (Compcache.turnCompcache(compcachestart.isChecked())){
						Toast.makeText(context, "Compcache " + ((compcachestart.isChecked()) ? "started" : "stopped"), 2000).show();
						return true;
					} else{
						Toast.makeText(context, "Error while turning on/off compcache", 2000).show();
						return false;
					} 
				}else {
						Toast.makeText(context, "Sorry, you need ROOT permissions", 2000);
						return false;
				}	
			}
		});
		
		compcacheauto.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				if (ROOT) {
					if(!(LSystem.RemountRW())) {
						return false;
					}
					if (Compcache.setAutoStart(!Compcache.autoStart())){
						LSystem.RemountROnly();
						Toast.makeText(context, "Compcache autostart set on " + Boolean.toString(Compcache.autoStart()), 2000).show();
						return true;
					}
					LSystem.RemountROnly();
					return false;
				} else{
					Toast.makeText(context, "Sorry, you need ROOT permissions",	2000).show();
					return false;
				}
			}
		});
		
		
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
		
		
		ads.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if (ROOT) {
					if(!ads.isChecked()) {
						if (Adsfilter.isFiltered()){
							if (Adsfilter.restoreHostsFile()) {
								Toast.makeText(context,"Hosts file restored",2000).show();
							} else {
								Toast.makeText(context,"Error restoring hosts file",2000).show();
								ads.setChecked(false);
							}
						}
					} else {
						if (Adsfilter.apply(context)) {
							Toast.makeText(context,"Hosts file patched",2000).show();
						} else {
							Toast.makeText(context,"Error patching hosts file",2000).show();
							ads.setChecked(false);
						}
					}
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 2000).show();
					ads.setChecked(false);
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
							LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, isMetal)+" > /system/etc/init.d/06sensitivity");
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
				if(sensitivityValueInt < (isMetal ? 15 : 20))
					sensitivityValue = (isMetal ? "15" : "20");
				else if (sensitivityValueInt>(isMetal ? 30 : 75))
					sensitivityValue=(isMetal ? "30" : "75");
				
				if(ROOT) {
					if(ROOT && LSystem.RemountRW()) {
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, isMetal)+" > /system/etc/init.d/06sensitivity");
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
				if (powerled.isChecked()) {
						LiquidSettings.runRootCommand("echo '0' > " + "/sys/class/leds2/power");
						LiquidSettings.runRootCommand("chmod 000 "  + "/sys/class/leds2/power");
					}else{		
						BatteryLED.getBatteryLevel();
						LiquidSettings.runRootCommand("/sys/class/leds2/power");
					}
				BatteryLED.setdisable(powerled.isChecked());
				return true;
			}
		});
	}
	

	private void updateValues() {
		editNoise.setSummary("noise is set to " + noiseValue);
		if (!isMetal)
			editSensitivity.setSummary("sensitivity is set to " + sensitivityValue);
	}
	
}
