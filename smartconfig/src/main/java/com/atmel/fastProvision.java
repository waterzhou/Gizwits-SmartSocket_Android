package com.atmel;

import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by water.zhou on 3/1/2016.
 */
public class fastProvision {
    private final String TAG = "FastProvision";
    private static final int SM_PORT = 28100;
    private static Thread wifiConfigThread = null;
    private static int wifiConfigInterval = 500;
    private static String ip;
    private UDPSocketClient mSocketClient = null;
    private AtomicBoolean isFastConfigDone = new AtomicBoolean(false);
    private AtomicBoolean isStopSending = new AtomicBoolean(false);

    public fastProvision() {
        this.mSocketClient = new UDPSocketClient();
    }

    private void fastProvisionCheckLoop (){
        (new Thread() {
            public void run() {
                Log.d(TAG, "Check loop start");

            }
        }).start();
    }

    private void fastProvisionExecuteLoop (final String ssid, final String psw){
        (new Thread() {
            public void run() {
                int i;
                int datalength;
                byte[] ucSSID = {0};
                byte[] ucPSW = {0};
                try {
                    ucSSID = ssid.getBytes("UTF-8");
                    ucPSW = psw.getBytes("UTF-8");
                }catch(UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                int ucSSIDLen = ssid.length();
                int ucPSWLen = psw.length();
                char ucSynIndex;

                Log.d(TAG, "Execute loop start");
                while (!isStopSending.get()) {
                    ucSynIndex = 1;
                    // first send five id packets
                    for (i=0; i<5; i++)
                    {
                        ip = "239.120.1.0";
                        datalength = 1;
                        mSocketClient.sendData(datalength,ip,SM_PORT,10);
                        ucSynIndex++;
                    }
                    // 发送长度
                    ip = "239.120." + (ucSynIndex & 255) + "." + (ucSSIDLen & 255);
                    datalength = 2;
                    mSocketClient.sendData(datalength,ip,SM_PORT,10);
                    ucSynIndex++;

                    ip = "239.120." + (ucSynIndex & 255) + "." + (ucPSWLen & 255);
                    datalength = 3;
                    mSocketClient.sendData(datalength, ip, SM_PORT, 10);
                    ucSynIndex++;

                    // 发送SSID
                    for (i=0; i<ucSSIDLen; i++)
                    {
                        ip = "239.120." + (ucSynIndex & 255) + "." + (ucSSID[i] & 255);
                        datalength = 4;
                        mSocketClient.sendData(datalength, ip, SM_PORT, 10);
                        ucSynIndex++;
                    }

                    // 发送密码
                    for (i=0; i<ucPSWLen; i++)
                    {
                        ip = "239.120." + (ucSynIndex & 255) + "." + (ucPSW[i] & 255);
                        datalength = 5;
                        mSocketClient.sendData(datalength, ip, SM_PORT, 10);
                        ucSynIndex++;
                    }

                }
            }
        }).start();
    }

    public synchronized void startFastProvision( Context context, String ssid, String psw,int timeout) {
        int count = 0;
        fastProvisionCheckLoop();
        fastProvisionExecuteLoop (ssid, psw);

        try {
            while ( count < 120 && (!isFastConfigDone.get()))
            {
                Thread.sleep(500);
                count ++;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        isFastConfigDone.set(true);
    }
}
