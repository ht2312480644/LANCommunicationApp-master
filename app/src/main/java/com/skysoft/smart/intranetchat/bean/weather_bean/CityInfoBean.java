package com.skysoft.smart.intranetchat.bean.weather_bean;

public class CityInfoBean {
    /**
     * city : 成都市
     * citykey : 101270101
     * parent : 四川
     * updateTime : 13:54
     */

    private String city;


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "CityInfoBean{" +
                "city='" + city + '\'' +
                '}';
    }
}
