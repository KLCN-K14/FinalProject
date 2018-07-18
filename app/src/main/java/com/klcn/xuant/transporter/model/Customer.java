package com.klcn.xuant.transporter.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Customer {

    private String name,phoneNum,email,imgUrl;

    private Long dateCreated;

    private int countTrip,countCancel;

    public Customer() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getCountTrip() {
        return countTrip;
    }

    public void setCountTrip(int countTrip) {
        this.countTrip = countTrip;
    }

    public int getCountCancel() {
        return countCancel;
    }

    public void setCountCancel(int countCancel) {
        this.countCancel = countCancel;
    }

    public Customer(String name, String phoneNum, String email, String imgUrl, Long dateCreated, int countTrip, int countCancel) {

        this.name = name;
        this.phoneNum = phoneNum;
        this.email = email;
        this.imgUrl = imgUrl;
        this.dateCreated = dateCreated;
        this.countTrip = countTrip;
        this.countCancel = countCancel;
    }
}
