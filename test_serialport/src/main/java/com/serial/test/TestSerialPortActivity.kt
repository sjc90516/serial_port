package com.serial.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.serialport.api.SerialHelper
import com.serialport.api.onMachineListener


class TestSerialPortActivity : AppCompatActivity() {

    val portType: String = "/dev/ttyS3"
    val rateType: Int = 115200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_test)
        doBusiness()
    }


    private fun doBusiness() {
        SerialHelper.getInstance().openPort(rateType, portType)

        SerialHelper.getInstance().setOnMachineListener(object : onMachineListener {
            override fun openTimeOut(cmd: String?) {
//                LogUtils.e("超时-> $cmd")
            }

            override fun fail(fail: String?) {
//                LogUtils.e("失败-> $fail")
            }

            override fun success(sendCmd: String?, receiveCmd: String?) {
//                LogUtils.e("成功-> $sendCmd / $receiveCmd")
            }
        })
        test()
    }


    private fun test() {


        SerialHelper.getInstance().openPort(rateType, portType)
        for (i in 1..100) {
            SerialHelper.getInstance().sendCmd("FC010105FD")
            SerialHelper.getInstance().sendCmd("FC010205FD")
            SerialHelper.getInstance().sendCmd("FC010305FD")
            SerialHelper.getInstance().sendCmd("FC010405FD")
            SerialHelper.getInstance().sendCmd("FC010505FD")
            SerialHelper.getInstance().sendCmd("FC010605FD")
            SerialHelper.getInstance().sendCmd("FC010705FD")
            SerialHelper.getInstance().sendCmd("FC010805FD")
        }
    }


}