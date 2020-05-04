package com.skysoft.smart.intranetchat.tools.customstatusbar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.skysoft.smart.intranetchat.R;

public class CustomStatusBarBackground {
    public static void customStatusBarTransparent(Activity activity){
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
    public static void drawableViewStatusBar(Context context, int drawable, View view){
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        TLog.d("TransparencyView", "statusBarHeight: "+statusBarHeight);
        ViewGroup.LayoutParams customStatusBar = view.getLayoutParams();
        customStatusBar.height = statusBarHeight;
        view.setBackground(context.getResources().getDrawable(drawable));
    }
    public static void colorViewStatusBar(Context context, int color, View view){
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        TLog.d("TransparencyView", "statusBarHeight: "+statusBarHeight);
        ViewGroup.LayoutParams customStatusBar = view.getLayoutParams();
        customStatusBar.height = statusBarHeight;
        view.setBackground(context.getResources().getDrawable(color));
    }
}
