package com.fanxl.timer;

import android.app.Application;

import com.thinkland.sdk.android.SDKInitializer;

public class MyApplication extends Application{
	
	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(getApplicationContext()); 
	}

}
