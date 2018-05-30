package com.klcn.xuant.transporter.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Driver {

    private String name,email,phoneNum,imgUrl,serviceVehicle,nameVehicle,licensePlate,avgRatings;

    public Driver() {
    }

    public Driver(String name, String email, String phoneNum, String imgUrl, String serviceVehicle, String nameVehicle, String licensePlate, String avgRatings) {
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.imgUrl = imgUrl;
        this.serviceVehicle = serviceVehicle;
        this.nameVehicle = nameVehicle;
        this.licensePlate = licensePlate;
        this.avgRatings = avgRatings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getServiceVehicle() {
        return serviceVehicle;
    }

    public void setServiceVehicle(String serviceVehicle) {
        this.serviceVehicle = serviceVehicle;
    }

    public String getNameVehicle() {
        return nameVehicle;
    }

    public void setNameVehicle(String nameVehicle) {
        this.nameVehicle = nameVehicle;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getAvgRatings() {
        return avgRatings;
    }

    public void setAvgRatings(String avgRatings) {
        this.avgRatings = avgRatings;
    }
}
