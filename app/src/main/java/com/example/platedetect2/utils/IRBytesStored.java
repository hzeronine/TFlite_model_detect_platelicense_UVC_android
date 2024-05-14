package com.example.platedetect2.utils;

public class IRBytesStored {
    public static IRBytesStored instance;
    private byte [] IR_array_data;
    public static IRBytesStored getInstance(){
        if(instance == null)
            instance = new IRBytesStored();
        return instance;
    }

    public void setIRData(byte[] Data){
        IR_array_data = Data;
    }

    public byte[] getIR_array_data() {
        return IR_array_data;
    }
}
