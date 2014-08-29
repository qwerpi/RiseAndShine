package com.qwerpi.riseandshine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AlarmManagerHelper.setAlarms(this);
		return super.onStartCommand(intent, flags, startId);
	}

}
