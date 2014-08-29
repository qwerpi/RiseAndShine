package com.qwerpi.riseandshine;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.qwerpi.riseandshine.AlarmContract.Alarm;

public class AlarmDBHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "alarmclock.db";

	private static final String SQL_CREATE_ALARM = "CREATE TABLE "
			+ Alarm.TABLE_NAME + " (" + Alarm._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ Alarm.COLUMN_NAME_ALARM_TIME_HOUR + " INTEGER,"
			+ Alarm.COLUMN_NAME_ALARM_TIME_MINUTE + " INTEGER,"
			+ Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS + " TEXT,"
			+ Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY + " BOOLEAN,"
			+ Alarm.COLUMN_NAME_ALARM_TONE + " TEXT,"
			+ Alarm.COLUMN_NAME_ALARM_ENABLED + " BOOLEAN" + " )";
	private static final String SQL_DELETE_ALARM = "DROP TABLE IF EXISTS "
			+ Alarm.TABLE_NAME;

	public AlarmDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ALARM);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ALARM);
		onCreate(db);
	}

	private AlarmModel populateModel(Cursor c) {
		AlarmModel model = new AlarmModel();
		model.id = c.getLong(c.getColumnIndex(Alarm._ID));
		model.timeHour = c.getInt(c
				.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TIME_HOUR));
		model.timeMinute = c.getInt(c
				.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TIME_MINUTE));
		model.repeatWeekly = c.getInt(c
				.getColumnIndex(Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY)) == 0 ? false
				: true;
		// model.alarmTone = Uri.parse(c.getString(c
		// .getColumnIndex(Alarm.COLUMN_NAME_ALARM_TONE)));
		model.alarmTone = c.getString(
				c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TONE)).equals("") ? null
				: Uri.parse(c.getString(c
						.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TONE)));
		model.isEnabled = c.getInt(c
				.getColumnIndex(Alarm.COLUMN_NAME_ALARM_ENABLED)) == 0 ? false
				: true;

		String[] repeatingDays = c.getString(
				c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS)).split(
				",");
		for (int i = 0; i < repeatingDays.length; ++i) {
			model.setRepeatingDay(i, repeatingDays[i].equals("false") ? false
					: true);
		}

		return model;
	}

	private ContentValues populateContent(AlarmModel model) {
		ContentValues values = new ContentValues();
		values.put(Alarm.COLUMN_NAME_ALARM_TIME_HOUR, model.timeHour);
		values.put(Alarm.COLUMN_NAME_ALARM_TIME_MINUTE, model.timeMinute);
		values.put(Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY, model.repeatWeekly);
		values.put(Alarm.COLUMN_NAME_ALARM_TONE, model.alarmTone == null ? ""
				: model.alarmTone.toString());
		values.put(Alarm.COLUMN_NAME_ALARM_ENABLED, model.isEnabled);

		String repeatingDays = "";
		for (int i = 0; i < 7; ++i) {
			repeatingDays += model.getRepeatingDay(i) + ",";
		}
		values.put(Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS, repeatingDays);

		return values;
	}

	public long createAlarm(AlarmModel model) {
		ContentValues values = populateContent(model);
		SQLiteDatabase db = this.getWritableDatabase();
		long ret = db.insert(Alarm.TABLE_NAME, null, values);

		if (db != null)
			db.close();
		return ret;
	}

	public AlarmModel getAlarm(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + Alarm.TABLE_NAME + " WHERE "
				+ Alarm._ID + " = " + id;

		Cursor c = db.rawQuery(select, null);

		if (c.moveToNext()) {
			AlarmModel m = populateModel(c);
			if (c != null)
				c.close();
			if (db != null)
				db.close();
			return m;
		}

		if (c != null)
			c.close();
		if (db != null)
			db.close();

		return null;
	}

	public long updateAlarm(AlarmModel model) {
		ContentValues values = populateContent(model);
		SQLiteDatabase db = this.getWritableDatabase();
		long ret = db.update(Alarm.TABLE_NAME, values, Alarm._ID + " = ?",
				new String[] { String.valueOf(model.id) });

		if (db != null)
			db.close();
		return ret;
	}

	public int deleteAlarm(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		int ret = db.delete(Alarm.TABLE_NAME, Alarm._ID + " = ?",
				new String[] { String.valueOf(id) });

		if (db != null)
			db.close();
		return ret;
	}

	public List<AlarmModel> getAlarms() {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + Alarm.TABLE_NAME + " ORDER BY "
				+ Alarm.COLUMN_NAME_ALARM_TIME_HOUR + ", "
				+ Alarm.COLUMN_NAME_ALARM_TIME_MINUTE;

		Cursor c = db.rawQuery(select, null);

		List<AlarmModel> alarmList = new ArrayList<AlarmModel>();

		while (c.moveToNext()) {
			alarmList.add(populateModel(c));
		}

		if (c != null)
			c.close();
		if (db != null)
			db.close();

		if (!alarmList.isEmpty()) {
			return alarmList;
		}

		return null;
	}
}
