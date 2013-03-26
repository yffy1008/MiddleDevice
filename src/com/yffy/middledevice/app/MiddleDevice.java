package com.yffy.middledevice.app;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;

import com.yffy.middledevice.R;
import com.yffy.middledevice.data.CalledAdapter;
import com.yffy.middledevice.data.Customer;
import com.yffy.middledevice.params.Params;
import com.yffy.middledevice.utils.FileUtils;
import com.yffy.middledevice.utils.TextUtils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

public class MiddleDevice extends Activity {
	private int currentIndex = 100;
	
	private VideoView vv;
	private TextView ads_tv;
	
	private MediaPlayer mp;
	
	private ChangeVideoBroadCastReceiver cVideoBroadCastReceiver;
	
	private ListView called__list;
	private ArrayList<Customer> called_alist;
	private CalledAdapter mCalledAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 设置水平
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
	
		setContentView(R.layout.mainlayout);
		
		initalWidget();
	}

	@Override
	protected void onStart() {
		super.onStart();
		startPlay(currentIndex + "");
		startService(getServiceIntent());
	}

	private Intent getServiceIntent(){
		return new Intent(MiddleDevice.this,ConnectionService.class);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(cVideoBroadCastReceiver, getIntentFilter());
	}
	
	private IntentFilter getIntentFilter(){
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Params.Code.CALL_CUSTOMER);
		mIntentFilter.addAction(Params.Code.GET_CALLED_LIST);
		return mIntentFilter;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(cVideoBroadCastReceiver);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		vv.stopPlayback();
		if (mp != null) {
			mp.release();
			mp = null;
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(getServiceIntent());
	}

	// 播放控制
	private void startPlay(String number) {
			try {
				String path = FileUtils.getFilePath(number);
				if (!new File(path).exists()) throw new Exception();
				vv.setVideoPath(path);
				vv.start();
			} catch (Exception e) {
				currentIndex = 1;
				vv.setVideoPath(Params.Video.HOME_PATH + "007.rmvb"
//				+ Params.Video.POSTFIX
				);
				vv.start();
				TextUtils.showOnScreen(MiddleDevice.this,"here is start");
			}
	}

	private void playErro(String filePath) {
		try {
			mp.reset();
			mp.setDataSource(filePath);
//			mp.setDataSource(Params.Video.HOME_PATH + "erro.mp3");
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.prepare();
			mp.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TextUtils.showOnScreen(MiddleDevice.this,"输入错误");
	}

	
	private class ChangeVideoBroadCastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent it) {
			String action = it.getAction();
			String content = null;
			if (action.equals(Params.Code.CALL_CUSTOMER)) {
				content = it.getStringExtra(Params.Code.MESSAGE);
				ads_tv.setText(content);
			
			} else if (action.equals(Params.Code.GET_CALLED_LIST)) {
				content = it.getStringExtra(Params.Customer.CUSTOMER);
				refreshCalledList(content);
			}
		}
	}
	
	private void refreshCalledList(String jsonString){
		try {
			JSONArray ja = new JSONArray(jsonString);
			called_alist.clear();
			for (int i = 0; i < ja.length(); i++)called_alist.add(new Customer(ja.getString(i)));
			mCalledAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initalWidget() {
		vv = (VideoView) findViewById(R.id.vv);
		vv.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				currentIndex++;
				vv.stopPlayback();
				startPlay(currentIndex+"");
			}
		});

		mp = new MediaPlayer();
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp1) {
				mp.release();
			}
		});

		ads_tv = (TextView) findViewById(R.id.text);
		
		cVideoBroadCastReceiver = new ChangeVideoBroadCastReceiver();
		
		called__list = (ListView)findViewById(R.id.called_list);
		called_alist = new ArrayList<Customer>();
		mCalledAdapter = new CalledAdapter(called_alist, MiddleDevice.this);
		called__list.setAdapter(mCalledAdapter);
	}
	
}
