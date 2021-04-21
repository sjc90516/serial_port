package com.serialport.apis;

public class NativeFunc {
    static {
        System.loadLibrary("SerialApi");
    }

    private volatile static NativeFunc instance = null;

    private NativeFunc() {
    }

    public static NativeFunc getInstance() {
        if (instance == null) {
            synchronized (NativeFunc.class) {
                if (instance == null) {
                    instance = new NativeFunc();
                }
            }
        }
        return instance;
    }


    public native int openWithPath(String path, int baudrate, int bits, char event, int stop, int flags);//event 类型 char 校验类型 取值N ,E, O, S

    public native int open(int baudrate, int bits, char event, int stop, int flags);

    public native int write(int fd, byte[] buf, int sizes);

    public native int read(int fd, byte[] buf, int sizes, int baudrate);

    public native void close(int fd);

    /**
     * @param type 0:Stop feeding the dog   1:Start to feed the dog
     * @return
     */
    public native static int WatchDogCtrl(int type);


}
