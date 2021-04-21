package com.serialport.api;

public interface onMachineListener {
    void success(String sendCmd, String receiveCmd);

    void fail(String fail);

    void openTimeOut(String cmd);
}
