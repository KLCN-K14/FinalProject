package com.klcn.xuant.transporter.common;

import android.location.Location;

import com.klcn.xuant.transporter.remote.FCMClient;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.klcn.xuant.transporter.remote.IGoogleAPI;
import com.klcn.xuant.transporter.remote.RetrofitClient;

public class Common {

    public static final String currentToken = "";

    public static final String drivers_tbl = "Drivers";
    public static final String driver_available_tbl = "DriverAvailable";
    public static final String driver_working_tbl = "DriverWorking";
    public static final String customers_tbl = "Customers";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String ride_info_tbl = "RideInfo";
    public static final String tokens_tbl = "Tokens";


    public static final String ride_info_status_driver_coming = "1";
    public static final String ride_info_status_on_trip = "2";
    public static final String ride_info_status_customer_cancel = "3";
    public static final String ride_info_status_driver_cancel = "4";
    public static final String ride_info_status_complete = "5";


    public static final Double base_fare = 10.;
    public static final Double cost_per_km = 10.;
    public static final Double cost_per_minute_standard = 5.;
    public static final Double cost_per_minute_premium = 10.;
    public static final Double transport_fee = 0.15;

    public static final String service_vehicle_standard = "Transport Standard";
    public static final String service_vehicle_premium = "Transport Premium";

    public static Location mLastLocationDriver = null;
    public static Location mLastLocationCustomer = null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String keySplit = "TRANSPORT";
    public static final String fcmURL = "https://fcm.googleapis.com/";

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
