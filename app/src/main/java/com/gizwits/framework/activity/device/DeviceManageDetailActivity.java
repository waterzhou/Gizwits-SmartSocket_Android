/**
 * Project Name:XPGSdkV4AppBase
 * File Name:DeviceManageDetailActivity.java
 * Package Name:com.gizwits.framework.activity.device
 * Date:2015-1-27 14:45:23
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
package com.gizwits.framework.activity.device;

import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gizwits.framework.activity.BaseActivity;
import com.gizwits.framework.adapter.ManageDetailsAdapter;
import com.gizwits.framework.config.DeviceDetails;
import com.gizwits.framework.utils.DialogManager;
import com.gizwits.powersocket.R;
import com.xpg.common.useful.NetworkUtils;
import com.xpg.common.useful.StringUtils;
import com.xpg.ui.utils.ToastUtils;
import com.xtremeprog.xpgconnect.XPGWifiDevice;

//TODO: Auto-generated Javadoc
/**
 * 
 * ClassName: Class DeviceManageDetailActivity. <br/>
 * 设备详细信息，该类主要有以下功能：1、修改当前设备名称；2、删除与当前设备的绑定关系<br/>
 * date: 2014-12-09 17:27:10 <br/>
 * 
 * @author StephenC
 */
public class DeviceManageDetailActivity extends BaseActivity implements OnClickListener {

	/** The iv back. */
	private ImageView ivBack;

	/** The iv tick. */
	private ImageView ivTick;

	/** The gv gvDetails. */
	private GridView gvDetails;

	/** The adapter ManageDetailsAdapter. */
	private ManageDetailsAdapter mManageDetailsAdapter;

	/** The RelativeLayout rlDetailsChoosing. */
	private RelativeLayout rlDetailsChoosing;

	/** The RelativeLayout rlDetails. */
	private RelativeLayout rlDetails;

	/** The ImageView ivDetails. */
	private ImageView ivDetails;

	/** The et device Name. */
	private EditText etName;

	/** The btn delDevice. */
	private Button btnDelDevice;

	/** 当前设备的实例 */
	private XPGWifiDevice xpgWifiDevice;

	/** 确定是否解绑的对话框 */
	private Dialog unbindDialog;

	/** 等待中的对话框 */
	private ProgressDialog progressDialog;

	private boolean isChange = true;

	/**
	 * ClassName: Enum handler_key. <br/>
	 * <br/>
	 * date: 2014-11-26 17:51:10 <br/>
	 * 
	 * @author Lien
	 */
	private enum handler_key {

		/** 修改名称成功 */
		CHANGE_SUCCESS,

		/** 修改名称失败 */
		CHANGE_FAIL,

		/** 删除绑定关系成功 */
		DELETE_SUCCESS,

		/** 删除绑定关系失败 */
		DELETE_FAIL,

		/** 获取绑定列表 */
		GET_BOUND,

		/** 获取设备图片 */
		GET_Details,

	}

