package com.cm.settings;

import java.io.File;

public class Compcache {
	
	public static boolean autoStart() {
		return (new File("/etc/init.d/06compcache").exists()) ? true : false;
	}
	
	public static boolean setAutoStart(boolean opt){
		try{
			if(opt == true){
				if (LiquidSettings.runRootCommand("echo " + Strings.getCompcacheAutostart() + " > /etc/init.d/06compcache"))
					return true;
				else 
					return false;
			} else {
				if (LiquidSettings.runRootCommand("rm /etc/init.d/06compcache"))
					return true;
				else 
					return false;
			}
		} catch (Exception e){}
		return false;
	}
	
	public static boolean isCompcacheRunning(){
		return (new File("/dev/block/ramzswap0").exists());
	}
	
}
