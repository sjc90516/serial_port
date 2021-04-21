package com.serialport.api;

import android.util.Log;

import com.serialport.apis.SerialPort;


public class SerialPortService {


    private static final String TAG = SerialPortService.class.getSimpleName();

    private String _path;
    private int _baudrate;
    private int _readBufferSize;
    private boolean _isHexType;

    private int _bits;
    private char _event;
    private int _stop;
    private int timeOut = 2000;

    public SerialPort mSerialPort;
    onMachineListener onMachineListener;

    /**
     * @param path           串口号如：/dev/ttyS0
     * @param baudrate       波特率如：115200
     * @param readBufferSize 返回缓存池长度如：1024
     * @param isHexType      是否16进制收发数据
     * @param bits           数据位，取值7或8
     * @param event          校验位，取值N ,E, O ,S
     * @param stop           停止位，取值1或2
     * @description 串口构造函数
     */
    public SerialPortService(final String path, int baudrate, int readBufferSize, boolean isHexType, int bits, char event, int stop, final onMachineListener onMachineListener) {

        mSerialPort = new SerialPort();
        _path = path;
        _baudrate = baudrate;
        _readBufferSize = readBufferSize;
        _isHexType = isHexType;

        _bits = bits;
        _event = event;
        _stop = stop;
        this.onMachineListener = onMachineListener;
    }

    /**
     * 设置串口读取超时
     * @param timeOut
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        if (mSerialPort != null) {
            mSerialPort.setTimeOut(timeOut);
        }
    }

    /**
     * @return -1表示打开串口出错；0表示串口已打开；1表示正确打开串口
     * @description 打开串口
     */
    public int OpenPort() {

//        File device = new File(_path);
//        if (!device.canRead() || !device.canWrite()) {
//            try {
//            /* Missing read/write permission, trying to chmod the file */
//                Process su;
//                su = Runtime.getRuntime().exec("/system/xbin/su");
//                String cmd = "chmod 777 " + device.getAbsolutePath() + "\n"
//                        + "exit\n";
//            /*String cmd = "chmod 777 /dev/s3c_serial0" + "\n"
//            + "exit\n";*/
//                su.getOutputStream().write(cmd.getBytes());
//                if ((su.waitFor() != 0) || !device.canRead()
//                        || !device.canWrite()) {
//                    throw new SecurityException();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new SecurityException();
//            }
//        }

        int result = mSerialPort.openPortWithPath(_path, _baudrate, _bits, _event, _stop, 0, _readBufferSize, onMachineListener);
        return result;

    }


    /**
     * @description 关闭串口
     */
    public void ClosePort() {
        mSerialPort.closePort();
    }


    /**
     * @return true已打开，false未打开
     * @description 串口是否已经打开
     */
    public boolean isOpen() {
        return mSerialPort.isOpen();
    }


    /**
     * @description 开始读取
     */
    public void startRead() {
        mSerialPort.startRead();
    }

    /**
     * @description 停止读取
     */
    public void stopRead() {
        mSerialPort.stopRead();
    }


    /**
     * @param data 要发送的数据
     * @return -1表示出错；>=0表示写入成功
     * @description 发送数据
     */
    public int SendData(String data) throws InterruptedException {

        if (_isHexType) {
            return mSerialPort.writeData(data.replaceAll("\\s*", ""));
        } else {
            return mSerialPort.writeData(data.getBytes());
        }
    }


    private void logE(String msg) {
        Log.e(TAG, "=>" + msg);
    }
}