	/** The handler. */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];

			switch (key) {
			case CHANGE_SUCCESS:
				DialogManager.dismissDialog(DeviceManageDetailActivity.this, progressDialog);
				ToastUtils.showShort(DeviceManageDetailActivity.this, "修改成功！");
				finish();
				break;

			case CHANGE_FAIL:
				DialogManager.dismissDialog(DeviceManageDetailActivity.this, progressDialog);
				ToastUtils.showShort(DeviceManageDetailActivity.this, "修改失败:" + msg.obj.toString());
				break;

			case DELETE_SUCCESS:
				DialogManager.dismissDialog(DeviceManageDetailActivity.this, progressDialog);
				ToastUtils.showShort(DeviceManageDetailActivity.this, "删除成功！");
				finish();
				break;

			case DELETE_FAIL:
				DialogManager.dismissDialog(DeviceManageDetailActivity.this, progressDialog);
				ToastUtils.showShort(DeviceManageDetailActivity.this, "删除失败:" + msg.obj.toString());
				break;

			case GET_BOUND:
				String uid = setmanager.getUid();
				String token = setmanager.getToken();
				mCenter.cGetBoundDevices(uid, token);
				break;
			case GET_Details:
				rlDetailsChoosing.setVisibility(View.GONE);
				ivTick.setVisibility(View.VISIBLE);
				int icon = msg.arg1 + 1;
				ivDetails.setImageResource(DeviceDetails.findByNum(icon).getResList());
				setmanager.setResByMacAndDid(xpgWifiDevice.getMacAddress(), xpgWifiDevice.getDid(), icon);
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices_info);
		initParams();
		initViews();
		initEvents();

	}

	/**
	 * Inits the params.
	 */
	private void initParams() {
		if (getIntent() != null) {
			String mac = getIntent().getStringExtra("mac");
			String did = getIntent().getStringExtra("did");
			xpgWifiDevice = findDeviceByMac(mac, did);
			xpgWifiDevice.setListener(deviceListener);
		}

	}

	/**
	 * Inits the views.
	 */
	private void initViews() {
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivTick = (ImageView) findViewById(R.id.ivTick);
		ivDetails = (ImageView) findViewById(R.id.ivDetails);
		ivDetails.setImageResource(setmanager.getResbyMacAndDid(xpgWifiDevice.getMacAddress(), xpgWifiDevice.getDid()));
		rlDetailsChoosing = (RelativeLayout) findViewById(R.id.rlDetailsChoosing);
		rlDetails = (RelativeLayout) findViewById(R.id.rlDetails);
		gvDetails = (GridView) findViewById(R.id.gvDetails);
		gvDetails.setSelector(R.color.transparent);
		mManageDetailsAdapter = new ManageDetailsAdapter(this);
		mManageDetailsAdapter
				.setSelected(setmanager.getNumbyMacAndDid(xpgWifiDevice.getMacAddress(), xpgWifiDevice.getDid()) - 1);
		gvDetails.setAdapter(mManageDetailsAdapter);
		gvDetails.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mManageDetailsAdapter.setSelected(arg2);
				mManageDetailsAdapter.notifyDataSetChanged();
				Message msg = handler.obtainMessage(handler_key.GET_Details.ordinal(), arg2, 0);
				handler.sendMessageDelayed(msg, 380);
			}
		});
		etName = (EditText) findViewById(R.id.etName);
		btnDelDevice = (Button) findViewById(R.id.btnDelDevice);
		unbindDialog = DialogManager.getUnbindDialog(this, this);
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		if (xpgWifiDevice != null) {
			if (StringUtils.isEmpty(xpgWifiDevice.getRemark())) {
				String macAddress = xpgWifiDevice.getMacAddress();
				int size = macAddress.length();
				etName.setText(xpgWifiDevice.getProductName() + macAddress.substring(size - 4, size));
			} else {
				etName.setText(xpgWifiDevice.getRemark());
			}

		}
	}

	/**
	 * Inits the events.
	 */
	private void initEvents() {
		btnDelDevice.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		ivTick.setOnClickListener(this);
		rlDetails.setOnClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBack:
			onBackPressed();
			break;
		case R.id.btnDelDevice:
			DialogManager.showDialog(this, unbindDialog);

			break;
		case R.id.ivTick:
			if (!NetworkUtils.isNetworkConnected(this)) {
				ToastUtils.showShort(this, "网络未连接");
				return;
			}
			if (!StringUtils.isEmpty(etName.getText().toString())) {
				isChange = true;
				progressDialog.setMessage("修改中，请稍候...");
				DialogManager.showDialog(this, progressDialog);
				mCenter.cUpdateRemark(setmanager.getUid(), setmanager.getToken(), xpgWifiDevice.getDid(),
						xpgWifiDevice.getPasscode(), etName.getText().toString());
			} else {
				ToastUtils.showShort(DeviceManageDetailActivity.this, "请输入一个设备名称");
			}
			break;
		case R.id.right_btn:
			if (!NetworkUtils.isNetworkConnected(this)) {
				ToastUtils.showShort(this, "网络未连接");
				return;
			}
			isChange = false;
			DialogManager.dismissDialog(this, unbindDialog);
			progressDialog.setMessage("删除中，请稍候...");
			DialogManager.showDialog(this, progressDialog);
			mCenter.cUnbindDevice(setmanager.getUid(), setmanager.getToken(), xpgWifiDevice.getDid(),
					xpgWifiDevice.getPasscode());
			break;
		case R.id.rlDetails:
			rlDetailsChoosing.setVisibility(View.VISIBLE);
			ivTick.setVisibility(View.GONE);
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (rlDetailsChoosing.getVisibility() == View.VISIBLE)
			rlDetailsChoosing.setVisibility(View.GONE);
		else
			finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.framework.activity.BaseActivity#didBindDevice(int,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	protected void didBindDevice(int error, String errorMessage, String did) {
		Log.d("Device扫描结果", "error=" + error + ";errorMessage=" + errorMessage + ";did=" + did);
		Message msg = new Message();
		if (error == 0) {
			msg.what = handler_key.GET_BOUND.ordinal();
			handler.sendMessage(msg);
		} else {
			msg.what = handler_key.CHANGE_FAIL.ordinal();
			msg.obj = errorMessage;
			handler.sendMessage(msg);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.framework.activity.BaseActivity#didUnbindDevice(int,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	protected void didUnbindDevice(int error, String errorMessage, String did) {
		Message msg = new Message();
		if (error == 0) {
			msg.what = handler_key.GET_BOUND.ordinal();
			handler.sendMessage(msg);
		} else {
			msg.what = handler_key.DELETE_FAIL.ordinal();
			msg.obj = errorMessage;
			handler.sendMessage(msg);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.framework.activity.BaseActivity#didDiscovered()
	 */
	@Override
	protected void didDiscovered(int error, List<XPGWifiDevice> deviceList) {
		Log.d("onDiscovered", "Device count:" + deviceList.size());
		deviceslist = deviceList;

		Message msg = new Message();
		if (msg != null) {
			msg.what = isChange ? handler_key.CHANGE_SUCCESS.ordinal() : handler_key.DELETE_SUCCESS.ordinal();
			handler.sendMessageDelayed(msg, 1500);
			msg = null;
		}
	}

}
