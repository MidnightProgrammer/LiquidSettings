package com.liquid.settings;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

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
		InputStream hostInput = cont.getResources().openRawResource(R.raw.hosts);
		byte [] bytesToWrite = {0x0};
		try {
			bytesToWrite = new byte [hostInput.available()];
			while (hostInput.read(bytesToWrite) != -1){}
		}catch (Exception e){
			Log.e("LS-APP",e.getMessage());
			return false;
		}
		if (LSystem.RemountROnly()){
			backupHostsFile();
			LSystem.RemountRW();
			try {
				LiquidSettings.runRootCommand("echo '" + new String(bytesToWrite) + "' > /system/etc/hosts");
			} catch (Exception e) {
				Log.e("LS-APP", e.getMessage());
				return false;
			}
			LSystem.RemountROnly();
			return true;
		}
		return false;
	}
	
}