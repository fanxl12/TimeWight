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
	 * ����GPS����������δ��6�������
	 * @param lon	string	Y	���ȣ��磺116.39277
 	 * @param lat	string	Y	γ�ȣ��磺39.933748
 	 * @param format	int	N	δ��6��Ԥ��(future)���ַ��ظ�ʽ��1��2��Ĭ��1
 	 * @param dtype	string	Y	�������ݸ�ʽ��json��xml,Ĭ��json
 	 * @param key	string	Y	�������key
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
