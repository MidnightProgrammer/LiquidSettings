package com.liquid.settings;

import java.io.File;
import android.content.Context;

public class Adsfilter {
	
	public static long getHostsDimensions(){
		return (new File("/system/etc/hosts").length());
	}
	
	public static boolean isFiltered(){
		if (new File("/system/etc/.hosts-bkp").exists() && getHostsDimensions()!=0 && getHostsDimensions() > 15000) 
			return true;
		else if (getHostsDimensions()!=0 && getHostsDimensions() > 15000)
			return true;
		return false;
	}
	
	//Backup /system/etc/hosts
	public static boolean backupHostsFile(){
		if (LSystem.RemountRW()){
			if (LiquidSettings.runRootCommand("cp /system/etc/hosts /system/etc/.hosts-bkp") && LSystem.RemountROnly())
				return true;
		}
		return false;
	}
	
	public static boolean restoreHostsFile(){
		if (LSystem.RemountRW()){
				if (LiquidSettings.runRootCommand("mv /system/etc/.hosts-bkp /system/etc/hosts") 
					&& LiquidSettings.runRootCommand("chmod 644 /system/etc/hosts") && LSystem.RemountROnly())
					return true;
		}
		return false;
	}
	
	public static boolean apply(Context cont){
		if (LSystem.RemountRW()){
			backupHostsFile();
			if (LiquidSettings.runRootCommand("echo $(cat /system/etc/hosts)'" + 
					(cont.getResources().getString(R.string.hosts))  + "' > /system/etc/hosts") 
					&& LiquidSettings.runRootCommand("chmod 644 /system/etc/hosts")
					&& LSystem.RemountROnly())
				return true;	
		}
		return false;
	}
	

	
}
