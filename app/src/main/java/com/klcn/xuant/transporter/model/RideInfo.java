package com.klcn.xuant.transporter.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class RideInfo {

    private String latPickup,lngPickup,destination,customerId;

    public RideInfo() {
    }

    public String getLatPickup() {
        return latPickup;
    }

    public void setLatPickup(String latPickup) {
        this.latPickup = latPickup;
    }

    public String getLngPickup() {
        return lngPickup;
    }

    public void setLngPickup(String lngPickup) {
        this.lngPickup = lngPickup;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public RideInfo(String latPickup, String lngPickup, String destination, String customerId) {

        this.latPickup = latPickup;
        this.lngPickup = lngPickup;
        this.destination = destination;
        this.customerId = customerId;
    }
}
