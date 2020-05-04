package com.skysoft.smart.intranetchat.bean.weather_bean;

public class ForecastBean {
    /**
     * date : 09//
     * high : 高温 20℃//
     * low : 低温 15℃//
     * ymd : 2019-10-09//
     * week : 星期三//
     * sunrise : 07:01
     * sunset : 18:41
     * aqi : 39
     * fx : 无持续风向
     * fl : <3级
     * type : 阴//
     * notice : 不要被阴云遮挡住好心情
     */

    private String TAG = ForecastBean.class.getSimpleName();
    private String date;
    private String high;
    private String low;
    private String ymd;
    private String week;
    private String type;
    private String fl;

    public String getFl() {
        return fl.substring(1);
    }

    public void setFl(String fl) {
        this.fl = fl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHigh() {
        return high.substring(high.indexOf(" ") + 1);
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low.substring(low.indexOf(" ") + 1);
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ForecastBean{" +
                "date='" + date + '\'' +
                ", high='" + high + '\'' +
                ", low='" + low.substring(3) + '\'' +
                ", ymd='" + ymd + '\'' +
                ", week='" + week + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
