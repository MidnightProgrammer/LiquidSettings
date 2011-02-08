package com.liquid.custom;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.util.Properties;

import android.app.Activity;
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
    boolean ROOT = false, WRITE = false;
    final Runtime runtime = Runtime.getRuntime();
    public ToggleButton vibrate;
    public EditText sensitivity;
    public EditText noise;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        boolean vibrstatus = vibrStatus();

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



        ROOT = runRootCommand("echo root");
        vibrate.setOnClickListener(this);
        setSens.setOnClickListener(sensListener);
        
    }
    // Vibrate listener
	@Override
	public void onClick(View v) {
		if(ROOT) {
			WRITE = runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system");
			if(WRITE) {
				if(vibrate.isChecked()) {
					runRootCommand("echo "+vibr+" > /system/etc/init.d/06vibrate");
					runRootCommand("echo 1 > /sys/module/avr/parameters/vibr");
					runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				}
				else {
					runRootCommand("echo "+novibr+" > /system/etc/init.d/06vibrate");
					runRootCommand("echo 0 > /sys/module/avr/parameters/vibr");
					runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				}
			runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
			WRITE = false;
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
    public static boolean runRootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
                try {
                        process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(command+"\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
                } catch (Exception e) {
                        Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
                        return false;
                }
                finally {
                        try {
                                if (os != null) {
                                        os.close();
                                }
                                process.destroy();
                        } catch (Exception e) {
                                // nothing
                        }
                }
                return true;
    }
    //check current vibration value
    public static boolean vibrStatus() {
    	String value;
    	try {
        	FileReader input = new FileReader("/sys/module/avr/parameters/vibr");
        	BufferedReader reader = new BufferedReader(input);
        	value = reader.readLine();
        	reader.close();
        	input.close();
    	} catch (Exception e) {
    		Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
    		return false;
    	}
        if(value == null) {
        	return false;
        }
        else if(value.equalsIgnoreCase("1")) {
        	return true;
        }
        else if(value.equalsIgnoreCase("0")) {          	
        	return false;
        }
        return false;

    }
	
	//sensitivity listener
	private OnClickListener sensListener = new OnClickListener() {
	    public void onClick(View v) {
			int senss = Integer.parseInt(sensitivity.getText().toString());
			int noiss = Integer.parseInt(noise.getText().toString());
			if(senss<25) {
				senss=25;
			}
			if(senss>75) {
				senss=75;
			}
			if(noiss<25) {
				senss=25;
			}
			if(noiss>75) {
				senss=75;
			}
			
			String sens = String.format("%s\n%s\n%s\n%s\n%s", 
		            "\"#!/system/bin/sh",
		            "#script created by liquid custom settings",
		            "#",
		            "echo "+senss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity",
		            "echo "+noiss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise\""
		        );
			if(ROOT) {
				WRITE = runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system");
				if(WRITE) {
					runRootCommand("echo "+sens+" > /system/etc/init.d/06sensitivity");
					runRootCommand("echo "+senss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity");
					runRootCommand("echo "+noiss+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise");
					runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
					runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
					WRITE = false;
				}
				else {
					//no write
				}
			}
			else {
				//no root
			}
			String sen = String.valueOf(senss);
			String noi = String.valueOf(noiss);
		sensitivity.setText(sen);
		noise.setText(noi);
		System.setProperty("liquidsensitivity", sen);
		System.setProperty("liquidnoise", noi);
	    }
	};

	
	
	// startup scripts
	public String novibr = String.format("%s\n%s\n%s\n%s", 
            "\"#!/system/bin/sh",
            "#script created by liquid custom settings",
            "#",
            "echo 0 > /sys/module/avr/parameters/vibr\""
        );
	public String vibr = String.format("%s\n%s\n%s\n%s", 
            "\"#!/system/bin/sh",
            "#script created by liquid custom settings",
            "#",
            "echo 1 > /sys/module/avr/parameters/vibr\""
        );

	
}