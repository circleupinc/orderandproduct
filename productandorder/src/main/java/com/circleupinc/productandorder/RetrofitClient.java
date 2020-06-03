package com.circleupinc.productandorder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClient {
    private static Retrofit retrofit = null;

    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();





    public static Retrofit getClient(String baseURL) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder( )
                    .baseUrl( baseURL )
                    .addConverterFactory( GsonConverterFactory.create( gson) )
                    .build( );
        }
        return retrofit;

    }




}
