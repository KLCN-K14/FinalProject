package com.klcn.xuant.transporter.common;

import android.location.Location;

import com.klcn.xuant.transporter.model.Driver;
import com.klcn.xuant.transporter.remote.FCMClient;
import com.klcn.xuant.transporter.remote.IFCMService;
import com.klcn.xuant.transporter.remote.IGoogleAPI;
import com.klcn.xuant.transporter.remote.RetrofitClient;

public class Common {

    public static final String drivers_tbl = "Drivers";
    public static final String driver_available_tbl = "DriverAvailable";
    public static final String driver_working_tbl = "DriverWorking";
    public static final String customers_tbl = "Customers";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String trip_info_tbl = "TripInfo";
    public static final String chat_tbl = "Chat";
    public static final String tokens_tbl = "Tokens";

    public static final String trip_info_status_customer_cancel = "1";
    public static final String trip_info_status_driver_cancel = "2";
    public static final String trip_info_status_complete = "3";

    public static final Double base_fare = 5.;
    public static final Double cancel_fee = 5.;
    public static final Double cost_per_km = 3.5;
    public static final Double cost_per_minute_standard = 2.;
    public static final Double cost_per_minute_premium = 3.5;
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
