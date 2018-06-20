package com.klcn.xuant.transporter.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class TripInfo {

    private String pickup,dropoff,customerId,driverId,serviceVehicle,status,distance,
            time,fixedFare,otherToll,rating,feedback,reasonCancel;
    private Long dateCreated,timePickup,timeDropoff;

    public TripInfo() {
    }

    public TripInfo(String pickup, String dropoff, String customerId, String driverId, String serviceVehicle, String status, String distance, String time, String fixedFare, String otherToll, String rating, String feedback, String reasonCancel, Long dateCreated, Long timePickup, Long timeDropoff) {
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.customerId = customerId;
        this.driverId = driverId;
        this.serviceVehicle = serviceVehicle;
        this.status = status;
        this.distance = distance;
        this.time = time;
        this.fixedFare = fixedFare;
        this.otherToll = otherToll;
        this.rating = rating;
        this.feedback = feedback;
        this.reasonCancel = reasonCancel;
        this.dateCreated = dateCreated;
        this.timePickup = timePickup;
        this.timeDropoff = timeDropoff;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    public void setDropoff(String dropoff) {
        this.dropoff = dropoff;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getServiceVehicle() {
        return serviceVehicle;
    }

    public void setServiceVehicle(String serviceVehicle) {
        this.serviceVehicle = serviceVehicle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFixedFare() {
        return fixedFare;
    }

    public void setFixedFare(String fixedFare) {
        this.fixedFare = fixedFare;
    }

    public String getOtherToll() {
        return otherToll;
    }

    public void setOtherToll(String otherToll) {
        this.otherToll = otherToll;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getReasonCancel() {
        return reasonCancel;
    }

    public void setReasonCancel(String reasonCancel) {
        this.reasonCancel = reasonCancel;
    }

    public Long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Long getTimePickup() {
        return timePickup;
    }

    public void setTimePickup(Long timePickup) {
        this.timePickup = timePickup;
    }

    public Long getTimeDropoff() {
        return timeDropoff;
    }

    public void setTimeDropoff(Long timeDropoff) {
        this.timeDropoff = timeDropoff;
    }
}
