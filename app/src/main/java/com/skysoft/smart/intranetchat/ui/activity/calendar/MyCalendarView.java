package com.skysoft.smart.intranetchat.ui.activity.calendar;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;

import java.util.Calendar;

public class MyCalendarView extends AppCompatActivity {

    final private static String TAG = "Toby_Test";
    private Toast sToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_calendar);

        final CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        final Calendar c = Calendar.getInstance();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView,
                                            int year, int month, int dayOfMonth) {
                ToastUtil.toast(MyCalendarView.this,
                        (getText(R.string.CalendarView_selectData_text) + " " +
                        String.valueOf(year) + "/" +
                        String.valueOf(month+1) + "/" +
                        String.valueOf(dayOfMonth)));
            }
        });
    }

}