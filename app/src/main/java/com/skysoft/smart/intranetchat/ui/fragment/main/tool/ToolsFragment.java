/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/15
 * Description: [PT-40][Intranet Chat] [APP][UI] Home page ui
 */
package com.skysoft.smart.intranetchat.ui.fragment.main.tool;

import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.activity.calendar.MyCalendarView;
import com.skysoft.smart.intranetchat.ui.activity.weather.WeatherActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class ToolsFragment extends Fragment {

    private ToolsViewModel toolsViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_main_tools, container, /*attachToRoot*/false);
        final View statusBar = root.findViewById(R.id.custom_status_bar_background);
        CustomStatusBarBackground.drawableViewStatusBar(getContext(),R.drawable.custom_gradient_main_title,statusBar);
        final LinearLayout linearWeather = root.findViewById(R.id.tools_linear_weather);
        final LinearLayout linearClock = root.findViewById(R.id.tools__linear_clock);
        final LinearLayout linearMemo = root.findViewById(R.id.tools__linear_calendar);
        final TextView title = root.findViewById(R.id.page_title);
        title.setText(getResources().getText(R.string.ToolsFragment_title_text));

        linearClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()) {
                    Intent clock = new Intent(AlarmClock.ACTION_SET_ALARM);
                    startActivity(clock);
                }
            }
        });
        linearWeather.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (QuickClickListener.isFastClick()) {
                    Intent weather = new Intent(getActivity(), WeatherActivity.class);
                    startActivity(weather);
                }
            }
        });
        linearMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()) {
                    try {
                        Intent intent = new Intent(getActivity(), MyCalendarView.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.toast(getActivity(), getString(R.string.ToolsFragment_linearMemo_Toast));
                    }
                }
            }
        });
        return root;
    }
}