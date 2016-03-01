package com.atmel;

import android.content.Context;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * Created by water.zhou on 3/1/2016.
 */
public class AtmelSmartconfig {
    private String TAG;
    private fastProvision mfastProvision;
    private boolean isSmartconfigGoing = false;

    private AtmelSmartconfig() {
        this.TAG = "AtmelSmartConfig";
        this.mfastProvision = null;
    }

    public static AtmelSmartconfig getInstance() {
        return AtmelSmartconfig.InstanceHolder.instance;
    }
    public synchronized void startAtmelsmartconfig(Context context, String ssid, String password, int timeout) {
        if(this.isSmartconfigGoing) {
            Log.w(this.TAG, "Atmelsmartconfig start(): one task is running, so stop it before start a new one");
            this.stopAtmelsmartconfig();
        }

        try {
            Log.d(TAG, "Trigger atmel smart config....................");
            mfastProvision = new fastProvision();
            mfastProvision.startFastProvision(context,ssid,password,timeout);
            this.isSmartconfigGoing = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public synchronized void stopAtmelsmartconfig() {
//        if(this.mEasylinkPlus != null && this.isSmartconfigGoing) {
//            Log.d(TAG, "Stop Atmel smart config................");
//            this.mEasylinkPlus = EasyLink_plus.getInstence(this.ctx);
//            this.mEasylinkPlus.stopTransmitting();
//            this.isSmartconfigGoing = false;
//        } else {
//            Log.d(TAG, "Atmel smart config is already stopped........");
//        }
    }

    private static class InstanceHolder {
        static AtmelSmartconfig instance = new AtmelSmartconfig();
        private InstanceHolder() {
        }
    }
}
