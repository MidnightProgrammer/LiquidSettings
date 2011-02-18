package com.cm.settings;

import java.io.File;

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
    public ToggleButton runcc;
    public ToggleButton ads;
    public SharedPreferences prefs;
    public boolean isFirstTime = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
      
        boolean vibrstatus = LiquidSettings.vibrStatus();
        setContentView(R.layout.main);
        
        sensitivity = (EditText)findViewById(R.id.sensitivity);
        noise = (EditText)findViewById(R.id.noise);
        vibrate = (ToggleButton)findViewById(R.id.vibrate);
        vibrate.setChecked(vibrstatus);
        Button setSens = (Button)findViewById(R.id.setSens);
        compcache= (ToggleButton)findViewById(R.id.setCompcache);
        compcache.setChecked(Compcache.autoStart());
        runcc= (ToggleButton) findViewById(R.id.ccrunning);
        runcc.setChecked(Compcache.isCompcacheRunning());
        ads= (ToggleButton) findViewById(R.id.ads);
        ads.setChecked(Adsfilter.isFiltered());
        ROOT=LiquidSettings.isRoot();
        //
        sensitivity.setText(prefs.getString("sens", "25"));
        noise.setText(prefs.getString("noise", "50"));
        //
        vibrate.setOnClickListener(this);
        setSens.setOnClickListener(this);
        compcache.setOnClickListener(this);
        runcc.setOnClickListener(this);
        ads.setOnClickListener(this);
    }
    
    private boolean checkConfFiles() {
    	isFirstTime = false;
    	Toast.makeText(this, "Checking if you can run this application...", 4000).show();
		if((new File("/system/etc/init.d/06vibrate").exists() == false) && (new File("/sys/module/avr/parameters/vibr")).exists() == false) {
			Toast.makeText(this, "Unable to find configuration files", 2000).show();
			return false;
		}
		if (Compcache.filesExist()==false){
			Toast.makeText(this, "You must use a kernel which supports compcache!",2500).show();
			return false;
		}
		return true;
    }
    
	public void onClick(View v) {
		switch(v.getId()) {
		
			case R.id.vibrate: //If button pressed is 'Haptic FeedBack'
				if(ROOT) {
					if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
						Boolean hapticon = LiquidSettings.vibrStatus();
						LiquidSettings.runRootCommand("echo "+((hapticon==true)? Strings.getnovibr() : Strings.getvibr())+" > /system/etc/init.d/06vibrate");
				        LiquidSettings.runRootCommand("echo " +((hapticon==true) ? "0" : "1") + " > /sys/module/avr/parameters/vibr");
				        LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
				        LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
				        Toast.makeText(this, "Haptic set on " + Boolean.toString(hapticon), 1500).show();
					}
				} else {
					Toast.makeText(this, "Sorry, you need to ROOT permissions.", 2000).show();
				}
				break;
		
		case R.id.setSens: //If button pressed is 'Set sensitivity'
			int senss = Integer.parseInt(sensitivity.getText().toString());
			int noiss = Integer.parseInt(noise.getText().toString());
			if(senss < 20)
				senss = 20;
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
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("sens", Integer.toString(senss));
					editor.putString("noise", Integer.toString(noiss));
					editor.commit();
					Toast.makeText(this, "Sensitivity set correctly", 1750).show();
				}
			}
			sensitivity.setText(Integer.toString(senss));
			noise.setText(Integer.toString(noiss));
			break;
			
		case R.id.setCompcache: //If button pressed is 'Compcache Autostart'
			if (Compcache.filesExist() && ROOT){
				if(LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system")) {
					if(isFirstTime && checkConfFiles() == false) 
						break;
				}
				if (Compcache.autoStart() == false){
						if (Compcache.setAutoStart(true) == false)
							Toast.makeText(this, "Error while writing compcache autostart",2000).show();
						else
							Toast.makeText(this,"File created",2000).show();
				} else{
					if(Compcache.setAutoStart(false) == false)
						Toast.makeText(this,"Error while deleting compcache autostart file",2000).show();
					else
						Toast.makeText(this, "File deleted",2000).show();
				}
			} else {
				Toast.makeText(this,"Your kernel must support compcache! Can't turn compcache on.",2500).show();
			}
			break;
		
		case R.id.ccrunning: //If button pressed 'Compcache start/stop'
			if (Compcache.filesExist() && ROOT){
				if (Compcache.isCompcacheRunning()){
					if (Compcache.turnCompcache(false))
						Toast.makeText(this,"Compcache stopped",2000).show();
					else
						Toast.makeText(this,"Error while stopping compcache",2000).show();
				}else{
					if (Compcache.turnCompcache(true))
						Toast.makeText(this,"Compcache started", 2000).show();
					else
						Toast.makeText(this,"Error while starting compcache",2000).show();
				}
			} else {
				Toast.makeText(this,"Your kernel must support compcache! Can't turn compcache on.",2500);
			}
			break;
		
		case R.id.ads: //If button pressed is 'Ads filter'
			if (ROOT){
				if (Adsfilter.isFiltered()){
					if (Adsfilter.restoreHostsFile())
						Toast.makeText(this,"Hosts file restored",2000).show();
					else
						Toast.makeText(this,"Error restoring hosts file",2000).show();
				} else{
					if (Adsfilter.apply(this))
						Toast.makeText(this,"Hosts file patched",2000).show();
					else
						Toast.makeText(this,"Error patching hosts file",2000).show();
				}
			}
			break;	
		
		} //END SWITCH STATEMENT
	
	} //END OnClick function
}
