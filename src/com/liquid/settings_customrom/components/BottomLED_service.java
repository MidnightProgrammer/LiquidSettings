package com.liquid.settings_customrom.components;

import com.liquid.settings_customrom.LiquidSettings;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

public class BottomLED_service extends Service {
	SmsManager sms;
	private final Handler mHandler = new Handler();
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        	if(prefs.getBoolean("bottomled", false)){                
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {            	
            	LiquidSettings.runRootCommand("tail /data/system/mail_led > /sys/class/leds2/bottom");
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            	LiquidSettings.runRootCommand("echo '0' > /sys/class/leds2/bottom");
            }
        	}else{
        		terminaservizio();
        		unregisterReceiver(this);
        	}
            
        }
    };
    
	@Override
	public IBinder onBind(Intent intent) {
		

		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate() {
		super.onCreate();
		LiquidSettings.runRootCommand("echo 0 > mail_led && chmod 777 mail_led");
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        this.registerReceiver(mIntentReceiver, filter, null, mHandler);        
	}
	public void terminaservizio(){
		LiquidSettings.runRootCommand("echo '0' > /sys/class/leds2/bottom");
		this.stopSelf();
	}
	
}