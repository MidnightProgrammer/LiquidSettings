package com.liquid.settings;

import java.io.BufferedReader;
import java.io.FileReader;

import android.util.Log;

public class OverClockStrings {
	
	public static String [] getAvailableGovernors (){
		String values;
		try {
        	FileReader input = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
        	BufferedReader reader = new BufferedReader(input);
        	values = reader.readLine();
        	values.replaceAll("\\n", "");
        	values.replaceAll("\n", "");
        	Log.d("LS-APP","Governors: " + values);
        	reader.close();
        	input.close();
        	return values.split(" ");
    	} catch (Exception e) {
    		Log.e("LS-APP", "Unexpected exception - Here is what I know: "+e.getMessage());
    		return null;
    	}
	}
	
	public static String [] getAvailableFrequencies (){
		String values;
		try {
        	FileReader input = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
        	BufferedReader reader = new BufferedReader(input);
        	values = reader.readLine();
        	reader.close();
        	input.close();
        	return values.split(" ");
    	} catch (Exception e) {
    		Log.e("LS-APP", "Unexpected exception - Here is what I know: "+e.getMessage());
    		return null;
    	}
	}
	
	public static String getActualMaxFrequence (){
		String values;
		try {
        	FileReader input = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
        	BufferedReader reader = new BufferedReader(input);
        	values = reader.readLine();
        	reader.close();
        	input.close();
        	return values;
    	} catch (Exception e) {
    		Log.e("LS-APP", "Unexpected exception - Here is what I know: "+e.getMessage());
    		return "";
    	}
	}
	
	public static String getActualMinFrequence (){
		String values;
		try {
        	FileReader input = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
        	BufferedReader reader = new BufferedReader(input);
        	values = reader.readLine();
        	reader.close();
        	input.close();
        	return values;
    	} catch (Exception e) {
    		Log.e("LS-APP", "Unexpected exception - Here is what I know: "+e.getMessage());
    		return "";
    	}
    }
    	
    	public static String getActualGovernor (){
    		String values;
    		try {
            	FileReader input = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
            	BufferedReader reader = new BufferedReader(input);
            	values = reader.readLine();
            	reader.close();
            	input.close();
            	return values;
        	} catch (Exception e) {
        		Log.e("LS-APP", "Unexpected exception - Here is what I know: "+e.getMessage());
        		return "";
        	}
    	}
}
