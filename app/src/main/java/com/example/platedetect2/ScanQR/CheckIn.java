package com.example.platedetect2.ScanQR;
public class CheckIn {
    String licensePlate ;
    int user_id;
    public CheckIn(String licensePlate, int user_id) {
        this.licensePlate = licensePlate;
        this.user_id = user_id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

}
