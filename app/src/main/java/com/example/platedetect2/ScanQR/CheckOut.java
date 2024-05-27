package com.example.platedetect2.ScanQR;

public class CheckOut {
    String licensePlate;
    String userId;
    int locationId;

    public CheckOut(String licensePlate, String userId, int locationId) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }


}
