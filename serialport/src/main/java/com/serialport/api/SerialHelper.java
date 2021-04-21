package com.serialport.api;


import android.util.Log;

import com.serialport.apis.SerialPortFinder;

import java.util.LinkedList;

public class SerialHelper {

    public static class Holder {
        public static SerialHelper helper = new SerialHelper();
    }

    public static SerialHelper getInstance() {
        return Holder.helper;
    }

    onMachineListener onMachineListener;

    SerialPortFinder serialPortFinder = new SerialPortFinder();

    protected SerialPortService mSerialPort;


    public void setOnMachineListener(onMachineListener onMachineListener) {
        this.onMachineListener = onMachineListener;
        if (mSerialPort != null) {
            mSerialPort.onMachineListener = onMachineListener;
            mSerialPort.mSerialPort.machineListener = onMachineListener;
        }
    }

    public int openPort(int baudrate, String path) {
        if (mSerialPort != null) {
            mSerialPort.ClosePort();//关闭mFd
            mSerialPort = null;
        }
        char event = 'N';
        mSerialPort = new SerialPortService(path, baudrate, 1024, true, 8, event, 1, onMachineListener);
        return mSerialPort.OpenPort();
    }

    /**
     * serialPort
     *
     * @param
     */
//    public void openPort(int rate, String serialPort) {
//        this.serialPort = serialPort;
//        if (mSerialPort != null) {
//            mSerialPort.ClosePort();
//        }
//    }
    public void sendCmd(String cmd) {
        mSerialPort.startRead();
        int sendResult = -4;
        try {
            sendResult = mSerialPort.SendData(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
            if (onMachineListener != null) {
                onMachineListener.fail("ArrayBlockingQueue error" + e.toString());
            }
        }
        if (sendResult != 0) {
            Log.e("", "sendCmd: sendResult = " + sendResult);

        }
    }

    public LinkedList<String> getAllPortsPath() {
        LinkedList arrayList = new LinkedList<String>();
        String[] devices = serialPortFinder.getAllDevicesPath();
        for (String path : devices) {
            arrayList.add(path);
        }
        return arrayList;
    }

}
