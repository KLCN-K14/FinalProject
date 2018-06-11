package com.klcn.xuant.transporter.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class RideInfo {

    private String latPickup,lngPickup,destination,customerId,status;
    private Long timePickup,timeDropOff;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTimePickup() {
        return timePickup;
    }

    public void setTimePickup(Long timePickup) {
        this.timePickup = timePickup;
    }

    public Long getTimeDropOff() {
        return timeDropOff;
    }

    public void setTimeDropOff(Long timeDropOff) {
        this.timeDropOff = timeDropOff;
    }

    public RideInfo(String latPickup, String lngPickup, String destination, String customerId, String status, Long timePickup, Long timeDropOff) {

        this.latPickup = latPickup;
        this.lngPickup = lngPickup;
        this.destination = destination;
        this.customerId = customerId;
        this.status = status;
        this.timePickup = timePickup;
        this.timeDropOff = timeDropOff;
    }
}
