package com.fanxl.timer.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GsonTools {
	
	/**
	 * @param jsonString
	 * @param cls
	 * @return
	 */
	public static <T> T getTickeys(String jsonString, Class<T> cls){
		T t = null;
		Gson gson = new Gson();
		t = gson.fromJson(jsonString, cls);
		return t;
	}
	
	
	public static List<Map<String, Object>> getMaps(String jsonString){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Gson gson = new Gson();
		list = gson.fromJson(jsonString, new TypeToken<List<Map<String, Object>>>(){}.getType());
		return list;
	}
	
	public static Map<String, Object> getMap(String jsonString){
		Map<String, Object> map = new HashMap<String, Object>();
		Gson gson = new Gson();
		map = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>(){}.getType());
		return map;
	}

}
