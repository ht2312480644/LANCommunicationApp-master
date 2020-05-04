package com.skysoft.smart.intranetchat.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.R;

public class CreateNotifyBitmap {
    private static final String TAG = "CreateNotifyBitmap";

    /**
     * 创建一个bitmap，用于inputMessage中显示@***
     * @param context 上下文
     * @param notify @+名字
     * @return 创建成功的bitmap*/
    public static Bitmap notifyBitmap(Context context, String notify){
        float sp = DisplayUtil.sp2px(context,context.getResources().getDimension(R.dimen.sp_6)-2);
        TLog.d(TAG, "onClickReplay: sp = " + sp);
        Paint paint = new Paint();
        paint.setTextSize(sp);
        paint.setColor(context.getResources().getColor(R.color.color_white));
        paint.setTextAlign(Paint.Align.LEFT);
        int width = (int) paint.measureText(notify);
        int height = (int) (sp+4);
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(context.getResources().getColor(R.color.color_blue));

        canvas.drawText(notify.trim(),0,height-4,paint);
        canvas.save();
        canvas.restore();
        return bitmap;
    }
}
