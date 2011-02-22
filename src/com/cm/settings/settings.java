package com.cm.settings;

import java.io.File;

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

public class settings extends PreferenceActivity { 
	
	public boolean ROOT = false;
	public boolean isFirstTime = false;
	public SharedPreferences prefs;
	public String noiseValue, sensitivityValue;
	EditTextPreference editNoise, editSensitivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.menu.menu); 
		final Context context = getApplicationContext();
		final CheckBoxPreference compcachestart = (CheckBoxPreference)findPreference("cc_run");
		final CheckBoxPreference compcacheauto = (CheckBoxPreference)findPreference("cc_auto");
		final CheckBoxPreference hf = (CheckBoxPreference)findPreference("hf");
		final CheckBoxPreference ads = (CheckBoxPreference)findPreference("ads_filter");
		editNoise = (EditTextPreference)findPreference("noise");
		editSensitivity = (EditTextPreference)findPreference("sensitivity");
		compcachestart.setChecked(Compcache.isCompcacheRunning());
		compcacheauto.setChecked(Compcache.autoStart());
		hf.setChecked(LiquidSettings.vibrStatus());
		ads.setChecked(Adsfilter.isFiltered());
		
		ROOT = LiquidSettings.isRoot();
		noiseValue = editNoise.getText();
		sensitivityValue = editSensitivity.getText();
		
		if(LiquidSettings.getModVersion().contains("CyanogenMod"))  {
        	Log.i("*** DEBUG ***", "you're running CyanogenMod");
        	Toast.makeText(this,"You are using a CM ROM. We suggest you to use the CM settings app for the Compcache",4000).show();
        	compcacheauto.setEnabled(false);
        	compcachestart.setEnabled(false);
        }
		
        prefs = getSharedPreferences("liquid-settings-pref", 0);
        if(prefs.getBoolean("firstrun", true)) {
        	isFirstTime = true;
        	SharedPreferences.Editor edit = prefs.edit();
        	edit.putBoolean("firstrun", false);
        	edit.commit();
        }
        
        updateValues();
        
        
		compcachestart.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				if(!compcachestart.isChecked()) {
					if (Compcache.filesExist() && ROOT) {
						if (Compcache.isCompcacheRunning()) {
							if (Compcache.turnCompcache(false))
								Toast.makeText(context,"Compcache stopped",2000).show();
						    else
								Toast.makeText(context,"Error while stopping compcache",2000).show();
						}
					} else {
						Toast.makeText(context,"Your kernel must support compcache! Can't turn compcache on.",2500);
					}
				} else {
						if (Compcache.turnCompcache(true))
							Toast.makeText(context,"Compcache started", 2000).show();
						else
							Toast.makeText(context,"Error while starting compcache",2000).show();
				}
				return true;
			}
			
		});
		
		
		compcacheauto.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				if (Compcache.filesExist() && ROOT) {
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						if(isFirstTime && checkConfFiles() == false) 
							Toast.makeText(context, "Unable to find configuration files", 2000);
					}
					if(compcacheauto.isChecked()) {
						if (Compcache.autoStart() == false){
								if (Compcache.setAutoStart(true) == false)
									Toast.makeText(context, "Error while writing compcache autostart",2000).show();
								else
									Toast.makeText(context,"File created",2000).show();
						}
					} else {
						if(Compcache.setAutoStart(false) == false)
							Toast.makeText(context,"Error while deleting compcache autostart file",2000).show();
						else
							Toast.makeText(context, "File deleted",2000).show();
					}
				} else {
					Toast.makeText(context, "Your kernel must support compcache! Can't turn compcache on.",2500).show();
				}
				return true;
			}
		});
		
		
		hf.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if(ROOT){
						if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
							LiquidSettings.runRootCommand("echo " + ((hf.isChecked()) ? Strings.getvibr() : Strings.getnovibr()) + " > /system/etc/init.d/06vibrate");
							LiquidSettings.runRootCommand("echo " + ((hf.isChecked()==true) ? "1": "0") +" > /sys/module/avr/parameters/vibr");
							LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
							LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
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
				noiseValue = newValue.toString();
				if (!(Utils.onlyNumbers(noiseValue)))
					return false;
				int noiseValueInt = Integer.parseInt(noiseValue);
				if(noiseValueInt < 20) 
					noiseValue = "20";
				else if(noiseValueInt > 75) 
					noiseValue = "75";
				if (!(Utils.onlyNumbers(sensitivityValue))){
					updateValues();
					return true;
				}
				if(ROOT) {
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						if(isFirstTime && checkConfFiles() == false) {
							Toast.makeText(context, "Error, unable to find configuration files.", 2000).show();
						} else {
							LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue)+" > /system/etc/init.d/06sensitivity");
							LiquidSettings.runRootCommand("echo "+sensitivityValue+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity");
							LiquidSettings.runRootCommand("echo "+noiseValue+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise");
							LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
							LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
							if (LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity"))
								Toast.makeText(context, "Sensitivity set correctly", 1750).show();
							else 
								Toast.makeText(context, "Error, unable to set noise", 2000).show();
						}
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
				sensitivityValue = newValue.toString();
				if (!(Utils.onlyNumbers(sensitivityValue)))
					return false;
				int sensitivityValueInt = Integer.parseInt(sensitivityValue);
				if(sensitivityValueInt < 20)
					sensitivityValue = "20";
				else if (sensitivityValueInt>75)
					sensitivityValue="75";
				if(!(Utils.onlyNumbers(noiseValue))){
					updateValues();
					return true;
				}
				
				if(ROOT) {
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						if(isFirstTime && checkConfFiles() == false) {
							Toast.makeText(context, "Error, unable to find configuration files.", 2000).show();
						} else {
							LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue)+" > /system/etc/init.d/06sensitivity");
							LiquidSettings.runRootCommand("echo "+sensitivityValue+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity");
							LiquidSettings.runRootCommand("echo "+noiseValue+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise");
							LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
							LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
							if (LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity"))
								Toast.makeText(context, "Sensitivity set correctly", 1750).show();
							else 
								Toast.makeText(context, "Error, unable to set sensitivity", 2000).show();
						}
					}
					updateValues();
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 2000).show();
				}
				return true;
			}
		});
	}
	
	
	private void updateValues() {
		editNoise.setSummary("noise is set to " + noiseValue);
		editSensitivity.setSummary("sensitivity is set to " + sensitivityValue);
	}
	
	
	private boolean checkConfFiles() {
    	isFirstTime = false;
    	Toast.makeText(this, "Checking if you can run this application...", 4000).show();
		if((new File("/system/etc/init.d/06vibrate").exists() == false) && (new File("/sys/module/avr/parameters/vibr")).exists() == false) {
			Toast.makeText(this, "Unable to find configuration files", 2000).show();
			return false;
		}
		if (Compcache.filesExist() == false){
			Toast.makeText(this, "You must use a kernel which supports compcache!",2500).show();
			return false;
		}
		return true;
    }
}
