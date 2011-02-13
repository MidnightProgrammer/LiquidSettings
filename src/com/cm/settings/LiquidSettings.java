package com.cm.settings;

import java.io.*;
import android.util.Log;

public class LiquidSettings {
	
	public static boolean isRoot(){
		return runRootCommand("echo $?");
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
	                             if (os != null) os.close();
	                             process.destroy();
	                        } catch (Exception e) {}
	                }
	                return true;
	    }
	
	public static boolean vibrStatus() {
		//Check if the haptic feedback is on
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
        if(value == null || value.equalsIgnoreCase("0")) {
        	return false;
        }
        else if(value.equalsIgnoreCase("1")) {
        	return true;
        }
        return false;
    }
	
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
	
	public static String getSens(int sens, int noise){
		return String.format("%s\n%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by liquid custom settings",
	            "#",
	            "echo "+sens+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity",
	            "echo "+noise+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise\""
	        );
	}
}