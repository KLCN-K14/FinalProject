package com.klcn.xuant.transporter.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Driver {
    public String name;
    public String email;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Driver() {
    }

    public Driver(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
