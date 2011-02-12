package com.cm.settings;

import java.io.*;
import java.io.FileReader;
import android.util.Log;

public class LiquidSettings {
	
	public static boolean isRoot(){
		return runRootCommand("echo $?");
	}
	
	public static boolean runRootCommand(String ... command) {
        int n = command.length;
        boolean right=false;
        //Check if the last command string == "echo $?"
        if (!command[n-1].equals("echo $?"))
        	++n;
        else 
                right=true;
        String [] cmd= new String[n+1];
        cmd[0]="su";
        for(int i=1;i<n;i++) cmd[i]=command[i-1];
        if (!right)
        	cmd[n]="echo $?";
	try {
        	Process proc= Runtime.getRuntime().exec(cmd);
        	BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        	String reply=in.readLine();
        	in.close();
        	proc=null;
        	//If terminal replies 0 the root is available
        	if (!reply.equals("0")) return false;
        	else return true;
        } catch (Exception e){
        }
        return false;
    }
	
	public static boolean vibrStatus() {
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
}