/**
 * Project Name:XPGSdkV4AppBase
 * File Name:MainControlActivity.java
 * Package Name:com.gizwits.aircondition.activity.control
 * Date:2015-1-27 14:44:17
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gizwits.powersocket.activity.control;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gizwits.framework.activity.BaseActivity;
import com.gizwits.framework.activity.account.UserManageActivity;
import com.gizwits.framework.activity.device.DeviceListActivity;
import com.gizwits.framework.activity.device.DeviceManageListActivity;
import com.gizwits.framework.activity.help.AboutActivity;
import com.gizwits.framework.activity.help.HelpActivity;
import com.gizwits.framework.adapter.MenuDeviceAdapter;
import com.gizwits.framework.config.JsonKeys;
import com.gizwits.framework.utils.DensityUtil;
import com.gizwits.framework.utils.DialogManager;
import com.gizwits.framework.widget.AboutVersionActivity;
import com.gizwits.framework.widget.SlidingMenu;
import com.gizwits.framework.widget.SlidingMenu.SlidingMenuListener;
import com.xpg.common.system.IntentUtils;
import com.xpg.common.useful.StringUtils;
import com.gizwits.powersocket.R;
import com.xtremeprog.xpgconnect.XPGWifiDevice;

// TODO: Auto-generated Javadoc
/**
 * Created by Lien on 14/12/21.
 * 
 * 设备主控界面
 * 
 * @author Lien
 */
public class MainControlActivity extends BaseActivity implements OnClickListener, SlidingMenuListener {

	/** The tag. */
	private final String TAG = "MainControlActivity";

	/** The m view. */
	private SlidingMenu mView;

	/** The iv menu. */
	private ImageView ivMenu;

	/** The tv Consumption. */
	private TextView tvConsumption;

	/** The tv Timing. */
	private TextView tvTiming;

	/** The tv Delay. */
	private TextView tvDelay;

	/** The btn Power. */
	private Button btnPower;

	/** The btn Appoinment. */
	private Button btnAppoinment;

	/** The linearLayout Timing. */
	private LinearLayout llTiming;

	/** The linearLayout Delay. */
	private LinearLayout llDelay;

	/** The m PowerOff dialog. */
	private Dialog mPowerOffDialog;

	/** The progress dialog. */
	private ProgressDialog progressDialogRefreshing;

	/** The disconnect dialog. */
	private Dialog mDisconnectDialog;

	/** 获取状态超时时间 */
	private int GetStatueTimeOut = 30000;

	/** 登陆设备超时时间 */
	private int LoginTimeOut = 5000;

	/** 是否超时标志位 */
	private boolean isTimeOut = false;

	/** The m adapter. */
	private MenuDeviceAdapter mAdapter;

	/** The lv device. */
	private ListView lvDevice;

	/**
	 * ClassName: Enum handler_key. <br/>
	 * <br/>
	 * date: 2014-11-26 17:51:10 <br/>
	 * 
	 * @author Lien
	 */
	private enum handler_key {

		/** 更新UI界面 */
		UPDATE_UI,

		/** 显示警告 */
		ALARM,

		/** 设备断开连接 */
		DISCONNECTED,

		/** 接收到设备的数据 */
		RECEIVED,

		/** 获取设备状态 */
		GET_STATUE,

		/** 获取设备状态超时 */
		GET_STATUE_TIMEOUT,

		/** The login start. */
		LOGIN_START,

		/**
		 * The login success.
		 */
		LOGIN_SUCCESS,

		/**
		 * The login fail.
		 */
		LOGIN_FAIL,

		/**
		 * The login timeout.
		 */
		LOGIN_TIMEOUT,
	}

