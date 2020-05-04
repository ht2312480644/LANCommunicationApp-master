package com.skysoft.smart.intranetchat.app.impl;

import com.skysoft.smart.intranetchat.bean.weather_bean.WeatherEntity;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetRequest_Interface {

    @GET("101270101")
    Call<WeatherEntity> getWeatherEntity();
}
