package com.example.cabnow.Api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Api {

    @Headers("Content-Type: application/json")
    @POST("adduser")
    Call<ResponseBody> createUser(@Body String body);

    @Headers("Content-Type: application/json")
    @POST("login")
    Call<ResponseBody> loginUser(@Body String body);
}
