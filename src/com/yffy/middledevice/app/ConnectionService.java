package com.yffy.middledevice.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.yffy.middledevice.params.Params;
import com.yffy.middledevice.utils.NetUtils;
import com.yffy.middledevice.utils.TextUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ConnectionService extends Service {
	private final int START_CONNECTION = 11;
	private final int CHECK_INFORMATION = 15;
	private int action = START_CONNECTION;
	private Timer timer;
	

	@Override
	public void onCreate() {
		super.onCreate();
		timer = new Timer();
		timer.schedule(tt, 0, 500);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (timer == null) {
			timer = new Timer();
			timer.schedule(tt, 0, 500);
		}
		return Service.START_STICKY;
	}
	
	private ConcurrentHashMap<String, String> cc = new ConcurrentHashMap<String, String>();
	
	private TimerTask tt = new TimerTask() {
		@Override
		public void run() {
			if (action == START_CONNECTION) {
				cc.clear();
				cc.put(Params.Code.REQUEST,Params.Code.GET_CALLED_LIST);
				handleResult(NetUtils.doPost(Params.Url.GET_LIST_SERVLET_URL,cc));
				action = CHECK_INFORMATION;
			} else if (action == CHECK_INFORMATION) {
				cc.clear();
				cc.put(Params.Code.REQUEST,Params.Code.CALL_CUSTOMER);
				handleResult(NetUtils.doPost(Params.Url.CONTORL_SERVLET_URL,cc));
				action = START_CONNECTION;
			} 
		}
	};
	
	private void handleResult(String response){
		String content = null;
		String responseCode = TextUtils.json2String(response,Params.Code.RESPONSE,null);
		if (responseCode == null) return;
			
		if (responseCode.equals(Params.Code.CALL_CUSTOMER)) {
			content = TextUtils.json2String(response,Params.Code.MESSAGE, "");
			sendBroadcast(new Intent(Params.Code.CALL_CUSTOMER).putExtra(Params.Code.MESSAGE, content));
		
		} else if (responseCode.equals(Params.Code.GET_CALLED_LIST)) {
			String customer = TextUtils.json2String(response,Params.Customer.CUSTOMER,"");
			sendBroadcast(new Intent(Params.Code.GET_CALLED_LIST).putExtra(Params.Customer.CUSTOMER,customer));
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (tt!= null) {
			tt.cancel();
			tt = null;
		}
	}
	
}
