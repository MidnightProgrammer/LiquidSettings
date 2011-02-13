package com.cm.settings;

import java.io.*;
import java.io.FileReader;
import android.util.Log;

public class LiquidSettings {
	
	public static boolean isRoot(){
		return runRootCommand("echo $?");
	}
	
	//DEBUG FUNCTION
	public static String debugf(){
		String [] cmd = {"su","echo $?"};
		try{
        	Process proc= Runtime.getRuntime().exec(cmd);
        	BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        	String reply=in.readLine(); //THE BUG IS HERE
        	in.close();
        	return reply;
		} catch (Exception e){
		}
		return null;
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
        	System.out.println("[DEBUG] " + reply +"\n");
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