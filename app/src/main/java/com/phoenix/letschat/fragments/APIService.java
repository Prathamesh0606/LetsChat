package com.phoenix.letschat.fragments;

import com.phoenix.letschat.Notifications.MyResponse;
import com.phoenix.letschat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAlgEatnY:APA91bH6VDFYgu5swE3yVv84qV01rGhPiOqkvildBZJfDNjH_qpmz-nw32ubuiAAP2q_nepzmNX4SszqaCoiuLe1-JX0c_28MOYIOYc9ERRvB3nAkH1uhJNd4S9HyZHfQydfON00EapS"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);


}
