package com.cm.settings;

import android.app.Activity;
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
    final Runtime runtime = Runtime.getRuntime();
    public ToggleButton vibrate;
    public EditText sensitivity;
    public EditText noise;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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

        ROOT = LiquidSettings.isRoot();
        vibrate.setOnClickListener(this);
        setSens.setOnClickListener(sensListener);
        
    }
    
    // Vibrate listener
	public void onClick(View v) {
		if(ROOT) {
			if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
				if(vibrate.isChecked()) {
					LiquidSettings.runRootCommand("echo "+LiquidSettings.getvibr ()+" > /system/etc/init.d/06vibrate");
					LiquidSettings.runRootCommand("echo 1 > /sys/module/avr/parameters/vibr");
					LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				}
				else {
					LiquidSettings.runRootCommand("echo" + LiquidSettings.getnovibr () +"> /system/etc/init.d/06vibrate");
					LiquidSettings.runRootCommand("echo 0 > /sys/module/avr/parameters/vibr");
					LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				}
			LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
			}
			else {
	        	Toast noroot = Toast.makeText(this, "No write", 2000);
	        	noroot.show();
			}
		}
		else {
        	Toast noroot = Toast.makeText(this, "No root", 2000);
        	noroot.show();
		}
	}
	
	//sensitivity listener
	private OnClickListener sensListener = new OnClickListener() {
	    public void onClick(View v) {
			int senss = Integer.parseInt(sensitivity.getText().toString());
			int noiss = Integer.parseInt(noise.getText().toString());
			if(senss<25)
				senss=25;
			else if(senss>75) 
				senss=75;
			if(noiss<25)
				senss=25;
			if(noiss>75)
				senss=75;
			
			String sens = String.format("%s\n%s\n%s\n%s\n%s", 
		            "\"#!/system/bin/sh",
		            "#script created by liquid custom settings",
		            "#",
		            "echo "+senss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity",
		            "echo "+noiss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise\""
		        );
			if(ROOT) {
				if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
					LiquidSettings.runRootCommand("echo "+sens+" > /system/etc/init.d/06sensitivity");
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
	    }
	};
	
}