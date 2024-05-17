package com.example.platedetect2.ScanQR;

public class CheckOut {
    String licensePlate;
    int userId;
    int locationId;

    public CheckOut(String licensePlate, int userId, int locationId) {
        this.licensePlate = licensePlate;
        this.userId = userId;
        this.locationId = locationId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }


}
