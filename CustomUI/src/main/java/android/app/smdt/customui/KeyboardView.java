package android.app.smdt.customui;

import android.app.smdt.config.SystemConfig;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardView extends PopupWindow  implements View.OnClickListener{
    private static final String TAG = "Keyboard";
    private static final Boolean DebugEnabled = true;
    private static final float LAND_KEYBOARD_WIDTH_RATIO = 0.5f;
    private static final float LAND_KEYBOARD_HEIGHT_RATIO = 0.2f;
    private static final float PORT_KEYBOARD_WIDTH_RATIO = 0.4f;
    private static final float PORT_KEYBOARD_HEIGHT_RATIO = 0.2f;
    private static final int DISPLAY_CONTENT_ID = 0;
    public static final int COMMAND_START_MAIN_ACTIVITY = 1234;
    public static final int COMMAND_START_SETUP_ACTIVITY = 1111;
    private int width;
    private int height;
    private int mScreenWidth;
    private int mScreenHeight;
    private LayoutInflater mInflater;
    private DisplayMetrics metrics;
    private int orient;
    private int xoff,yoff;
    private EditText mPasswordEt;
    private List<CustomKeyboardCallback> mKeyboardCallback;
    public interface CustomKeyboardCallback{
        public void startCustomApp(int command);
    }

    public KeyboardView(Context context,int layoutId){
        mInflater = LayoutInflater.from(context);
        View mView  = mInflater.inflate(layoutId,null);
        orient = SystemConfig.getDisplayOrient(context,DISPLAY_CONTENT_ID);
        metrics = SystemConfig.getDisplayMetricsById(context,DISPLAY_CONTENT_ID);
        mScreenHeight = metrics.heightPixels;
        mScreenWidth = metrics.widthPixels;
        SystemConfig.D(TAG,DebugEnabled,"orient:" + orient + ",width:" +
                mScreenWidth + ",height:" + mScreenHeight);
        if(orient % 2 == 0){
            width = (int)(mScreenWidth * LAND_KEYBOARD_WIDTH_RATIO);
            height = (int)(mScreenHeight * LAND_KEYBOARD_HEIGHT_RATIO);
        }else{
            width = (int)(mScreenWidth * PORT_KEYBOARD_WIDTH_RATIO);
            height = (int)(mScreenHeight * PORT_KEYBOARD_HEIGHT_RATIO);
        }
        xoff = (mScreenWidth - width) / 2;
        yoff = (mScreenHeight - height) / 2 - height/4 ;
        setWidth(width);
        setHeight(height);
        setContentView(mView);
        setFocusable(true);
        setOutsideTouchable(true);
        update();
        setBackgroundDrawable(new ColorDrawable(0x55A0522D));
        showAsDropDown(mView,xoff,yoff);
        ViewGroup mKeyGroup = (ViewGroup)mView;
        for(int id = 0; id < mKeyGroup.getChildCount(); id ++){
            LinearLayout mKeyParent = (LinearLayout)mKeyGroup.getChildAt(id);
            for(int child = 0; child < mKeyParent.getChildCount(); child ++){
                if(mKeyParent.getChildAt(child) instanceof Button) {
                    Button btn = (Button) mKeyParent.getChildAt(child);
                    btn.setOnClickListener(this);
                }else if(mKeyParent.getChildAt(child) instanceof EditText){
                    mPasswordEt = (EditText)mKeyParent.getChildAt(child);
                    mPasswordEt.setText("");
                }
            }
        }
        mKeyboardCallback = new ArrayList<>();
    }
    public void setOnKeyboardListener(CustomKeyboardCallback callback){
        if(mKeyboardCallback != null){
            mKeyboardCallback.add(callback);
        }
    }
    @Override
    public void onClick(View view) {
        String text = ((Button)view).getText().toString();
        String mPassword = mPasswordEt.getText().toString();
        if(text.trim().isEmpty()){
            return;
        }
        if(text.trim().equals("back")){

            int len = mPassword.trim().length();
            SystemConfig.D(TAG,DebugEnabled,"Length:" + len +",password:" + mPassword);
            if(len > 0){
                mPassword = mPassword.substring(0,len -1);
            }
        }else if(text.trim().equals("enter")){
            if(!mPassword.trim().equals("")){
                int command = Integer.parseInt(mPassword.trim());
                mPasswordEt.setText("");
                if(mKeyboardCallback != null && mKeyboardCallback.size() > 0){
                    for(int index = 0; index < mKeyboardCallback.size(); index ++){
                        mKeyboardCallback.get(index).startCustomApp(command);
                    }
                }
                this.dismiss();
            }
        }else{
            mPassword += text;
        }
        mPasswordEt.setText(mPassword);
        SystemConfig.D(TAG,DebugEnabled,"password:" + mPassword);
    }
}
