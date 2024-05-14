package com.example.platedetect2.utils;

import android.hardware.usb.UsbDevice;

import java.util.ArrayList;

public class DeviceListControl {
    public static DeviceListControl instance;
    private ArrayList<ModelDevice> CameraDevice;
    private ArrayList<ModelDevice> ArduinoDevice;

    public static DeviceListControl getInstance(){
        if(instance == null)
            instance = new DeviceListControl();
        return instance;
    }
    public ArrayList<ModelDevice> getCameraDevice() {
        return CameraDevice;
    }

    public void setCameraDevice(ArrayList<ModelDevice> cameraDevice) {
        CameraDevice = cameraDevice;
    }

    public ArrayList<ModelDevice> getArduinoDevice() {
        return ArduinoDevice;
    }

    public void setArduinoDevice(ArrayList<ModelDevice> arduinoDevice) {
        ArduinoDevice = arduinoDevice;
    }

    protected class ModelDevice {
        UsbDevice device;
        String deviceID;

        public UsbDevice getDevice() {
            return device;
        }

        public void setDevice(UsbDevice device) {
            this.device = device;
        }

        public String getDeviceID() {
            return deviceID;
        }

        public void setDeviceID(String deviceID) {
            this.deviceID = deviceID;
        }
    }
}
