package com.qwerpi.riseandshine;

import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmDetailsActivity extends ActionBarActivity {

	private AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	private AlarmModel alarmDetails;

	private TimePicker timePicker;
	private CheckBox chkSunday;
	private CheckBox chkMonday;
	private CheckBox chkTuesday;
	private CheckBox chkWednesday;
	private CheckBox chkThursday;
	private CheckBox chkFriday;
	private CheckBox chkSaturday;
	private TextView txtToneSelection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		getSupportActionBar().setTitle("Alarm Details");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		timePicker = (TimePicker) findViewById(R.id.alarm_details_time_picker);

		chkSunday = (CheckBox) findViewById(R.id.alarm_details_repeat_sunday);
		chkMonday = (CheckBox) findViewById(R.id.alarm_details_repeat_monday);
		chkTuesday = (CheckBox) findViewById(R.id.alarm_details_repeat_tuesday);
		chkWednesday = (CheckBox) findViewById(R.id.alarm_details_repeat_wednesday);
		chkThursday = (CheckBox) findViewById(R.id.alarm_details_repeat_thursday);
		chkFriday = (CheckBox) findViewById(R.id.alarm_details_repeat_friday);
		chkSaturday = (CheckBox) findViewById(R.id.alarm_details_repeat_saturday);

		txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);

		long id = getIntent().getExtras().getLong("id");
		if (id == -1) { // new alarm
			alarmDetails = new AlarmModel();

			// select existing alarm as default
			if (alarmDetails.alarmTone == null) {
				alarmDetails.alarmTone = RingtoneManager
						.getActualDefaultRingtoneUri(this,
								RingtoneManager.TYPE_ALARM);
				// alarmDetails.alarmTone = RingtoneManager
				// .getDefaultUri(RingtoneManager.TYPE_ALARM);
				TextView txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
				txtToneSelection.setText(RingtoneManager.getRingtone(this,
						alarmDetails.alarmTone).getTitle(this));
			}
		} else { // existing alarm
			alarmDetails = dbHelper.getAlarm(id);

			timePicker.setCurrentMinute(alarmDetails.timeMinute);
			timePicker.setCurrentHour(alarmDetails.timeHour);

			chkSunday.setChecked(alarmDetails
					.getRepeatingDay(AlarmModel.SUNDAY));
			chkMonday.setChecked(alarmDetails
					.getRepeatingDay(AlarmModel.MONDAY));
			chkTuesday.setChecked(alarmDetails
					.getRepeatingDay(AlarmModel.TUESDAY));
			chkWednesday.setChecked(alarmDetails
					.getRepeatingDay(AlarmModel.WEDNESDAY));
			chkThursday.setChecked(alarmDetails
					.getRepeatingDay(AlarmModel.THURSDAY));
			chkFriday.setChecked(alarmDetails
					.getRepeatingDay(AlarmModel.FRDIAY));
			chkSaturday.setChecked(alarmDetails
					.getRepeatingDay(AlarmModel.SATURDAY));

			if (alarmDetails.alarmTone == null) {
				txtToneSelection.setText("Silent");
			} else {
				txtToneSelection.setText(RingtoneManager.getRingtone(this,
						alarmDetails.alarmTone).getTitle(this));
			}
		}

		final LinearLayout ringToneContainer = (LinearLayout) findViewById(R.id.alarm_ringtone_container);
		ringToneContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						RingtoneManager.ACTION_RINGTONE_PICKER);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
						RingtoneManager.TYPE_ALARM);
				startActivityForResult(intent, 1);
			}
		});
	}

	// Workaround for hour clearing on screen rotation
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putInt("alarmHour", timePicker.getCurrentHour());
		savedInstanceState.putInt("alarmMinute", timePicker.getCurrentMinute());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		int alarmHour = savedInstanceState.getInt("alarmHour");
		int alarmMinute = savedInstanceState.getInt("alarmMinute");

		if (timePicker != null) {
			timePicker.setCurrentHour(alarmHour);
			timePicker.setCurrentMinute(alarmMinute);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.alarm_details, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.action_save_alarm_details:
			updateModelFromLayout();

			AlarmManagerHelper.cancelAlarms(this);

			if (alarmDetails.id < 0) {
				dbHelper.createAlarm(alarmDetails);
			} else {
				dbHelper.updateAlarm(alarmDetails);
			}

			AlarmManagerHelper.setAlarms(this);

			setResult(RESULT_OK);
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1: {
				alarmDetails.alarmTone = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

				if (alarmDetails.alarmTone == null) {
					txtToneSelection.setText("Silent");
				} else {
					txtToneSelection.setText(RingtoneManager.getRingtone(this,
							alarmDetails.alarmTone).getTitle(this));
				}
				break;
			}
			default: {
				break;
			}
			}
		}
	}

	private void updateModelFromLayout() {
		alarmDetails.timeMinute = timePicker.getCurrentMinute().intValue();
		alarmDetails.timeHour = timePicker.getCurrentHour().intValue();
		alarmDetails.setRepeatingDay(AlarmModel.SUNDAY, chkSunday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.MONDAY, chkMonday.isChecked());
		alarmDetails
				.setRepeatingDay(AlarmModel.TUESDAY, chkTuesday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.WEDNESDAY,
				chkWednesday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.THURSDAY,
				chkThursday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.FRDIAY, chkFriday.isChecked());
		alarmDetails.setRepeatingDay(AlarmModel.SATURDAY,
				chkSaturday.isChecked());

		alarmDetails.repeatWeekly = chkSunday.isChecked()
				|| chkMonday.isChecked() || chkTuesday.isChecked()
				|| chkWednesday.isChecked() || chkThursday.isChecked()
				|| chkFriday.isChecked() || chkSaturday.isChecked();

		alarmDetails.isEnabled = true;
	}

}