	/**
	 * The handler.
	 */
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {

			case RECEIVED:
				if (deviceDataMap==null) {
					return;
				} else {
					for (String myKey : deviceDataMap.keySet()) {
						Log.e("Map", "key=" + myKey + ",value=" + deviceDataMap.get(myKey));
					}
					try {
						if (deviceDataMap.get("data") != null) {
							Log.i("info", (String) deviceDataMap.get("data"));
							inputDataToMaps(statuMap, (String) deviceDataMap.get("data"));
						}
						// 返回主线程处理P0数据刷新
						handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
						handler.sendEmptyMessage(handler_key.ALARM.ordinal());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			case UPDATE_UI:
				if (mView.isOpen())
					break;

				if (statuMap != null && statuMap.size() > 0) {
					handler.removeMessages(handler_key.GET_STATUE_TIMEOUT.ordinal());

					// 开关更新
					updatePower((Boolean) statuMap.get(JsonKeys.ON_OFF));
//					// 能耗更新
//					String consumption = (String) statuMap.get(JsonKeys.POWER_CONSUMPTION);
//					if (!StringUtils.isEmpty(consumption))
//						setConsumption(Integer.parseInt(consumption));
//					// 定时更新
//					boolean isTurnOn = (Boolean) statuMap.get(JsonKeys.TIME_ON_OFF);
//					String minOn = (String) statuMap.get(JsonKeys.TIME_ON_MINUTE);
//					String minOff = (String) statuMap.get(JsonKeys.TIME_OFF_MINUTE);
//					if (!StringUtils.isEmpty(minOn) && !StringUtils.isEmpty(minOff))
//						setTiming(isTurnOn, Integer.parseInt(minOn), Integer.parseInt(minOff));
//					// 延时更新
//					isTurnOn = (Boolean) statuMap.get(JsonKeys.COUNT_DOWN_ON_OFF);
//					String min = (String) statuMap.get(JsonKeys.COUNT_DOWN_MINUTE);
//					if (!StringUtils.isEmpty(min))
//						setDelay(isTurnOn, Integer.parseInt(min));

					DialogManager.dismissDialog(MainControlActivity.this, progressDialogRefreshing);
				}
				break;
			case ALARM:
				break;
			case DISCONNECTED:
				if (!mView.isOpen()) {
					DialogManager.dismissDialog(MainControlActivity.this, progressDialogRefreshing);
					DialogManager.dismissDialog(MainControlActivity.this, mPowerOffDialog);
					DialogManager.showDialog(MainControlActivity.this, mDisconnectDialog);
				}
				break;
			case GET_STATUE:
				mCenter.cGetStatus(mXpgWifiDevice);
				break;
			case GET_STATUE_TIMEOUT:
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			case LOGIN_SUCCESS:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				refreshMainControl();
				break;
			case LOGIN_FAIL:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			case LOGIN_TIMEOUT:
				isTimeOut = true;
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			default:
				break;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#onCreate(android.os.Bundle
	 * )
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_control);
		initViews();
		initEvents();
		initParams();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#onResume()
	 */
	@Override
	public void onResume() {
		if (mView.isOpen()) {
			refreshMenu();
		} else {
			if (!mDisconnectDialog.isShowing())
				refreshMainControl();
		}
		super.onResume();
	}

	/**
	 * 更新菜单界面.
	 * 
	 * @return void
	 */
	private void refreshMenu() {
		initBindList();
		mAdapter.setChoosedPos(-1);
		for (int i = 0; i < bindlist.size(); i++) {
			if (bindlist.get(i).getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
				mAdapter.setChoosedPos(i);
		}

		// 当前绑定列表没有当前操作设备
		if (mAdapter.getChoosedPos() == -1) {
			mAdapter.setChoosedPos(0);
			mXpgWifiDevice = mAdapter.getItem(0);
		}

		mAdapter.notifyDataSetChanged();

		int px = DensityUtil.dip2px(this, mAdapter.getCount() * 50);
		lvDevice.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, px));
	}

	/**
	 * 更新主控制界面.
	 * 
	 * @return void
	 */
	private void refreshMainControl() {
		mXpgWifiDevice.setListener(deviceListener);
		DialogManager.showDialog(this, progressDialogRefreshing);
		handler.sendEmptyMessageDelayed(handler_key.GET_STATUE_TIMEOUT.ordinal(), GetStatueTimeOut);
		handler.sendEmptyMessage(handler_key.GET_STATUE.ordinal());
	}

