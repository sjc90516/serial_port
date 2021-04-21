/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.serialport.apis;

import android.util.Log;

import com.serialport.api.onMachineListener;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class SerialPort {

    private static final String TAG = SerialPort.class.getSimpleName();
    public boolean showDebug = false;
    private int mFd = 0;

    private int nReadBufferSize = 0;
    private boolean bOpenPort = false;
    private int mBaudrate = 0;

    public onMachineListener machineListener;
    Lock lock;
    ArrayBlockingQueue blockingQueue;
    ReadThread readThread;

    int timeOut = 2000;

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isOpen() {
        return bOpenPort;
    }

    /*
     * 返回值：-1表示打开串口出错；0表示串口已打开；1表示正确打开串口
     * event 类型 char 校验类型 取值N ,E, O, S
     */
    public int OpenPort(int baudrate, int bits, char event, int stop, int flags, int readBufferSize, onMachineListener machineListener) {
        if (isOpen()) return 0;

        mFd = NativeFunc.getInstance().open(baudrate, bits, event, stop, flags);

        log("mFd=" + mFd);
        if (mFd <= 0) {
            logE("open serial port throw IOException");
            return -1;
        }

        mBaudrate = baudrate;
        nReadBufferSize = readBufferSize;
        bOpenPort = true;
        this.machineListener = machineListener;

        return 1;
    }

    /**
     * 返回值：-1表示打开串口出错；0表示串口已打开；1表示正确打开串口
     */
    public int openPortWithPath(String path, int baudrate, int bits, char event, int stop, int flags, int readBufferSize, onMachineListener machineListener) {
        if (isOpen()) return 0;

        mFd = NativeFunc.getInstance().openWithPath(path, baudrate, bits, event, stop, flags);

        log("mFd=" + mFd);
        if (mFd <= 0) {
            logE("open serial port throw IOException");
            return -1;
        }
        mBaudrate = baudrate;
        nReadBufferSize = readBufferSize;
        bOpenPort = true;
        if (lock == null) {
            lock = new ReentrantLock();
            blockingQueue = new ArrayBlockingQueue<byte[]>(8);
        }

        this.machineListener = machineListener;
        return 1;
    }

    public void closePort() {
        stopRead();
        if (bOpenPort) {
            NativeFunc.getInstance().close(mFd);
            log("close serial port");
        }

        bOpenPort = false;
        mFd = 0;
    }


    public void startRead() {

//        stopRead();
        if (readThread != null && readThread.isAlive()) {
            return;
        }

        readThread = new ReadThread();
        readThread.start();
    }

    public void stopRead() {

    }

    //buffer：格式为{(byte) 0x91, (byte) 0x81, (byte) 0x8C}
    //返回值：-1表示出错；>=0表示写入成功


    public int writeData(byte[] buffer) throws InterruptedException {//
        if (!isOpen()) {
            Log.e("", "---------------Serial port is not open---------------");
            return -1;
        }
        if (buffer == null || buffer.length == 0) {
            Log.e("", "---------------Serial port is not open---------------");
            return -2;
        }
        if (blockingQueue.offer(buffer, 3, TimeUnit.SECONDS)) return 0;
        return -3;
//        return NativeFunc.getInstance().write(mFd, buffer, buffer.length);
    }


    /**
     * 生成者  生产产品1，放入block队列，
     */
    //返回值：-1表示出错；>=0表示写入成功
    public int writeData(String buffer) throws InterruptedException {
        if (!isOpen()) {
            Log.e("", "---------------Serial port is not open---------------");
            return -1;
        }
        if (buffer == null) {
            Log.e("", "---------------Serial port is not open---------------");
            return -2;
        }

        byte[] buf = HexString2Bytes(buffer);


        if (blockingQueue.offer(buf, 3, TimeUnit.SECONDS)) return 0;

        return -3;
    }


    private byte[] HexString2Bytes(String src) {

        byte[] tmp = src.getBytes();
        int bytes = tmp.length / 2;

        byte[] ret = new byte[bytes];


        for (int i = 0; i < bytes; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    private byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    /**
     * 消费者，消费副产品1，
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            lock.lock();

            try {
                while (true) {
                    byte[] buf = (byte[]) blockingQueue.poll(4, TimeUnit.SECONDS);
                    if (buf != null) {
                        logE("串口发送：" + byte2HexStr(buf, buf.length));
//                    返回值：-1表示出错；>=0表示写入成功
                        int res = NativeFunc.getInstance().write(mFd, buf, buf.length);
                        if (res < 0) {
                            machineListener.fail("fail->" + byte2HexStr(buf, buf.length));
                            break;
                        }
                    } else {
                        break;
                    }

                    long time = System.currentTimeMillis();
                    byte[] buffer = new byte[nReadBufferSize];
                    while (true) {
                        long nowTime = System.currentTimeMillis();
                        long change = nowTime - time;
                        if (change >= timeOut) {
                            if (machineListener != null) {
                                machineListener.openTimeOut(byte2HexStr(buf, buf.length));
                            }
                            break;
                        }

                        int size = NativeFunc.getInstance().read(mFd, buffer, buffer.length, mBaudrate);
                        if (size > 0) {
                            String data = byte2HexStr(buffer, size).trim().replace(" ", "");
                            logE(data);
                            if (machineListener != null) {
                                machineListener.success(byte2HexStr(buf, buf.length), data.trim());
                            }
                            break;
                        }
                        Thread.sleep(50);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * bytes转换成十六进制字符串
     */
    public String byte2HexStr(byte[] b, int length) {
        String hs = "";
        String stmp;
        for (int n = 0; n < length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));

            if (stmp.length() == 1) {
                hs += " " + "0" + stmp;
            } else {
                hs += " " + stmp;
            }
        }
        return hs.trim().toUpperCase();
    }


    public void watchDogCtrl(int type) {
        NativeFunc.WatchDogCtrl(type);
    }

    private void log(String msg) {
        if (showDebug) {
            Log.d(TAG, this.toString() + "=>" + msg);
        }
    }

    private void logE(String msg) {
        if (showDebug) {
            Log.e(TAG, "=>" + msg);
        }
    }
}
