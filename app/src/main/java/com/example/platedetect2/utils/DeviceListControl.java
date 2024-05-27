package com.example.platedetect2.utils;

import android.hardware.usb.UsbDevice;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import java.util.ArrayList;

public class DeviceListControl {
    public static DeviceListControl instance;
    private ArrayList<ModelDevice> CameraDevice;
    private boolean Camera_isSet = false;
    private ArrayList<ModelDevice> ArduinoDevice;
    private boolean Arduino_isSet = false;



    public static DeviceListControl getInstance(){
        if(instance == null)
            instance = new DeviceListControl();
        return instance;
    }

    public void addNewArduinoDevice(ModelDevice dataDevice){
        if(ArduinoDevice == null){
            ArduinoDevice = new ArrayList<>();
        }
        if(!FindArduinoDevice(dataDevice.vendorId))
            ArduinoDevice.add(dataDevice);
    }
    public ArrayList<ModelDevice> getCameraDevice() {
        if(CameraDevice == null)
            CameraDevice = new ArrayList<>();
        return CameraDevice;
    }

    public void setCameraDevice(ArrayList<ModelDevice> cameraDevice) {
        CameraDevice = cameraDevice;
    }

    public ArrayList<ModelDevice> getArduinoDevice() {
        if(ArduinoDevice == null)
            ArduinoDevice = new ArrayList<>();
        return ArduinoDevice;
    }

    public void setArduinoDevice(ArrayList<ModelDevice> arduinoDevice) {
        ArduinoDevice = arduinoDevice;
    }

    public boolean FindArduinoDevice(int deviceId) {
        for (ModelDevice modelDevice : ArduinoDevice) {
            if(modelDevice.vendorId == deviceId)
                return true;
        }
        return false;
    }
    public boolean FindCameraDevice(int vendorId) {
        for (ModelDevice modelDevice : CameraDevice) {
            if(modelDevice.vendorId == vendorId)
                return true;
        }
        return false;
    }
    public void addNewCameraDevice(ModelDevice modelData) {
        if(CameraDevice == null){
            CameraDevice = new ArrayList<>();
        }
        if(!FindCameraDevice(modelData.vendorId))
            CameraDevice.add(modelData);
    }
    public boolean Camera_isSet() {
        return Camera_isSet;
    }

    public void setCamera_isSet(boolean camera_isSet) {
        Camera_isSet = camera_isSet;
    }

    public boolean Arduino_isSet() {
        return Arduino_isSet;
    }

    public void setArduino_isSet(boolean arduino_isSet) {
        Arduino_isSet = arduino_isSet;
    }
    public static class ModelDevice {
        UsbDevice device;
        int vendorId;
        int port;
        UsbSerialDriver driver;

        public ModelDevice(UsbDevice device, int deviceID, int port, UsbSerialDriver driver) {
            this.device = device;
            this.vendorId = deviceID;
            this.port = port;
            this.driver = driver;
        }

        public UsbDevice getDevice() {
            return device;
        }

        public void setDevice(UsbDevice device) {
            this.device = device;
        }

        public int getvendorId() {
            return vendorId;
        }

        public void setvendorId(int vendorId) {
            this.vendorId = vendorId;
        }
    }
}
