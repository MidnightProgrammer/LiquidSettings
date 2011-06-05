package com.liquid.settings.components;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

public class ProxService extends Service {
	
	protected PowerManager powermanager;
	protected PowerManager.WakeLock wakelock; 
	public void onCreate() {
		super.onCreate();
		this.powermanager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		this.wakelock = this.powermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bring phone");
		this.wakelock.acquire();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void terminaservizio(){
		/*try {
			sensorManager.unregisterListener(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			if (this.wakelock.isHeld()){
				this.wakelock.release();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.stopSelf();
	}
}