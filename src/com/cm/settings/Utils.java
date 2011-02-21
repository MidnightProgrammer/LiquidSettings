package com.cm.settings;

public class Utils {
	
	public static boolean onlyNumbers(String str){
		char [] string = str.toCharArray();
		for (int i=0; i < str.length(); i++){
			if (((int)string[i]<49 || (int)string[i]>57))
				return false;
		}
		return true;
	}
	
	
}
