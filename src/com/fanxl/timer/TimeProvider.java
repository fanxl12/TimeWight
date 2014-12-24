package com.fanxl.timer;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.format.Time;
import android.widget.RemoteViews;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.fanxl.timer.utils.GetWeahter;
import com.fanxl.timer.utils.GsonTools;
import com.fanxl.timer.utils.Mark;
import com.fanxl.timer.utils.Util;

/**
 * 桌面插件的主界面程序
 * @author fanxl
 *
 */
public class TimeProvider extends AppWidgetProvider{
	
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	private boolean isUpdateWeather = false;
	
	private Context context;
	private int[] futureIcons = new int[]{R.id.future_one_icon, R.id.future_two_icon, R.id.future_three_icon, R.id.future_four_icon};
	private int[] futureTexts = new int[]{R.id.future_one_text, R.id.future_two_text, R.id.future_three_text, R.id.future_four_text};
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Mark.SUCCESS:
				Map<String, Object> weather = GsonTools.getMap(msg.obj+"");
				showWeahter(weather);
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if(intent.getAction().equals(Mark.UPDATE_WEAHTER_ACTION) && !isUpdateWeather){
			SharedPreferences sp = context.getSharedPreferences(Mark.SP_NAME, Activity.MODE_PRIVATE);
			Util.toToast(context, "正在为您更新天气，请稍后!");
			isUpdateWeather = true;
			this.context=context;
			updateWithNewLocation(Double.parseDouble(sp.getString(Mark.GPS_LON, "0.00")), Double.parseDouble(sp.getString(Mark.GPS_LAT, "0.00")));
		}
		
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		this.context = context;
		System.out.println("TimeProvider onUpdate");
		mLocationClient = new LocationClient(context.getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        
       //设置定位条件
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);        //是否打开GPS
        option.setCoorType("bd09ll");       //设置返回值的坐标类型。
        option.setLocationMode(LocationMode.Battery_Saving);
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setScanSpan(Mark.UPDATE_TIME); //三十分钟获取一次
        option.setProdName("TimeWight"); //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        mLocationClient.setLocOption(option);
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.time);
		
		Time time = showTime(remoteViews);
		
		//启动一个服务来更新时间
		Intent intent = new Intent(context, UpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		//使用Alarm定时更新界面数据
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC, time.toMillis(true), 1000, pendingIntent);
        
		Intent intentClick = new Intent(Mark.UPDATE_WEAHTER_ACTION);
		PendingIntent updateIntent = PendingIntent.getBroadcast(context, 0,intentClick, 0);
        remoteViews.setOnClickPendingIntent(R.id.main_time, updateIntent);
        
        remoteViews.setTextViewText(R.id.update_time_tips, "正在为您更新天气!");
        
        mLocationClient.start();
		
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		if (mLocationClient != null && mLocationClient.isStarted()) {
        	mLocationClient.stop();
        	System.out.println("停止定位服务");
        	mLocationClient = null;
        }
	}
	
	private String getWeekDay(int weekDay){
		switch (weekDay) {
		case 0:
			return "周日";
		case 1:
			return "周一";
		case 2:
			return "周二";
		case 3:
			return "周三";
		case 4:
			return "周四";
		case 5:
			return "周五";
		case 6:
			return "周六";
		}
		return "周";
	}

	protected void updateWithNewLocation(double lon, double lat) {
		GetWeahter weather = new GetWeahter(mHandler);
		weather.getWeatherByGps(lat,lon);
	}
	
	@SuppressWarnings("unchecked")
	private void showWeahter(Map<String, Object> data){
		Map<String, Object> result = (Map<String, Object>) data.get("result");
		List<Map<String, Object>> futures = (List<Map<String, Object>>) result.get("future");
		Map<String, Object> sk = (Map<String, Object>) result.get("sk"); //风力
		Map<String, Object> today = (Map<String, Object>) result.get("today"); //天气和温度
		Map<String, Object> weatherId = (Map<String, Object>) today.get("weather_id");
		
		AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
		int[] appIds = appWidgetManger.getAppWidgetIds(new ComponentName(context, TimeProvider.class));
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.time);
		Time time = showTime(remoteViews);
		int weather_icon = R.drawable.day_big_00;
		if(6<time.hour && time.hour<18){
			weather_icon = getBigIcon(Integer.parseInt(weatherId.get("fa")+""), true);
		}else{
			weather_icon = getBigIcon(Integer.parseInt(weatherId.get("fa")+""), false);
		}
		remoteViews.setImageViewResource(R.id.iv_weather_icon, weather_icon);
        remoteViews.setTextViewText(R.id.main_tv_temp, "当前温度:"+sk.get("temp")+"°");
        remoteViews.setTextViewText(R.id.main_tv_wind, sk.get("wind_direction")+""+sk.get("wind_strength"));
        remoteViews.setTextViewText(R.id.main_tv_weather, today.get("city")+"\t"+today.get("weather"));
        remoteViews.setTextViewText(R.id.main_day_temp, Util.getTemp(today.get("temperature")));
        
        showFutureWeahter(futures, remoteViews, time);
		if(isUpdateWeather){
			Util.toToast(context, "天气更新成功!");
			isUpdateWeather = false;
		}
		
		int hour = time.hour;
		int min = time.minute;
		String strTime = String.format(Locale.CHINA, "%02d:%02d", hour,min);
		remoteViews.setTextViewText(R.id.update_time_tips, strTime+"更新了天气");
		
		appWidgetManger.updateAppWidget(appIds, remoteViews);
	}
	
	@SuppressWarnings("unchecked")
	private void showFutureWeahter(List<Map<String, Object>> futures, RemoteViews remoteViews, Time time){
		if(futures==null)return;
		int weekDay = time.weekDay;
		for (int i = 1; i < 5; i++) {
			
			Map<String, Object> itemData = futures.get(i);
			Map<String, Object> weatherId = (Map<String, Object>) itemData.get("weather_id");
			if(6<time.hour && time.hour<18){
				remoteViews.setImageViewResource(futureIcons[i-1], getBigIcon(Integer.parseInt(weatherId.get("fa")+""), true));
			}else{
				remoteViews.setImageViewResource(futureIcons[i-1], getBigIcon(Integer.parseInt(weatherId.get("fa")+""), false));
			}
			weekDay++;
			if(weekDay==7)weekDay=0;
			remoteViews.setTextViewText(futureTexts[i-1], getWeekDay(weekDay)+Util.getTemp(itemData.get("temperature")));
		}
	}
	
	
	private Time showTime(RemoteViews remoteViews){
		Time time = new Time();
		time.setToNow();
		int hour = time.hour;
		int min = time.minute;
		int second = time.second;
		int year = time.year;
		int month = time.month + 1;
		int day = time.monthDay;
		int weekDay = time.weekDay;
		String strTime = String.format(Locale.CHINA, "%02d:%02d:%02d", hour,
				min, second);
		remoteViews.setTextViewText(R.id.main_tv_time, strTime);
		remoteViews.setTextViewText(R.id.main_tv_date, year + "年" + month + "月"
				+ day + "日\t\t" + getWeekDay(weekDay));
		return time;
	}
	
	private int getBigIcon(int icon, boolean isDay){
		switch (icon) {
		case 00:
			if(isDay){
				return R.drawable.day_big_00;
			}else{
				return R.drawable.night_big_00;
			}
		case 01:
			if(isDay){
				return R.drawable.day_big_01;
			}else{
				return R.drawable.night_big_01;
			}
		case 02:
			if(isDay){
				return R.drawable.day_big_02;
			}else{
				return R.drawable.night_big_02;
			}
		case 03:
			if(isDay){
				return R.drawable.day_big_03;
			}else{
				return R.drawable.night_big_03;
			}
		case 04:
			if(isDay){
				return R.drawable.day_big_04;
			}else{
				return R.drawable.night_big_04;
			}
		case 05:
			if(isDay){
				return R.drawable.day_big_05;
			}else{
				return R.drawable.night_big_05;
			}
		case 06:
			if(isDay){
				return R.drawable.day_big_06;
			}else{
				return R.drawable.night_big_06;
			}
		case 07:
			if(isDay){
				return R.drawable.day_big_07;
			}else{
				return R.drawable.night_big_07;
			}
		case 8:
			if(isDay){
				return R.drawable.day_big_08;
			}else{
				return R.drawable.night_big_08;
			}
		case 9:
			if(isDay){
				return R.drawable.day_big_09;
			}else{
				return R.drawable.night_big_09;
			}
		case 10:
			if(isDay){
				return R.drawable.day_big_10;
			}else{
				return R.drawable.night_big_10;
			}
		case 11:
			if(isDay){
				return R.drawable.day_big_11;
			}else{
				return R.drawable.night_big_11;
			}
		case 12:
			if(isDay){
				return R.drawable.day_big_12;
			}else{
				return R.drawable.night_big_12;
			}
		case 13:
			if(isDay){
				return R.drawable.day_big_13;
			}else{
				return R.drawable.night_big_13;
			}
		case 14:
			if(isDay){
				return R.drawable.day_big_14;
			}else{
				return R.drawable.night_big_14;
			}
		case 15:
			if(isDay){
				return R.drawable.day_big_15;
			}else{
				return R.drawable.night_big_15;
			}
		case 16:
			if(isDay){
				return R.drawable.day_big_16;
			}else{
				return R.drawable.night_big_16;
			}
		case 17:
			if(isDay){
				return R.drawable.day_big_17;
			}else{
				return R.drawable.night_big_17;
			}
		case 18:
			if(isDay){
				return R.drawable.day_big_18;
			}else{
				return R.drawable.night_big_18;
			}
		case 19:
			if(isDay){
				return R.drawable.day_big_19;
			}else{
				return R.drawable.night_big_19;
			}
		case 20:
			if(isDay){
				return R.drawable.day_big_20;
			}else{
				return R.drawable.night_big_20;
			}
		case 21:
			if(isDay){
				return R.drawable.day_big_21;
			}else{
				return R.drawable.night_big_21;
			}
		case 22:
			if(isDay){
				return R.drawable.day_big_22;
			}else{
				return R.drawable.night_big_22;
			}
		case 23:
			if(isDay){
				return R.drawable.day_big_23;
			}else{
				return R.drawable.night_big_23;
			}
		case 24:
			if(isDay){
				return R.drawable.day_big_24;
			}else{
				return R.drawable.night_big_24;
			}
		case 25:
			if(isDay){
				return R.drawable.day_big_25;
			}else{
				return R.drawable.night_big_25;
			}
		case 26:
			if(isDay){
				return R.drawable.day_big_26;
			}else{
				return R.drawable.night_big_26;
			}
		case 27:
			if(isDay){
				return R.drawable.day_big_27;
			}else{
				return R.drawable.night_big_27;
			}
		case 28:
			if(isDay){
				return R.drawable.day_big_28;
			}else{
				return R.drawable.night_big_28;
			}
		case 29:
			if(isDay){
				return R.drawable.day_big_29;
			}else{
				return R.drawable.night_big_29;
			}
		case 30:
			if(isDay){
				return R.drawable.day_big_30;
			}else{
				return R.drawable.night_big_30;
			}
		case 31:
			if(isDay){
				return R.drawable.day_big_31;
			}else{
				return R.drawable.night_big_31;
			}
		case 53:
			if(isDay){
				return R.drawable.day_big_53;
			}else{
				return R.drawable.night_big_53;
			}
		default:
			if(isDay){
				return R.drawable.day_big_00;
			}else{
				return R.drawable.night_big_00;
			}
		}
	}
	
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
		            return ;
			StringBuffer sb = new StringBuffer(256);
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			if (location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			} 
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			context.getSharedPreferences(Mark.SP_NAME, Activity.MODE_PRIVATE).edit().putString(Mark.GPS_LAT, lat+"").putString(Mark.GPS_LON, lon+"").commit();
			System.out.println(sb.toString());
			updateWithNewLocation(lon, lat);
		}
	}

}
