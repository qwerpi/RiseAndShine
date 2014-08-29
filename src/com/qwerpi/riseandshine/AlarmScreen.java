package com.qwerpi.riseandshine;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmScreen extends ActionBarActivity implements
		SensorEventListener {

	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;
	private Vibrator vibrator;

	private Button dismiss;

	private SensorManager mSensorManager;
	private Sensor mLight;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Log.e("ALARM_SCREEN", "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_screen);

		getSupportActionBar().hide();

		int timeHour = getIntent().getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
		int timeMinute = getIntent().getIntExtra(
				AlarmManagerHelper.TIME_MINUTE, 0);
		Uri tone = (Uri) getIntent().getExtras().get(AlarmManagerHelper.TONE);
		long id = getIntent().getLongExtra(AlarmManagerHelper.ID, -1);
		if (id >= 0) {
			AlarmDBHelper db = new AlarmDBHelper(this);
			AlarmModel model = db.getAlarm(id);
			model.isEnabled = model.repeatWeekly; // only disable one-time
													// alarms
			db.updateAlarm(model);
		}

		TextView txtTime = (TextView) findViewById(R.id.alarm_screen_time);
		String time = String.format("%02d : %02d <small>%s</small>",
				(timeHour % 12) + (timeHour % 12 == 0 ? 12 : 0), timeMinute,
				(timeHour >= 12 ? "PM" : "AM"));
		txtTime.setText(Html.fromHtml(time));

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

		dismiss = (Button) findViewById(R.id.alarm_screen_button);
		dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.cancel();
				if (mPlayer.isPlaying())
					mPlayer.stop();
				finish();
			}
		});
		// dismiss.setEnabled(false);

		mPlayer = new MediaPlayer();
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		AudioManager audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
			try {
				long[] pattern = { 0, 500, 500 };
				vibrator.vibrate(pattern, 0);
				if (tone != null) {
					mPlayer.setDataSource(this, tone);
					mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					mPlayer.setLooping(true);
					mPlayer.prepare();
					mPlayer.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Runnable releaseWakelock = new Runnable() {
			public void run() {
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			}
		};

		new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
//		Log.e("ALARM_SCREEN", "onResume");
		super.onResume();

		mSensorManager.registerListener((SensorEventListener) this, mLight,
				SensorManager.SENSOR_DELAY_NORMAL);

		// Set the window to keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Acquire wakelock
		PowerManager pm = (PowerManager) getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = pm
					.newWakeLock(
							(PowerManager.FULL_WAKE_LOCK
									| PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
							TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
//			Log.i(TAG, "Wakelock aquired!!");
		}
	}

	@Override
	protected void onPause() {
//		Log.e("ALARM_SCREEN", "onPause");
		super.onPause();

		mSensorManager.unregisterListener((SensorEventListener) this, mLight);

		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}

		// set alarms in case this alarm repeats
		AlarmManagerHelper.setAlarms(this);
	}
	
//	@Override
//	protected void onStart() {
//		Log.e("ALARM_SCREEN", "onStart");
//		super.onStart();
//	}
//	
//	@Override
//	protected void onStop() {
//		Log.e("ALARM_SCREEN", "onStop");
//		super.onStop();
//	}
//	
//	@Override
//	protected void onDestroy() {
//		Log.e("ALARM_SCREEN", "onDestroy");
//		super.onDestroy();
//	}
//	
//	@Override
//	protected void onRestart() {
//		Log.e("ALARM_SCREEN", "onRestart");
//		super.onRestart();
//	}
	
	// prevent back button from exiting
	@Override
	public void onBackPressed() {
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			// if (event.values[0] == mLight.getMaximumRange())
			// dismiss.setEnabled(true);
			// dismiss.setText(event.values[0]+"");
		}
	}
}
