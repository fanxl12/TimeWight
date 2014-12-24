package com.fanxl.timer.utils;

import android.content.Context;
import android.widget.Toast;

public class Util {
	
	public static String getTemp(Object object){
		String temp = object + "";
		return temp.subSequence(0, 1)+"¡ã/"+temp.substring(temp.indexOf("~")+1, temp.lastIndexOf("¡æ"))+"¡ã";
	}
	
	public static void toToast(Context context, String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

}
