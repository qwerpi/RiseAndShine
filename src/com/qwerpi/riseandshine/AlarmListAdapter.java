package com.qwerpi.riseandshine;

import java.util.List;

import android.content.Context;
import android.support.v7.view.ActionMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class AlarmListAdapter extends BaseAdapter {

	private Context mContext;
	private List<AlarmModel> mAlarms;

	public AlarmListAdapter(Context context, List<AlarmModel> alarms) {
		mContext = context;
		mAlarms = alarms;
	}

	public void setAlarms(List<AlarmModel> alarms) {
		mAlarms = alarms;
	}

	@Override
	public int getCount() {
		if (mAlarms != null) {
			return mAlarms.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mAlarms != null) {
			return mAlarms.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mAlarms != null) {
			return mAlarms.get(position).id;
		}
		return 0;
	}

	// TODO handle view recycling
	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		// if (view == null) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.alarm_list_item, parent, false);
		// }

		AlarmModel model = (AlarmModel) getItem(position);

		TextView txtTime = (TextView) view.findViewById(R.id.alarm_item_time);
		TextView txtRepeat = (TextView) view
				.findViewById(R.id.alarm_item_repeat);
		CompoundButton btnEnabled = (CompoundButton) view
				.findViewById(R.id.alarm_item_toggle);

		String time = String.format("%02d : %02d <small>%s</small>",
				(model.timeHour % 12) + (model.timeHour % 12 == 0 ? 12 : 0),
				model.timeMinute, (model.timeHour >= 12 ? "PM" : "AM"));
		// txtTime.setText(String.format("%02d : %02d", model.timeHour,
		// model.timeMinute));
		txtTime.setText(Html.fromHtml(time));
		String html = String.format("<font color='%s'>S</font> "
				+ "<font color='%s'>M</font> " + "<font color='%s'>T</font> "
				+ "<font color='%s'>W</font> " + "<font color='%s'>T</font> "
				+ "<font color='%s'>F</font> " + "<font color='%s'>S</font> ",
				model.getRepeatingDay(0) ? "0x33B5E5" : "0x888888",
				model.getRepeatingDay(1) ? "0x33B5E5" : "0x888888",
				model.getRepeatingDay(2) ? "0x33B5E5" : "0x888888",
				model.getRepeatingDay(3) ? "0x33B5E5" : "0x888888",
				model.getRepeatingDay(4) ? "0x33B5E5" : "0x888888",
				model.getRepeatingDay(5) ? "0x33B5E5" : "0x888888",
				model.getRepeatingDay(6) ? "0x33B5E5" : "0x888888");
		if (model.repeatWeekly)
			txtRepeat.setText(Html.fromHtml(html),
					TextView.BufferType.SPANNABLE);
		else
			txtRepeat.setText("");

		btnEnabled.setChecked(model.isEnabled);
		btnEnabled.setTag(model.id);
		btnEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				((MainActivity) mContext).setAlarmEnabled((Long) btn.getTag(),
						checked);
				((AlarmModel) getItem(position)).isEnabled = checked;
			}
		});

		view.setTag(model.id);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				((MainActivity) mContext).startAlarmDetailsActivity((Long) view
						.getTag());
			}
		});
		
		view.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				((MainActivity) mContext).deleteAlarm((Long) view.getTag());
				return true;
			}
		});

		return view;
	}

}
