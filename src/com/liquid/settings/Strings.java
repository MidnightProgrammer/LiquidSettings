package com.liquid.settings;

import android.util.Log;

public class Strings {
	public static String getnovibr(){
		return String.format("%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by liquid custom settings",
	            "#",
	            "echo 0 > /sys/module/avr/parameters/vibr\""
	        );
	}
	
	public static String getvibr(){
		return String.format("%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by liquid custom settings",
	            "#",
	            "echo 1 > /sys/module/avr/parameters/vibr\""
	        );	
		}
	
	public static String getSens(String sens, String noise, boolean metal){
		return String.format("%s\n%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by Liquid Settings App",
	            "#",
	            metal ? ("echo "+sens+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity") : ("echo "+sens+" > /sys/devices/platform/i2c-adapter/i2c-0/0-004d/sensitivity"),
	            metal ? ("echo "+noise+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise\"") : "# There is no noise for Liquid MT\""
	        );
	}
	
	public static String getCompcacheAutostart(){
		return String.format("%s\n%s\n%s\n%s","\"#!/system/bin/sh",
				"#script created by liquid custom settings",
				"#",
				"compcache start\""
				);
	}
	
	public static boolean onlyNumber(String str){
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException excp){
			Log.e("Liquid-settings", excp.getMessage());
			return false;
		}
	}
}
