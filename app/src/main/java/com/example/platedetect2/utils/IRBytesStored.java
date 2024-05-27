package com.example.platedetect2.utils;

import com.example.platedetect2.ScanQR.EmailLocation;

public class IRBytesStored {
    public static IRBytesStored instance;
    private byte [] IR_array_data_open = null;
    private byte [] IR_array_data_close = null;
    boolean IR_isSet = false;
    private int statusCheck = -1;
    private String liscensePlate;
    private EmailLocation emailLocation;
    public static IRBytesStored getInstance(){
        if(instance == null)
            instance = new IRBytesStored();
        return instance;
    }
    public byte[] getIR_array_CloseData() {
        return IR_array_data_close;
    }

    public void setIR_CloseData(byte[] IR_array_data_close) {
        this.IR_array_data_close = IR_array_data_close;
    }
    public void setIR_OpenData(byte[] Data){
        IR_array_data_open = Data;
    }

    public byte[] getIR_array_OpenData() {
        return IR_array_data_open;
    }

    public int getStatusCheck() {
        return statusCheck;
    }

    public void setStatusCheck(int statusCheck) {
        this.statusCheck = statusCheck;
    }
    public boolean isIR_isSet() {
        return IR_isSet;
    }

    public void setIR_isSet(boolean IR_isSet) {
        this.IR_isSet = IR_isSet;
    }

    public String getLiscensePlate() {
        return liscensePlate;
    }

    public void setLiscensePlate(String liscensePlate) {
        this.liscensePlate = liscensePlate;
    }

    public EmailLocation getEmailLocation() {
        return emailLocation;
    }

    public void setEmailLocation(EmailLocation emailLocation) {
        this.emailLocation = emailLocation;
    }
}
