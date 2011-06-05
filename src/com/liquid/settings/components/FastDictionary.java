package com.liquid.settings.components;

public class FastDictionary {
	private String [] string;
	private String [] second;
	
	public FastDictionary (String ... param){
		string=new String[param.length];
		second= new String[param.length];
		for (int i=0; i < param.length; i++){
			string[i]=(param[i].split(":"))[0];
			second[i]=(param[i].split(":"))[1];
		}
	}
	
	public String getValue (String key){
		for (int i=0;i<string.length;i++){
			if (string[i].equals(key))
				return second[i];
			else if (second[i].equals(key))
				return string[1];
		}
		return "";
	}
	
	public String [] getFirsts(){
		return string;
	}
	
	public String [] getSeconds (){
		return second;
	}
	
	public int getIntValue (String key){
		return Integer.parseInt(this.getValue(key));
	}
}

