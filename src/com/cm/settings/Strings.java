package com.cm.settings;

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
	
	public static String getSens(String sens, String noise){
		return String.format("%s\n%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by liquid custom settings",
	            "#",
	            "echo "+sens+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity",
	            "echo "+noise+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise\""
	        );
	}
	
	public static String getCompcacheAutostart(){
		return String.format("%s\n%s\n%s\n%s","\"#!/system/bin/sh",
				"#script created by liquid custom settings",
				"#",
				"compcache start\""
				);
	}
}
