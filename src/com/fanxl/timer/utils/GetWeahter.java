package com.fanxl.timer.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

public class GetWeahter {
	
private Handler mHandler;
	
	public GetWeahter(Handler mHandler){
		this.mHandler=mHandler;
	}
	
	/**
	 * 根据GPS坐标来请求未来6天的天气
	 * @param lon	string	Y	经度，如：116.39277
 	 * @param lat	string	Y	纬度，如：39.933748
 	 * @param format	int	N	未来6天预报(future)两种返回格式，1或2，默认1
 	 * @param dtype	string	Y	返回数据格式：json或xml,默认json
 	 * @param key	string	Y	你申请的key
	 */
	public void getWeatherByGps(double lat, double lon) {
		Parameters params = new Parameters();
		params.add("key", Mark.APP_KEY);
		params.add("dtype", "json");
		params.add("lon", lon);
		params.add("lat", lat);
		params.add("format", 2);
		JuheData.executeWithAPI(Mark.APP_ID, "http://v.juhe.cn/weather/geo",
				JuheData.GET, params, new DataCallBack() {
					@Override
					public void resultLoaded(int err, String reason,
							String result) {

						if (err == 0) {
							Message msg = Message.obtain(mHandler, Mark.SUCCESS, result);
							msg.sendToTarget();
						} else {
							Message msg = Message.obtain(mHandler, Mark.FAIL, result);
							msg.sendToTarget();
						}
					}
				});
	}

}