	/**
	 * Inits the params.
	 */
	private void initParams() {
		statuMap = new ConcurrentHashMap<String, Object>();

		refreshMenu();
		refreshMainControl();
	}

	/**
	 * Inits the views.
	 */
	private void initViews() {
		mView = (SlidingMenu) findViewById(R.id.main_layout);

		tvConsumption = (TextView) findViewById(R.id.tvConsumption);
		tvTiming = (TextView) findViewById(R.id.tvTiming);
		tvDelay = (TextView) findViewById(R.id.tvDelay);
		ivMenu = (ImageView) findViewById(R.id.ivMenu);
		btnPower = (Button) findViewById(R.id.btnPower);
		btnAppoinment = (Button) findViewById(R.id.btnAppoinment);
		llTiming = (LinearLayout) findViewById(R.id.llTiming);
		llDelay = (LinearLayout) findViewById(R.id.llDelay);

		mAdapter = new MenuDeviceAdapter(this, bindlist);
		lvDevice = (ListView) findViewById(R.id.lvDevice);
		lvDevice.setAdapter(mAdapter);

		progressDialogRefreshing = new ProgressDialog(MainControlActivity.this);
		progressDialogRefreshing.setMessage("正在更新状态,请稍后。");
		progressDialogRefreshing.setCancelable(false);

		mDisconnectDialog = DialogManager.getDisconnectDialog(this, new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogManager.dismissDialog(MainControlActivity.this, mDisconnectDialog);
				IntentUtils.getInstance().startActivity(MainControlActivity.this, DeviceListActivity.class);
				finish();
			}
		});
	}

	/**
	 * Inits the events.
	 */
	private void initEvents() {
		btnPower.setOnClickListener(this);
		ivMenu.setOnClickListener(this);
		btnAppoinment.setOnClickListener(this);

		lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!mAdapter.getItem(position).isOnline())
					return;

				if (mAdapter.getChoosedPos() != position) {
					mAdapter.setChoosedPos(position);
					mXpgWifiDevice = bindlist.get(position);
				}

				mView.toggle();
			}
		});
		mView.setSlidingMenuListener(this);
	}

	/**
	 * 电源开关切换.
	 * 
	 * @param isOn
	 *            the is on
	 */
	private void updatePower(boolean isPower) {
		if (isPower) {
			btnPower.setSelected(true);
		} else {
			btnPower.setSelected(false);
		}
	}

	/**
	 * 能耗显示.
	 * 
	 * @param num
	 *            the power consumption
	 */
	private void setConsumption(int num) {
		tvConsumption.setText(num + "度");
	}

	/**
	 * 设置预约栏显示与隐藏,预约时间的设置.
	 * 
	 * @param isTurnOn
	 *            is turn on the appoinment
	 * @param on
	 *            the time start
	 * @param off
	 *            the time end
	 * @true 显示
	 * @false 隐藏
	 */
	private void setTiming(boolean isTurnOn, int on, int off) {
		// if (isTurnOn) {
		// llTiming.setVisibility(View.VISIBLE);
		// } else {
		// llTiming.setVisibility(View.INVISIBLE);
		// }
		int minOn = on % 60;
		int hourOn = on / 60;
		int minOff = off % 60;
		int hourOff = off / 60;
		tvTiming.setText(String.format("%02d:%02d-%02d:%02d", hourOn, minOn, hourOff, minOff));
	}

	/**
	 * 设置延时栏显示与隐藏,延时时间的设置.
	 * 
	 * @param isTurnOn
	 *            is turn on the appoinment
	 * @param on
	 *            the time delay
	 * @true 显示
	 * @false 隐藏
	 */
	private void setDelay(boolean isTurnOn, int on) {
		// if (isTurnOn) {
		// llDelay.setVisibility(View.VISIBLE);
		// } else {
		// llDelay.setVisibility(View.INVISIBLE);
		// }
		int min = on % 60;
		int hour = on / 60;
		tvDelay.setText(String.format("%02d:%02d", hour, min));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (mView.isOpen()) {
			return;
		}

		switch (v.getId()) {
		case R.id.btnPower:
			mCenter.cPowerOn(mXpgWifiDevice, !btnPower.isSelected());
			updatePower(!btnPower.isSelected());
			break;
		case R.id.ivMenu:
			mView.toggle();
			break;
		case R.id.btnAppoinment:
			startActivity(new Intent(MainControlActivity.this, AppointmentActivity.class));
			break;
		}
	}

	public void onClickSlipBar(View view) {
		switch (view.getId()) {
		case R.id.rlDevice:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, DeviceManageListActivity.class);
			break;
		case R.id.rlAbout:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, AboutActivity.class);
			break;
		case R.id.rlAbout_Demo:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, AboutVersionActivity.class);
			break;
		case R.id.rlAccount:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, UserManageActivity.class);
			break;
		case R.id.rlHelp:
			IntentUtils.getInstance().startActivity(MainControlActivity.this, HelpActivity.class);
			break;
		case R.id.rlDeviceList:
			mCenter.cDisconnect(mXpgWifiDevice);
			DisconnectOtherDevice();
			IntentUtils.getInstance().startActivity(MainControlActivity.this, DeviceListActivity.class);
			finish();
			break;
		}
	}

	/**
	 * Login device.
	 * 
	 * @param xpgWifiDevice
	 *            the xpg wifi device
	 */
	private void loginDevice(XPGWifiDevice xpgWifiDevice) {
		mXpgWifiDevice = xpgWifiDevice;
		mXpgWifiDevice.setListener(deviceListener);
		mXpgWifiDevice.login(setmanager.getUid(), setmanager.getToken());
		isTimeOut = false;
		handler.sendEmptyMessageDelayed(handler_key.LOGIN_TIMEOUT.ordinal(), LoginTimeOut);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.framework.activity.BaseActivity#didLogin(com.xtremeprog.
	 * xpgconnect.XPGWifiDevice, int)
	 */
	@Override
	protected void didLogin(XPGWifiDevice device, int result) {
		if (isTimeOut)
			return;

		if (result == 0) {
			handler.sendEmptyMessage(handler_key.LOGIN_SUCCESS.ordinal());
		} else {
			handler.sendEmptyMessage(handler_key.LOGIN_FAIL.ordinal());
		}

	}

	/**
	 * 检查出了选中device，其他device有没有连接上
	 * 
	 * @param mac
	 *            the mac
	 * @param did
	 *            the did
	 * @return the XPG wifi device
	 */
	private void DisconnectOtherDevice() {
		for (XPGWifiDevice theDevice : bindlist) {
			if (theDevice.isConnected() && !theDevice.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
				mCenter.cDisconnect(theDevice);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#didReceiveData(com.
	 * xtremeprog .xpgconnect.XPGWifiDevice,
	 * java.util.concurrent.ConcurrentHashMap, int)
	 */
	@Override
	protected void didReceiveData(XPGWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int result) {
		Log.e("didReceiveData", "did=" + device.getDid());

		if (!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
			return;

		deviceDataMap = dataMap;
		handler.sendEmptyMessage(handler_key.RECEIVED.ordinal());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (mView.isOpen()) {
			mView.toggle();
		} else {
			if (mXpgWifiDevice != null && mXpgWifiDevice.isConnected()) {
				mCenter.cDisconnect(mXpgWifiDevice);
				DisconnectOtherDevice();
			}
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#didDisconnected(com.
	 * xtremeprog .xpgconnect.XPGWifiDevice)
	 */
	@Override
	protected void didDisconnected(XPGWifiDevice device) {
		if (!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
			return;

		handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
	}

	/**
	 * 菜单界面返回主控界面.
	 * 
	 * @return void
	 */
	private void backToMain() {
		mXpgWifiDevice = mAdapter.getItem(mAdapter.getChoosedPos());

		if (!mXpgWifiDevice.isConnected()) {
			loginDevice(mXpgWifiDevice);
			DialogManager.showDialog(this, progressDialogRefreshing);
		}

		refreshMainControl();
	}

	@Override
	public void OpenFinish() {

	}

	@Override
	public void CloseFinish() {
		backToMain();
	}

}
