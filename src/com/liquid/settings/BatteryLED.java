package com.liquid.settings;

import android.util.Log;

public class BatteryLED {

	public static boolean setdisable(boolean opt){
		try{
			if(opt)
				return (LSystem.RemountRW() & LiquidSettings.runRootCommand("echo " + Strings.batteryleddisable() + " > /etc/init.d/10batteryled") && LiquidSettings.runRootCommand("chmod +x /etc/init.d/10batteryled") && LSystem.RemountROnly());
			else
				return (LSystem.RemountRW() && LiquidSettings.runRootCommand("rm /etc/init.d/10batteryled") && LiquidSettings.runRootCommand("echo '1' > /sys/class/leds2/power") && LiquidSettings.runRootCommand("chmod 777 /sys/class/leds2/power") && LSystem.RemountROnly());
		} catch (Exception e){
			Log.e("LS-APP", e.getMessage());
		}
		return false;
	}
	
}
