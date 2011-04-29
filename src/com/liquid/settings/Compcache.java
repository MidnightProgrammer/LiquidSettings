package com.liquid.settings;

import java.io.File;

public class Compcache {
	
	public static boolean autoStart() {
		return (new File("/etc/init.d/06compcache").exists()) ? true : false;
	}
	
	public static boolean setAutoStart(boolean opt){
		try{
			if(opt == true){
				if (LiquidSettings.runRootCommand("echo " + Strings.getCompcacheAutostart() + " > /etc/init.d/06compcache") && LiquidSettings.runRootCommand("chmod +x /etc/init.d/06compcache"))
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
		return ((new File("/dev/block/ramzswap0").exists()) ? true : false);
	}
	
	public static boolean turnCompcache(boolean status){
		if(status){
			if (LiquidSettings.runRootCommand("compcache start"))
				return ((isCompcacheRunning()) ? true : false);
			return false;
		} else {
			if (LiquidSettings.runRootCommand("compcache stop")){
				//This for statement is to waste time
				//Compcache can't stop immediatly, so the app
				//must wait time before return true.
				while ((isCompcacheRunning())){}
				return true;
			}
			return false;
		}
	}
	
	public static boolean filesExist(){
		//Check if the necessary files exist
		if ((new File("/system/bin/compcache").exists()==false) || (new File("/system/bin/insmod").exists()==false) || (new File("/system/lib/modules/ramzswap.ko").exists()==false) || (new File("/system/xbin/rzscontrol").exists()==false) ){
			return false;
		}
		return true;
	}
	
}
