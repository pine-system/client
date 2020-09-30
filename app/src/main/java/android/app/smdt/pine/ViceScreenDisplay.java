package android.app.smdt.pine;

import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

public class ViceScreenDisplay{
    private static final String TAG = "vice";
    private static final boolean DebugEnabled = true;
    public static final int VICE_SCREEN_INDEX = 1;
    private DisplayManager mDM;
    private Display[] mDisplays;
    private DifferentDisplay mDifferentDisplay;

    public ViceScreenDisplay(Context context, int index, ViewGroup viceLayout){
        mDM = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        mDisplays = mDM.getDisplays();
        Display mDisplay = mDisplays[index];
        mDifferentDisplay = new DifferentDisplay(context,mDisplay,viceLayout);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mDifferentDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY); //增加后出现权限错误
        }else{
            mDifferentDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

        }
        mDifferentDisplay.show();
    }
    public void onDestroy(){
        mDifferentDisplay = null;
    }

    class DifferentDisplay extends Presentation{
        ViewGroup mViceLayout;
        public DifferentDisplay(Context outerContext, Display display,ViewGroup viceLayout) {
            super(outerContext, display);
            mViceLayout = viceLayout;
        }
        public DifferentDisplay(Context outerContext, Display display, int theme) {
            super(outerContext, display, theme);
        }
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(mViceLayout);
        }
    }
}
