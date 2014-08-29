package com.qwerpi.riseandshine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class AlarmManagerHelper extends BroadcastReceiver {

	public static final String ID = "id";
	public static final String TIME_HOUR = "timeHour";
	public static final String TIME_MINUTE = "timeMinute";
	public static final String TONE = "alarmTone";

	@Override
	public void onReceive(Context context, Intent intent) {
		setAlarms(context);
	}

	public static void setAlarms(Context context) {
		cancelAlarms(context);

		AlarmDBHelper dbHelper = new AlarmDBHelper(context);
		List<AlarmModel> alarms = dbHelper.getAlarms();

		Calendar nextAlarm = null;

		if (alarms != null) {
			for (AlarmModel alarm : alarms) {
				if (alarm.isEnabled) {
					PendingIntent pIntent = createPendingIntent(context, alarm);

					Calendar alarmDate = Calendar.getInstance();
					alarmDate.set(Calendar.HOUR_OF_DAY, alarm.timeHour);
					alarmDate.set(Calendar.MINUTE, alarm.timeMinute);
					alarmDate.set(Calendar.SECOND, 0);

					// don't set alarms in the past
					Calendar cur = Calendar.getInstance();
					while (cur.after(alarmDate)) {
						alarmDate.add(Calendar.DATE, 1);
					}

					// only set more than 24hr in the future if a specific day
					// of week is selected
					// otherwise, the next closest time should be used
					if (alarm.repeatWeekly) {
						while (!alarm.getRepeatingDay(alarmDate
								.get(Calendar.DAY_OF_WEEK) - 1)) {
							alarmDate.add(Calendar.DATE, 1);
						}
					}

					if (nextAlarm == null || nextAlarm.after(alarmDate)) {
						nextAlarm = (Calendar) alarmDate.clone();
					}

					setAlarm(context, alarmDate, pIntent);
				}
			}
		}

		if (nextAlarm != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("M/d h:mm a", Locale.US);
			sdf.setCalendar(nextAlarm);
			Settings.System.putString(context.getContentResolver(),
					Settings.System.NEXT_ALARM_FORMATTED, sdf.format(nextAlarm.getTime()));
		} else {
			Settings.System.putString(context.getContentResolver(),
					Settings.System.NEXT_ALARM_FORMATTED, "");
		}
	}

	@SuppressLint("NewApi")
	private static void setAlarm(Context context, Calendar alarmDate,
			PendingIntent pIntent) {
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			alarmManager.setExact(AlarmManager.RTC_WAKEUP,
					alarmDate.getTimeInMillis(), pIntent);
		} else {
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					alarmDate.getTimeInMillis(), pIntent);
		}
	}

	public static void cancelAlarms(Context context) {
		AlarmDBHelper dbHelper = new AlarmDBHelper(context);
		List<AlarmModel> alarms = dbHelper.getAlarms();

		if (alarms != null) {
			for (AlarmModel alarm : alarms) {
				if (alarm.isEnabled) {
					PendingIntent pIntent = createPendingIntent(context, alarm);

					AlarmManager alarmManager = (AlarmManager) context
							.getSystemService(Context.ALARM_SERVICE);
					alarmManager.cancel(pIntent);
				}
			}
		}
	}

	private static PendingIntent createPendingIntent(Context context,
			AlarmModel model) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(ID, model.id);
		intent.putExtra(TIME_HOUR, model.timeHour);
		intent.putExtra(TIME_MINUTE, model.timeMinute);
		intent.putExtra(TONE, model.alarmTone);

		return PendingIntent.getBroadcast(context, (int) model.id, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
