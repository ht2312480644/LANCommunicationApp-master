package com.skysoft.smart.intranetchat.ui.activity.weather;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.impl.GetRequest_Interface;
import com.skysoft.smart.intranetchat.bean.weather_bean.WeatherEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity {
    TextView currentTemp, cityTemp, date, airQuality, pmIndex;
    private static final String TAG = "WeatherActivity";
    public List<Map<String,String>> listweather=new ArrayList<Map<String,String>>();
    LinearLayout lin_01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TLog.i(TAG, "onCreate: "+"跳转成功！");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        currentTemp =(TextView)findViewById(R.id.weather_current_temp);
        cityTemp =(TextView)findViewById(R.id.weather_city_temp);
        date =(TextView)findViewById(R.id.weather_date);
        pmIndex = (TextView)findViewById(R.id.weather_PM_index);
        airQuality =(TextView)findViewById(R.id.weather_air_quality);
        lin_01=(LinearLayout) findViewById(R.id.activity_weather_layout_first);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10,TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                //设置数据解析器
                .addConverterFactory(GsonConverterFactory.create())
                //设置网络请求的Url地址
                .baseUrl("http://t.weather.sojson.com/api/weather/city/")
                .client(okHttpClient)
                .build();
        GetRequest_Interface Weatherface = retrofit.create(GetRequest_Interface.class);
        Call<WeatherEntity> weatherEntityCall = Weatherface.getWeatherEntity();
        weatherEntityCall.enqueue(new Callback<WeatherEntity>() {
            @Override
            public void onResponse(Call<WeatherEntity> call, Response<WeatherEntity> response) {
                WeatherEntity body = response.body();
                String stg_city = body.getCityInfo().getCity();
                String stg_wendu = body.getData().getWendu();
                Double stg_pm25 = body.getData().getPm25();
                String stg_quality = body.getData().getQuality();
                String stg_date = body.getData().getForecast().get(0).getYmd();
                String stg_week = body.getData().getForecast().get(0).getWeek();
                String stg_higt = body.getData().getForecast().get(0).getHigh();
                String stg_low  = body.getData().getForecast().get(0).getLow();
                String stg_type = body.getData().getForecast().get(0).getType();
                currentTemp.setText(stg_wendu);
                cityTemp.setText(stg_city+"    "+stg_type+"    "+stg_low+"-"+stg_higt);
                date.setText(stg_date+"    "+stg_week);
                pmIndex.setText("PM2.5指数："+stg_pm25);
                airQuality.setText("空气质量："+stg_quality);

                for (int i = 1;i < 7;i++ ){
                    Map<String,String> map = new HashMap<String,String>();
                    map.put("week",body.getData().getForecast().get(i).getWeek());
                    map.put("temp",body.getData().getForecast().get(i).getLow()+"-"+body.getData().getForecast().get(i).getHigh()+"  ");
                    map.put("weather",body.getData().getForecast().get(i).getType());
                    listweather.add(map);
                }
                lin_01.removeAllViews();
                for(int i=0;i<listweather.size();i++)
                {

                    Map<String,String> map=listweather.get(i);
                    LayoutInflater layoutInflater=LayoutInflater.from(getApplicationContext());
                    View view=layoutInflater.inflate(R.layout.listview_weather,null);
                    TextView tv_week=(TextView)view.findViewById(R.id.listview_weather_week);
                    TextView tv_temp=(TextView)view.findViewById(R.id.listview_weather_temp);
                    TextView tv_weather=(TextView)view.findViewById(R.id.listview_weather_weather);
                    tv_week.setText(map.get("week"));
                    tv_temp.setText(map.get("temp"));
                    tv_weather.setText(map.get("weather"));
                    lin_01.addView(view);
                }
            }

            @Override
            public void onFailure(Call<WeatherEntity> call, Throwable t) {
                t.printStackTrace();
                TLog.i(TAG, "onFailure: 失败");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
