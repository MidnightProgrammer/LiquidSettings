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
import android.net.Uri;
import android.content.Intent;

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
		final Preference donate = (Preference) findPreference("donate");
		final Preference follow = (Preference) findPreference("follow");
		final Preference gnufabio_twt = (Preference) findPreference("gnufabio_twt");
		final Preference enrix_twt = (Preference) findPreference("enrix_twt");
		
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
        	Toast.makeText(this,"You are using a CM ROM. We suggest you to use the CM settings app for the Compcache",6000).show();
        	compcacheauto.setEnabled(false);
        	compcachestart.setEnabled(false);
        }
		
		if (!Compcache.filesExist()){
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
					if(!(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system"))) {
						return false;
					}
					if (Compcache.setAutoStart(!Compcache.autoStart())){
						Toast.makeText(context, "Compcache autostart set on " + Boolean.toString(Compcache.autoStart()), 2000).show();
						return true;
					}
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
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						if(isFirstTime && checkConfFiles() == false) {
							Toast.makeText(context, "Error, unable to find configuration files.", 2000).show();
						} else {
							LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue)+" > /system/etc/init.d/06sensitivity");
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
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 2000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to sensitivityValue
				sensitivityValue = newValue.toString();
				int sensitivityValueInt = Integer.parseInt(sensitivityValue);
				if(sensitivityValueInt < 20)
					sensitivityValue = "20";
				else if (sensitivityValueInt>75)
					sensitivityValue="75";
				
				if(ROOT) {
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						if(isFirstTime && checkConfFiles() == false) {
							Toast.makeText(context, "Error, unable to find configuration files.", 2000).show();
						} else {
							LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue)+" > /system/etc/init.d/06sensitivity");
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
		
	donate.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference) {
				Uri url = Uri.parse("http://goo.gl/UBBG8"); 
				Intent launchbrowser = new Intent(Intent.ACTION_VIEW,url);
				startActivity(launchbrowser);
				return true;
			}
	});
	
	follow.setOnPreferenceClickListener(new OnPreferenceClickListener(){
		public boolean onPreferenceClick(Preference preference) {
			Uri url = Uri.parse("http://goo.gl/TNHFJ"); 
			Intent launchbrowser = new Intent(Intent.ACTION_VIEW,url);
			startActivity(launchbrowser);
			return true;
		}
});
	
	gnufabio_twt.setOnPreferenceClickListener(new OnPreferenceClickListener(){
		public boolean onPreferenceClick(Preference preference) {
			Uri url = Uri.parse("http://twitter.com/#!/GnuFabio"); 
			Intent launchbrowser = new Intent(Intent.ACTION_VIEW,url);
			startActivity(launchbrowser);
			return true;
		}
});
	
	enrix_twt.setOnPreferenceClickListener(new OnPreferenceClickListener(){
		public boolean onPreferenceClick(Preference preference) {
			Uri url = Uri.parse("http://twitter.com/#!/enrix835"); 
			Intent launchbrowser = new Intent(Intent.ACTION_VIEW,url);
			startActivity(launchbrowser);
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
