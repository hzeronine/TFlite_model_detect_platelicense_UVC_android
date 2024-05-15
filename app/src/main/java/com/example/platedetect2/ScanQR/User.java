package com.example.platedetect2.ScanQR;


import java.util.Date;

public class User {
    private int user_id;
    private String full_name;
    private String user_code;
    private int role;
    private Date date_of_birth;
    private String phone_number;
    private String address;
    private String email;
    private int gender;
    private double wallet;

    // Constructor
    public User(int user_id, String full_name, String user_code, int role, Date date_of_birth, String phone_number, String address, String email, int gender, double wallet) {
        this.user_id = user_id;
        this.full_name = full_name;
        this.user_code = user_code;
        this.role = role;
        this.date_of_birth = date_of_birth;
        this.phone_number = phone_number;
        this.address = address;
        this.email = email;
        this.gender = gender;
        this.wallet = wallet;
    }

    // Getter và Setter cho user_id
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    // Getter và Setter cho full_name
    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    // Getter và Setter cho user_code
    public String getUser_code() {
        return user_code;
    }

    public void setUser_code(String user_code) {
        this.user_code = user_code;
    }

    // Getter và Setter cho role
    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    //Getter và Setter cho date_of_birth
    public Date getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(Date date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    // Getter và Setter cho phone_number
    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    // Getter và Setter cho address
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Getter và Setter cho email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter và Setter cho gender
    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    // Getter và Setter cho wallet
    public long getWallet() {
        return (long)wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }
}
