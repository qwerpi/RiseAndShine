package com.qwerpi.riseandshine;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private AlarmListAdapter mAdapter;
	private AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	private ListView lv;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_main);

		lv = (ListView) findViewById(android.R.id.list);

		mAdapter = new AlarmListAdapter(this, dbHelper.getAlarms());
		lv.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_add_new_alarm:
			startAlarmDetailsActivity(-1);
			break;
		case R.id.action_settings:
			// Toast.makeText(this, "TODO: Open settings here",
			// Toast.LENGTH_SHORT).show();
			
			// for testing life cycle of AlarmScreen
			Intent alarmIntent = new Intent(mContext, AlarmScreen.class);
			alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			alarmIntent.putExtra("id", -1);
			alarmIntent.putExtra("timeHour", 0);
			alarmIntent.putExtra("timeMinute", 0);
			alarmIntent.putExtra("alarmTone", RingtoneManager
					.getActualDefaultRingtoneUri(this,
							RingtoneManager.TYPE_ALARM));
			mContext.startActivity(alarmIntent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			mAdapter.setAlarms(dbHelper.getAlarms());
			mAdapter.notifyDataSetChanged();
		}
	}

	public void setAlarmEnabled(long id, boolean isEnabled) {
		AlarmManagerHelper.cancelAlarms(this);

		AlarmModel model = dbHelper.getAlarm(id);
		model.isEnabled = isEnabled;
		dbHelper.updateAlarm(model);

		AlarmManagerHelper.setAlarms(this);
	}

	public void startAlarmDetailsActivity(long id) {
		Intent intent = new Intent(this, AlarmDetailsActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent, 0);
	}

	public void deleteAlarm(Long id) {
		final long alarmId = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please confirm").setTitle("Delete set?")
				.setCancelable(true).setNegativeButton("Cancel", null)
				.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Cancel Alarms
						AlarmManagerHelper.cancelAlarms(mContext);
						// Delete alarm from DB by id
						dbHelper.deleteAlarm(alarmId);
						// Refresh the list of the alarms in the adaptor
						mAdapter.setAlarms(dbHelper.getAlarms());
						// Notify the adapter the data has changed
						mAdapter.notifyDataSetChanged();
						// Set the alarms
						AlarmManagerHelper.setAlarms(mContext);
					}
				}).show();
	}

}
