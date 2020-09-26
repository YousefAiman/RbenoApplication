package com.example.yousef.rbenoapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA968MPYY:APA91bHTW1CdkvNCcmMJQDqt-1O2HcpXHZJvW-uFQFjgTBhvNP38XOdklT--qqmMDs5LXmgcYTbDW4pH7v0kxKMENWyPOgfQXtRkqCLPG8g9NNeSIo5voXLVFCajGz0ka8-LrNe-Y_01"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
