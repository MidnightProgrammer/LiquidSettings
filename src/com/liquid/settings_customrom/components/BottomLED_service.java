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

public class BottomLED_service extends Service {
	SmsManager sms;
	private final Handler mHandler = new Handler();
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        	if(prefs.getBoolean("bottomled", false)){                
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {            	
            	LiquidSettings.runRootCommand("tail /data/system/mail_led > /sys/class/leds2/bottom");
            	
     			/*Cursor cursor = getContentResolver().query(Uri.parse("content://sms/")
                        , new String[]{"read",}
                        , null //selection
                        , null //selectionArgs
                        , "read ASC"); //sortOrder

               		if (cursor != null && cursor.moveToFirst()) {
               		String read = cursor.getString(cursor.getColumnIndex("read"));
               			if (read.equals("0")){
               				LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/bottom");
               				//LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/mail");
               			}else{
               				LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/bottom");
               				//LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/mail");
               			}
               		}
               		cursor.close();*/
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
		LiquidSettings.runRootCommand("echo 0 > /data/system/mail_led && chmod 777 /data/system/mail_led");
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