package com.cm.settings;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

public class settings extends PreferenceActivity { 
	
	public boolean ROOT = false;
	public boolean isFirstTime = false;
	public SharedPreferences prefs;
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.menu.menu); 
		final Context context = getApplicationContext();
		final CheckBoxPreference compcachestart = (CheckBoxPreference)findPreference("cc_run");
		final CheckBoxPreference compcacheauto = (CheckBoxPreference)findPreference("cc_auto");
		final CheckBoxPreference hf = (CheckBoxPreference)findPreference("hf");
		final CheckBoxPreference ads = (CheckBoxPreference)findPreference("ads_filter");
		compcachestart.setChecked(Compcache.isCompcacheRunning());
		compcacheauto.setChecked(Compcache.autoStart());
		hf.setChecked(LiquidSettings.vibrStatus());
		ads.setChecked(Adsfilter.isFiltered());
		ROOT = LiquidSettings.isRoot();
		
		if(LiquidSettings.getModVersion().equals("CyanogenMod"))  {
        	Log.i("*** DEBUG ***", "you're running CyanogenMod");
        	Toast.makeText(this,"You are using a CM ROM. We suggest you to use the CM settings app for the Compcache",4000).show();
        }
        
        /* store value `firstrun` to check if the app's running for
         * the first time.
         */
		
        prefs = getSharedPreferences("liquid-settings-pref", 0);
        if(prefs.getBoolean("firstrun", true)) {
        	isFirstTime = true;
        	SharedPreferences.Editor edit = prefs.edit();
        	edit.putBoolean("firstrun", false);
        	edit.commit();
        }
		
		
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
				if(ROOT) {
					if(hf.isChecked()) {
						if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
							boolean hapticon = LiquidSettings.vibrStatus();
							LiquidSettings.runRootCommand("echo "+((hapticon==true)? Strings.getnovibr() : Strings.getvibr())+" > /system/etc/init.d/06vibrate");
							LiquidSettings.runRootCommand("echo " +((hapticon==true) ? "0" : "1") + " > /sys/module/avr/parameters/vibr");
							LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
							LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
							Toast.makeText(context, "Haptic set on " + Boolean.toString(!hapticon), 1500).show();
						} else {
							Toast.makeText(context, "Error: unable to mount partition", 2000);
							hf.setChecked(false);
						}
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
