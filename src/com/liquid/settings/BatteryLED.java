package com.liquid.settings;

import java.io.BufferedReader;
import java.io.FileReader;

import android.util.Log;


public class BatteryLED {
	public static boolean isCharging(boolean isMetal){
		String ac_value,usb_value;
    	try {
        	FileReader input = new FileReader(isMetal ? "/sys/devices/platform/msm-battery/power_supply/ac/online":"/sys/class/power_supply/ac/online");
        	BufferedReader reader = new BufferedReader(input);
        	ac_value = reader.readLine();
        	reader.close();
        	input.close();
        	input=new FileReader(isMetal ? "/sys/devices/platform/msm-battery/power_supply/usb/online" : "/sys/class/power_supply/usb/online");
        	reader=new BufferedReader(input);
        	usb_value=reader.readLine();
        	reader.close();
        	input.close();
    	} catch (Exception e) {
    		Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
    		return false;
    	}
    	if ( ac_value.equalsIgnoreCase("1") || usb_value.equalsIgnoreCase("1"))
    		return true;
    	return false;
	}
	
	public static int getBatteryLevel(boolean isMetal){
		String battery_level;
		LiquidSettings.runRootCommand("export BATTERY_LEVEL=`cat " + (isMetal ? "/dump/sys/devices/platform/msm-battery/power_supply/battery/uevent" : "/sys/class/power_supply/battery/uevent") + "| grep POWER_SUPPLY_CAPACITY`; echo ${BATTERY_LEVEL#POWER_SUPPLY_CAPACITY=} > /sdcard/.battery_level");
		try{
			FileReader input = new FileReader("/sdcard/.battery_level");
			BufferedReader reader = new BufferedReader(input);
    		battery_level = reader.readLine();
    		reader.close();
    		input.close();
    		Log.d("LS-APP","Battery level detected: " + battery_level);
    		return Integer.parseInt(battery_level);
		} catch (Exception e){
			Log.e("LS-APP",e.getMessage());
		}
		return 0;
	}
	
	public static boolean setdisable(boolean opt,boolean isMetal){
		try{
			if(opt)
				return (LSystem.RemountRW() & LiquidSettings.runRootCommand("echo " + Strings.batteryleddisable() + " > /etc/init.d/10batteryled") && LiquidSettings.runRootCommand("chmod +x /etc/init.d/10batteryled") && LSystem.RemountROnly());
			else{
				int batteryLevel=getBatteryLevel(isMetal);
				if (isCharging(isMetal)){
					LiquidSettings.runRootCommand("echo '3' > " + (isMetal ? "/sys/class/leds/power" : "/sys/class/leds2/power"));
				} else {
					if (batteryLevel <= 10){
						LiquidSettings.runRootCommand("echo '2' > " + (isMetal ? "/sys/class/leds/power" : "/sys/class/leds2/power"));
					} else if (batteryLevel > 10 && batteryLevel <=20){
						LiquidSettings.runRootCommand("echo '1' > " + (isMetal ? "/sys/class/leds/power" : "/sys/class/leds2/power"));
					}
				}
				return (LSystem.RemountRW() && LiquidSettings.runRootCommand("rm /etc/init.d/10batteryled") && LSystem.RemountROnly());
			}
		} catch (Exception e){
			Log.e("LS-APP", e.getMessage());
		}
		return false;
	}
	
}
