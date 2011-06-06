package com.liquid.settings_customrom.components;

import com.liquid.settings_customrom.LiquidSettings;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.telephony.TelephonyManager;

public class SmsLED_service extends Service {
	SmsManager sms;
	private final Handler mHandler = new Handler();
	private final Handler mHandler2 = new Handler();
	private SharedPreferences prefs;
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {        	
        	if(prefs.getBoolean("fixled", false)){                
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {            	
            	//LiquidSettings.runRootCommand("tail /data/system/mail_led > /sys/class/leds2/bottom");
     			Cursor cursor = getContentResolver().query(Uri.parse("content://sms/")
                        , new String[]{"read",}
                        , null //selection
                        , null //selectionArgs
                        , "read ASC"); //sortOrder

               		if (cursor != null && cursor.moveToFirst()) {
               		String read = cursor.getString(cursor.getColumnIndex("read"));
               			if (read.equals("0")){
               				LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/mail");
               				if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/bottom");
               			}else{
               				LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/mail");
               				if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/bottom");
               			}
               		}
               		cursor.close();
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            	LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/mail");
            	if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/bottom");
            }
        	}else{
        		terminaservizio();
        		unregisterReceiver(this);
        	}
            
        }
    };
    
    BroadcastReceiver mIntentReceiver2 = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {     
        	Log.d("=====LS=====","OKPREFS");
        	if(prefs.getBoolean("fixled", false)){                
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {            	
            	//LiquidSettings.runRootCommand("tail /data/system/mail_led > /sys/class/leds2/bottom");
            	
     			Cursor cursor = getContentResolver().query(Uri.parse("content://sms/")
                        , new String[]{"read",}
                        , null //selection
                        , null //selectionArgs
                        , "read ASC"); //sortOrder

               		if (cursor != null && cursor.moveToFirst()) {
               		String read = cursor.getString(cursor.getColumnIndex("read"));
               			if (read.equals("0")){
               				LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/mail");
               				if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/bottom");
               			}else{
               				LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/mail");
               				if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/bottom");
               			}
               		}
               		cursor.close();
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
		//LiquidSettings.runRootCommand("echo 0 > /data/system/mail_led && chmod 777 /data/system/mail_led");
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        this.registerReceiver(mIntentReceiver, filter, null, mHandler);
        /*IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.registerReceiver(mIntentReceiver2, filter2, null, mHandler2);*/  
	}
	public void terminaservizio(){
		LiquidSettings.runRootCommand("echo '0' > /sys/class/leds2/bottom");
		this.stopSelf();
	}
	
}