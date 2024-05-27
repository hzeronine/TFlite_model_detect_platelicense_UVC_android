package com.example.platedetect2.ScanQR;
public class CheckIn {
    String licensePlate ;
    String user_id;
    public CheckIn(String licensePlate, String user_id) {
        this.licensePlate = licensePlate;
        this.user_id = user_id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

}
