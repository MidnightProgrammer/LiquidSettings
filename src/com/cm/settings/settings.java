package com.cm.settings;

import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    public SharedPreferences prefs;
    public boolean isFirstTime = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
        
        ROOT=LiquidSettings.isRoot();
        Toast isroot = Toast.makeText(this,"Got root permissions",2000);
        isroot.show();
        
        vibrate.setOnClickListener(this);
        setSens.setOnClickListener(this);
        
    }
    
    private boolean checkConfFiles() {
    	Toast firstime = Toast.makeText(this, "Checking if you can run this application...", 4000);
		firstime.show();
		if((new File("/system/etc/init.d/06vibrate").exists() == false) && (new File("/sys/module/avr/parameters/vibr")).exists() == false) {
			Toast nocompatibility = Toast.makeText(this, "Unable to find configuration files", 2000);
			nocompatibility.show();
			return false;
		}
		return true;
    }
    
	public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.vibrate:
				if(ROOT) {
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						if(isFirstTime) {
							if(checkConfFiles() == false)
								break;
						}
				        if(vibrate.isChecked()) {
				        	LiquidSettings.runRootCommand("echo "+LiquidSettings.getvibr ()+" > /system/etc/init.d/06vibrate");
				        	LiquidSettings.runRootCommand("echo 1 > /sys/module/avr/parameters/vibr");
				        	LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				        } else {
				        	LiquidSettings.runRootCommand("echo" + LiquidSettings.getnovibr () +"> /system/etc/init.d/06vibrate");
				        	LiquidSettings.runRootCommand("echo 0 > /sys/module/avr/parameters/vibr");
				        	LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				        }
				        LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
				    } else {
				        	Toast noroot = Toast.makeText(this, "No write", 2000);
				        	noroot.show();
				    }
				} else {
						Toast noroot = Toast.makeText(this, "No root", 2000);
						noroot.show();
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
					if(isFirstTime) {
						if(checkConfFiles() == false)
							break;
					}
					LiquidSettings.runRootCommand("echo "+LiquidSettings.getSens(senss, noiss)+" > /system/etc/init.d/06sensitivity");
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
		}	
	}
	
}
