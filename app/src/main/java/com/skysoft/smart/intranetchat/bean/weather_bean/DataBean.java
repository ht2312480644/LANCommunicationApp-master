package com.skysoft.smart.intranetchat.bean.weather_bean;

import java.util.List;

public class DataBean {
    /**
     * shidu : 73%
     * pm25 : 27.0
     * pm10 : 46.0
     * quality : 优
     * wendu : 20
     * ganmao : 各类人群可自由活动
     * forecast : [{"date":"09","high":"高温 20℃","low":"低温 15℃","ymd":"2019-10-09","week":"星期三","sunrise":"07:01","sunset":"18:41","aqi":39,"fx":"无持续风向","fl":"<3级","type":"阴","notice":"不要被阴云遮挡住好心情"},{"date":"10","high":"高温 22℃","low":"低温 16℃","ymd":"2019-10-10","week":"星期四","sunrise":"07:01","sunset":"18:40","aqi":80,"fx":"无持续风向","fl":"<3级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"},{"date":"11","high":"高温 24℃","low":"低温 17℃","ymd":"2019-10-11","week":"星期五","sunrise":"07:02","sunset":"18:39","aqi":71,"fx":"无持续风向","fl":"<3级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"},{"date":"12","high":"高温 24℃","low":"低温 16℃","ymd":"2019-10-12","week":"星期六","sunrise":"07:03","sunset":"18:37","aqi":71,"fx":"无持续风向","fl":"<3级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"},{"date":"13","high":"高温 22℃","low":"低温 16℃","ymd":"2019-10-13","week":"星期日","sunrise":"07:03","sunset":"18:36","aqi":69,"fx":"无持续风向","fl":"<3级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"},{"date":"14","high":"高温 20℃","low":"低温 15℃","ymd":"2019-10-14","week":"星期一","sunrise":"07:04","sunset":"18:35","aqi":63,"fx":"无持续风向","fl":"<3级","type":"阴","notice":"不要被阴云遮挡住好心情"},{"date":"15","high":"高温 18℃","low":"低温 13℃","ymd":"2019-10-15","week":"星期二","sunrise":"07:05","sunset":"18:34","fx":"无持续风向","fl":"<3级","type":"阴","notice":"不要被阴云遮挡住好心情"},{"date":"16","high":"高温 18℃","low":"低温 14℃","ymd":"2019-10-16","week":"星期三","sunrise":"07:05","sunset":"18:33","fx":"东风","fl":"<3级","type":"阴","notice":"不要被阴云遮挡住好心情"},{"date":"17","high":"高温 18℃","low":"低温 14℃","ymd":"2019-10-17","week":"星期四","sunrise":"07:06","sunset":"18:32","fx":"东北风","fl":"<3级","type":"小雨","notice":"雨虽小，注意保暖别感冒"},{"date":"18","high":"高温 20℃","low":"低温 14℃","ymd":"2019-10-18","week":"星期五","sunrise":"07:07","sunset":"18:31","fx":"东北风","fl":"<3级","type":"阴","notice":"不要被阴云遮挡住好心情"},{"date":"19","high":"高温 19℃","low":"低温 15℃","ymd":"2019-10-19","week":"星期六","sunrise":"07:07","sunset":"18:30","fx":"东风","fl":"<3级","type":"小雨","notice":"雨虽小，注意保暖别感冒"},{"date":"20","high":"高温 16℃","low":"低温 12℃","ymd":"2019-10-20","week":"星期日","sunrise":"07:08","sunset":"18:29","fx":"东北风","fl":"<3级","type":"小雨","notice":"雨虽小，注意保暖别感冒"},{"date":"21","high":"高温 19℃","low":"低温 12℃","ymd":"2019-10-21","week":"星期一","sunrise":"07:09","sunset":"18:27","fx":"东风","fl":"<3级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"},{"date":"22","high":"高温 19℃","low":"低温 12℃","ymd":"2019-10-22","week":"星期二","sunrise":"07:09","sunset":"18:26","fx":"东风","fl":"<3级","type":"晴","notice":"愿你拥有比阳光明媚的心情"},{"date":"23","high":"高温 17℃","low":"低温 13℃","ymd":"2019-10-23","week":"星期三","sunrise":"07:10","sunset":"18:25","fx":"东风","fl":"<3级","type":"多云","notice":"阴晴之间，谨防紫外线侵扰"}]
     * yesterday : {"date":"08","high":"高温 20℃","low":"低温 15℃","ymd":"2019-10-08","week":"星期二","sunrise":"07:00","sunset":"18:42","aqi":44,"fx":"无持续风向","fl":"<3级","type":"阴","notice":"不要被阴云遮挡住好心情"}
     */

    private double pm25;
    private String quality;
    private String wendu;
    private List<ForecastBean> forecast;

    public double getPm25() {
        return pm25;
    }

    public void setPm25(double pm25) {
        this.pm25 = pm25;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getWendu() {
        return wendu+"℃";
    }

    public void setWendu(String wendu) {
        this.wendu = wendu;
    }

    public List<ForecastBean> getForecast() {
        return forecast;
    }

    public void setForecast(List<ForecastBean> forecast) {
        this.forecast = forecast;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "pm25=" + pm25 +
                ", quality='" + quality + '\'' +
                ", wendu='" + wendu +'\'' +
                ", forecast=" + forecast.toString() +
                '}';
    }
}
