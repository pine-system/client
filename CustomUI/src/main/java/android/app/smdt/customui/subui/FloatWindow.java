package android.app.smdt.customui.subui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Button;

public class FloatWindow {

    public static final int FLOAT_WINDOW_TYPE_DIALOG = 1;
    public static final int FLOAT_WINDOW_TYPE_ALERT_WINDOW = 2;

    private static final int FLOAT_WINDOW_WIDTH_DEFAULT = 400;
    private static final int FLOAT_WINDOW_HEIGHT_DEFAULT = 300;

    private static FloatWindow mFloatWin = null;

    private LayoutInflater mInflater;
    private WindowManager mWM;
    private WindowManager.LayoutParams wmParams;

    public FloatWindow(Context context,int fwType,int layoutId){
        // 获取WindowManager服务
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // 新建悬浮窗控件
        Button button = new Button(context);
        button.setText("Floating Window");
        button.setBackgroundColor(Color.BLUE);

        // 设置LayoutParam
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = 500;
        layoutParams.height = 100;
        layoutParams.x = 300;
        layoutParams.y = 300;

        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(button, layoutParams);
    }
    private static Activity a;
    public static FloatWindow makeInstance(Context context,int layoutId){
        a = (Activity)context;
        if(mFloatWin == null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!Settings.canDrawOverlays(context)){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.getPackageName()));

                   a.startActivityForResult(intent, 23);
                }
            }
            mFloatWin = new FloatWindow(context,FLOAT_WINDOW_TYPE_ALERT_WINDOW,layoutId);
        }
        return mFloatWin;
    }

}
