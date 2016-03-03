package com.atmel;

import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by water.zhou on 3/1/2016.
 */
public class fastProvision {
    private final String TAG = "FastProvision";
    private static final int SM_PORT = 28100;
    private static final int RM_PORT = 28105;
    private static Thread wifiConfigThread = null;
    private static int wifiConfigInterval = 500;
    private static String ip;
    private UDPSocketClient mSocketClient = null;
    private AtomicBoolean isFastConfigDone = new AtomicBoolean(false);
    private AtomicBoolean isStopSending = new AtomicBoolean(false);

    public fastProvision() {
        this.mSocketClient = new UDPSocketClient();
    }

//    private void fastProvisionCheckLoop (){
//        (new Thread() {
//            public void run() {
//                int count = 0;
//                Log.d(TAG, "Check loop start");
//                while (!isFastConfigDone.get())
//                {
//                    try {
//                        Thread.sleep(500);
//                    } catch (Exception e) {
//                        Log.e(TAG, e.getMessage());
//                    }
//                    count ++;
//                }
//            }
//        }).start();
//    }

    private void fastProvisionExecuteLoop (final String ssid, final String psw){
        (new Thread() {
            public void run() {
                int i;
                int count = 0;
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
                Random random =  new Random();

                Log.d(TAG, "Execute loop start");
                while (!isFastConfigDone.get()) {
                    ucSynIndex = 1;
                    // first send five id packets
                    for (i=0; i<5; i++)
                    {
                        ip = "239.120.1.0";
                        datalength = 1;
                        mSocketClient.sendData(datalength,ip,SM_PORT,10);
                        ucSynIndex++;
                    }
                    // send length
                    datalength = random.nextInt(99-10) + 10;
                    ip = "239."+ ((ucPSWLen + datalength/10) & 255) + "." + (ucSynIndex & 255) + "." + ((ucSSIDLen + datalength%10)& 255);
                    Log.d(TAG, "======================\n" + ip);
                    mSocketClient.sendData(datalength,ip,SM_PORT,10);
                    ucSynIndex++;
                    // send SSID
                    for (i=0; i<ucSSIDLen;)
                    {
                        datalength = random.nextInt(99-10) + 10;
                        if( i + 2 > ucSSIDLen)
                            ip = "239." + ((ucSSID[i] + datalength/10) & 255) + "." + (ucSynIndex & 255) + "." + ((0 + datalength%10)& 255);
                        else
                            ip = "239." + ((ucSSID[i] + datalength/10) & 255) + "." + (ucSynIndex & 255) + "." + ((ucSSID[i + 1] + datalength%10) & 255);
                        i += 2;
                        Log.d(TAG, "send SSID=" + ip);
                        mSocketClient.sendData(datalength, ip, SM_PORT, 10);
                        ucSynIndex++;
                    }
                    // send pwd
                    for (i=0; i<ucPSWLen;)
                    {
                        datalength = random.nextInt(99-10) + 10;
                        if (i + 2 > ucPSWLen)
                            ip = "239." + ((ucPSW[i] + datalength/10 )& 255) +"."+ (ucSynIndex & 255) + "." + ((0 + datalength%10)& 255);
                        else
                            ip = "239." + ((ucPSW[i] + datalength/10 ) & 255) +"."+ (ucSynIndex & 255) + "." + ((ucPSW[i+1] + datalength%10) & 255);
                        i += 2;
                        Log.d(TAG, "send pwd=" + ip);
                        mSocketClient.sendData(datalength, ip, SM_PORT, 10);
                        ucSynIndex++;
                    }
               }
            }
        }).start();
    }

    public synchronized void startFastProvision( Context context, String ssid, String psw,int timeout) {
        //fastProvisionCheckLoop();
        fastProvisionExecuteLoop(ssid, psw);
    }

    public synchronized void stopFastProvision() {
        isFastConfigDone.set(true);
        mSocketClient.interrupt();
        mSocketClient.close();
    }

}
