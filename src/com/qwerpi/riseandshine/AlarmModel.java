package com.qwerpi.riseandshine;

import android.net.Uri;

public class AlarmModel {

	public static final int SUNDAY = 0;
	public static final int MONDAY = 1;
	public static final int TUESDAY = 2;
	public static final int WEDNESDAY = 3;
	public static final int THURSDAY = 4;
	public static final int FRDIAY = 5;
	public static final int SATURDAY = 6;

	public long id = -1; // -1 indicates new alarm
	public int timeHour;
	public int timeMinute;
	private boolean repeatingDays[];
	public boolean repeatWeekly;
	public Uri alarmTone;
	public boolean isEnabled = true;

	public AlarmModel() {
		repeatingDays = new boolean[7];
	}

	public void setRepeatingDay(int dayOfWeek, boolean value) {
		repeatingDays[dayOfWeek] = value;
	}

	public boolean getRepeatingDay(int dayOfWeek) {
		return repeatingDays[dayOfWeek];
	}

}