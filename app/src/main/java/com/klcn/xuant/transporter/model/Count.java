package com.klcn.xuant.transporter.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Count {

    int tripTotal, tripCompleted, tripCancel;
    double totalFeedback, totalRating;
    String id;




    public Count() {
    }

    public Count(int tripTotal, int tripCompleted, int tripCancel, double totalFeedback, double totalRating, String id) {
        this.tripTotal = tripTotal;
        this.tripCompleted = tripCompleted;
        this.tripCancel = tripCancel;
        this.totalFeedback = totalFeedback;
        this.totalRating = totalRating;
        this.id = id;
    }

    public int getTripTotal() {
        return tripTotal;
    }

    public void setTripTotal(int tripTotal) {
        this.tripTotal = tripTotal;
    }

    public int getTripCompleted() {
        return tripCompleted;
    }

    public void setTripCompleted(int tripCompleted) {
        this.tripCompleted = tripCompleted;
    }

    public int getTripCancel() {
        return tripCancel;
    }

    public void setTripCancel(int tripCancel) {
        this.tripCancel = tripCancel;
    }

    public double getTotalFeedback() {
        return totalFeedback;
    }

    public void setTotalFeedback(double totalFeedback) {
        this.totalFeedback = totalFeedback;
    }

    public double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
