package com.cm.settings;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

public class LiquidSettings {

	public static String getModVersion() {
		String mod = "ro.modversion";
		String CMversion;
		BufferedReader input;
		try {
            Process get = Runtime.getRuntime().exec("getprop " + mod);
            input = new BufferedReader(new InputStreamReader(get.getInputStream()), 1024);
            CMversion = input.readLine();
            input.close();
		} catch (IOException ex) {
            Log.e("*** ERROR ***", "Unable to read mod version.");
            return null;
		}
		return CMversion;
	}
	
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

}