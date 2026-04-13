package com.example.simactivationapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/phone/validate")
    Call<String> validatePhone(@Query("phone") String phone);
}