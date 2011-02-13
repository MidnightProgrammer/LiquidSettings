package com.cm.settings;


public class Compcache {
	
	public static boolean autoStart(){
		//return (new File("/etc/init.d/06compcache").exists());
		return true;
	}
	
	public static boolean setAutoStart(boolean opt){
		try{
			if(opt){
				if (LiquidSettings.runRootCommand("echo " + Strings.getCompcacheAutostart() + " > /etc/06compcache"))
					return true;
				else return false;
			} else {
				if (LiquidSettings.runRootCommand("rm /etc/06compcache"))
					return true;
				else return false;
			}
		} catch (Exception e){}
		
		return false;
	}
}
