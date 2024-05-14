package com.example.platedetect2.utils;

import com.herohan.uvcapp.ICameraHelper;

public class DetectDeviceHelper{
    private  static DetectDeviceHelper instance;
    ICameraHelper cameraHelper;

    public static DetectDeviceHelper getInstance(){
        if(instance == null){
            instance = new DetectDeviceHelper();
        }
        return instance;
    }
    public void setStateCallback(ICameraHelper.StateCallback IStateCallBack){
        cameraHelper.setStateCallback(IStateCallBack);
    }

}
