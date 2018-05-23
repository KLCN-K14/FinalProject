package com.klcn.xuant.transporter.remote;

import com.klcn.xuant.transporter.model.FCMResponse;
import com.klcn.xuant.transporter.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAANVWs4qo:APA91bG5wmb9LQgkrfCwKaiO2Xhl8u9xMePsC_JoGGOhgd3OnIRzWeivf-yL46iRk2hEjNMyolGWUPbd4X7IQwpayAe8yy_xMLI2ksiygk-oLf--0UzLzJWg6z-WbOsSOYdN-SejWgdk"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
