package com.fanxl.timer;

import java.util.Locale;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Time;
import android.widget.RemoteViews;

public class UpdateService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("UpdateService onCreate");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		updateWidght(this);
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateWidght(Context context) {
		// ����Calendar��Time��cpu���ɽ�С
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
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.time);
		remoteViews.setTextViewText(R.id.main_tv_time, strTime);
		remoteViews.setTextViewText(R.id.main_tv_date, year + "��" + month + "��"
				+ day + "��\t\t" + getWeekDay(weekDay));
		AppWidgetManager appWidgetManger = AppWidgetManager
				.getInstance(context);
		int[] appIds = appWidgetManger.getAppWidgetIds(new ComponentName(
				context, TimeProvider.class));
		appWidgetManger.updateAppWidget(appIds, remoteViews);
	}

	private String getWeekDay(int weekDay) {
		switch (weekDay) {
		case 0:
			return "����";
		case 1:
			return "��һ";
		case 2:
			return "�ܶ�";
		case 3:
			return "����";
		case 4:
			return "����";
		case 5:
			return "����";
		case 6:
			return "����";
		}
		return "��";
	}

}
