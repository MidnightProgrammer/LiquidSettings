package com.cm.settings;

import java.io.File;
import java.lang.System;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class settings extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    boolean ROOT = false;
    public ToggleButton vibrate;
    public EditText sensitivity;
    public EditText noise;
    public ToggleButton compcache;
    public SharedPreferences prefs;
    public boolean isFirstTime = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(LiquidSettings.getModVersion().equals("CyanogenMod"))  {
        	Log.i("*** DEBUG ***", "you're running CyanogenMod");
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
      
        boolean vibrstatus = LiquidSettings.vibrStatus();
       
        String sensproperty = System.getProperty("liquidsensitivity");
        String noiseproperty = System.getProperty("liquidnoise");
        setContentView(R.layout.main);
        sensitivity = (EditText)findViewById(R.id.sensitivity);
        noise = (EditText)findViewById(R.id.noise);
        if((sensproperty != null) && (noiseproperty != null)) {
    		sensitivity.setText(sensproperty);
    		noise.setText(noiseproperty);
        }
        vibrate = (ToggleButton)findViewById(R.id.vibrate);
        vibrate.setChecked(vibrstatus);
        Button setSens = (Button)findViewById(R.id.setSens);
        compcache= (ToggleButton)findViewById(R.id.setCompcache);
        compcache.setChecked(Compcache.autoStart());
        
        ROOT=LiquidSettings.isRoot();
        Toast.makeText(this,"Got root permissions",2000).show();
        
        vibrate.setOnClickListener(this);
        setSens.setOnClickListener(this);
        compcache.setOnClickListener(this);
        
    }
    
    private boolean checkConfFiles() {
    	Toast.makeText(this, "Checking if you can run this application...", 4000).show();
		if((new File("/system/etc/init.d/06vibrate").exists() == false) && (new File("/sys/module/avr/parameters/vibr")).exists() == false) {
			Toast.makeText(this, "Unable to find configuration files", 2000).show();
			return false;
		}
		return true;
    }
    
	public void onClick(View v) {
		
		switch(v.getId()) {
		
			case R.id.vibrate:
				if(ROOT) {
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						if(isFirstTime && checkConfFiles()==false) 
							break;
				        if(vibrate.isChecked()) {
				        	LiquidSettings.runRootCommand("echo "+Strings.getvibr ()+" > /system/etc/init.d/06vibrate");
				        	LiquidSettings.runRootCommand("echo 1 > /sys/module/avr/parameters/vibr");
				        	LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				        } else {
				        	LiquidSettings.runRootCommand("echo" + Strings.getnovibr () +"> /system/etc/init.d/06vibrate");
				        	LiquidSettings.runRootCommand("echo 0 > /sys/module/avr/parameters/vibr");
				        	LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				        }
				        LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
				    }
				} else {
					Toast.makeText(this, "Sorry, you need to ROOT permissions.", 2000).show();
				}
				break;
		
		case R.id.setSens:
			int senss = Integer.parseInt(sensitivity.getText().toString());
			int noiss = Integer.parseInt(noise.getText().toString());
			if(senss < 25)
				senss = 25;
			else if(senss > 75) 
				senss = 75;
			if(noiss < 25)
				senss = 25;
			else if(noiss > 75)
				senss=75;
			
			if(ROOT) {
				if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
					if(isFirstTime && checkConfFiles() == false) 
						break;
					LiquidSettings.runRootCommand("echo "+Strings.getSens(senss, noiss)+" > /system/etc/init.d/06sensitivity");
					LiquidSettings.runRootCommand("echo "+senss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity");
					LiquidSettings.runRootCommand("echo "+noiss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise");
					LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
					LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
				}
			}
			
			String sen = String.valueOf(senss);
			String noi = String.valueOf(noiss);
			
			sensitivity.setText(sen);
			noise.setText(noi);
			System.setProperty("liquidsensitivity", sen);
			System.setProperty("liquidnoise", noi);
			break;
			
		case R.id.setCompcache:
			if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
				if(isFirstTime && checkConfFiles() == false) 
					break;
			}
			if (Compcache.autoStart() == false){
				if (Compcache.setAutoStart(true) == false){
					Toast.makeText(this, "Error while writing compcache autostart",2000).show();
					break;
				} else{
					Toast.makeText(this,"File created",2000).show();
				}
			} else{
				if(Compcache.setAutoStart(false) == false){
					Toast.makeText(this,"Error while deleting compcache autostart file",2000).show();
					break;
				} else {
					Toast.makeText(this, "File deleted",2000).show();
				}	
			}
			break;
		}
		
	}
	
}
