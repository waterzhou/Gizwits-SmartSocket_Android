package com.atmel;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by water.zhou on 3/1/2016.
 */
public class UDPSocketClient {

    private static final String TAG = "UDPSocketClient";
    private DatagramSocket mSocket;
    private volatile boolean mIsStop;
    private volatile boolean mIsClosed;
    private int mBufferBoundary;

    public UDPSocketClient() {
        try {
            this.mSocket = new DatagramSocket();
            this.mIsStop = false;
            this.mIsClosed = false;
            this.mBufferBoundary = 256;
        } catch (SocketException e) {
            Log.e(TAG, "SocketException");
            e.printStackTrace();
        }
    }

    public void setUDPSocketDataBufferMax( int length)
    {
        mBufferBoundary = length;
    }
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void interrupt() {
        Log.i(TAG, "USPSocketClient is stopped");
        this.mIsStop = true;
    }

    /**
     * close the UDP socket
     */
    public synchronized void close() {
        if (!this.mIsClosed) {
            this.mSocket.close();
            this.mIsClosed = true;
        }
    }

    /**
     * send the data by UDP
     *
     * @param data
     *            the data to be sent
     * @param targetHost
     *            the host name of target, e.g. 192.168.1.101
     * @param targetPort
     *            the port of target
     * @param interval
     *            the milliseconds to between each UDP sent
     */
    public void sendData(int dataLength, String targetHostName, int targetPort,
                         long interval) {
        byte[] data = new byte[mBufferBoundary];
        sendData(dataLength, data, 0, data.length, targetHostName, targetPort, interval);
    }

    /**
     * send the data by UDP
     *
     * @param data
     *            the data to be sent
     * @param offset
     * 			  the offset which data to be sent
     * @param count
     * 			  the count of the data
     * @param targetHost
     *            the host name of target, e.g. 192.168.1.101
     * @param targetPort
     *            the port of target
     * @param interval
     *            the milliseconds to between each UDP sent
     */
    public void sendData(int dataLength, byte[] data, int offset, int count,
                         String targetHostName, int targetPort, long interval) {
        if ((data == null) || (data.length <= 0)) {
            Log.e(TAG, "sendData(): data is illegal");
            return;
        }
        if (!mIsStop) {
            if (data.length == 0) {
                return;
            }
            try {
                DatagramPacket localDatagramPacket = new DatagramPacket(
                        data, data.length,
                        InetAddress.getByName(targetHostName), targetPort);
                localDatagramPacket.setLength(dataLength);
                this.mSocket.send(localDatagramPacket);
            } catch (UnknownHostException e) {
                Log.e(TAG, "sendData(): UnknownHostException");
                mIsStop = true;
            } catch (IOException e) {
                Log.e(TAG, "sendData(): IOException................");
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "sendData is Interrupted");
                mIsStop = true;
            }
        }
        if (mIsStop) {
            close();
        }
    }
}
