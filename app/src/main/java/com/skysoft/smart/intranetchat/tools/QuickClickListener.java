package com.skysoft.smart.intranetchat.tools;

public class QuickClickListener {
    private static final int FAST_CLICK_DELAY_TIME = 999;
   private static long lastClickTime = 0;


    public static boolean isFastClick() {
        return isFastClick(FAST_CLICK_DELAY_TIME);
    }

    public static boolean isFastClick(long time) {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) <= time ) {
            flag = false;
        }else
            lastClickTime = currentClickTime;
        return flag;
    }
}
