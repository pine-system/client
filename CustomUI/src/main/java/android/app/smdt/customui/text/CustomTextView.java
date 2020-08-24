package android.app.smdt.customui.text;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomLayout;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.fonts.FontFamily;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;


public class CustomTextView  extends androidx.appcompat.widget.AppCompatTextView implements  Runnable{
    private static final int CUSTOM_LAYOUT_REFRESH_TIMEOUT_DEFAULT = 200;//刷新率为200毫秒
    private static final int TV_DEFAULT_GRAVITY = Gravity.CENTER;
    private static final int TV_DEFAULT_FONT_COLOR = Color.WHITE;
    private static final int TV_DEFAULT_BACKGROUND_COLOR = Color.BLACK;
    private static final float TV_DEFAULT_FONT_SIZE = 16.0F;
    private static final String TAG = "textLayout";
    private static final Boolean DebugEnable = false;

    private int tvWidth;
    private int tvHeight;
    private int tvBackgroundColor;
    private int tvFontColor;
    private float fontSize;
    private int tvGravity;
    private int marginTop;
    private boolean isTvRefresh;
    private FrameLayout.LayoutParams tvParams;
    private CharSequence title;
    private CustomLayout parent;
    private Handler mHandler;
    //1.本textView的创建
    public CustomTextView(@NonNull Context context, CustomLayout parent,int id) {
        super(context);
        setTvWidth(FrameLayout.LayoutParams.WRAP_CONTENT);
        setTvHeight(FrameLayout.LayoutParams.WRAP_CONTENT);
        setTvFontColor(TV_DEFAULT_FONT_COLOR);
      //  setTvBackgroundColor(TV_DEFAULT_BACKGROUND_COLOR);
        setFontSize(TV_DEFAULT_FONT_SIZE);
        setTvGravity(TV_DEFAULT_GRAVITY);
        tvParams = new FrameLayout.LayoutParams(tvWidth, tvHeight,tvGravity);

        setLayoutParams(tvParams);
        setBackgroundColor(tvBackgroundColor);
        setTextSize(fontSize);
        setTextColor(tvFontColor);
        setSingleLine(true);
        setMarqueeRepeatLimit(-1);
        setEllipsize(TextUtils.TruncateAt.END);
        setId(id);
        this.parent = parent;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(this);
    }
    //2.本textView的修改
    public CharSequence getTvTitle() {
        return title;
    }

    public void setTvTitile(String title) {
            this.title = title;
            isTvRefresh = true;
    }
    public void setTvGravity(int gravity){
        if(this.tvGravity != gravity){
            this.tvGravity = gravity;
            isTvRefresh = true;
        }
    }
    public int getTvWidth() {
        return tvWidth;
    }

    public void setTvWidth(int tvWidth) {
        if (this.tvWidth != tvWidth) {
            this.tvWidth = tvWidth;
            isTvRefresh = true;
        }
    }

    public int getTvHeight() {
        return tvHeight;
    }

    public void setTvHeight(int tvHeight) {
        if (this.tvHeight != tvHeight) {
            this.tvHeight = tvHeight;
            isTvRefresh = true;
        }
    }

    public int getTvBackgroundColor() {
        return tvBackgroundColor;
    }

    public void setTvBackgroundColor(int tvBackgroundColor) {
        if (this.tvBackgroundColor != tvBackgroundColor) {
            this.tvBackgroundColor = tvBackgroundColor;
            isTvRefresh = true;
        }
    }

    public int getTvFontColor() {
        return tvFontColor;
    }

    public void setTvFontColor(int tvFontColor) {
        if (this.tvFontColor != tvFontColor) {
            this.tvFontColor = tvFontColor;
            isTvRefresh = true;
        }
    }

    public void setFontSize(float fontSize) {
        if (this.fontSize != fontSize) {
            this.fontSize = fontSize;
            isTvRefresh = true;
        }
    }
    public void setMarginTop(int marginTop){
        if (this.marginTop != marginTop) {
            this.marginTop = marginTop;
            isTvRefresh = true;
        }
    }
    public void setTvEllipsize(boolean enabled){
        if(enabled){
            this.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);
        }else{
            this.setEllipsize(TextUtils.TruncateAt.END);
            this.setFocusable(false);
            this.setFocusableInTouchMode(false);
        }
    }

    //3.本textView的删除
    public void tvRemove(){
        isTvRefresh = false;
        mHandler.removeCallbacks(this);
        parent.RemoveSubViewIntoCustomLayout(this);
    }
    //4.本textView的插入
    public void tvInsert(){
        parent.InsertSubViewIntoCustomLayout(this);
        isTvRefresh = true;
    }

    //4.本textView的刷新
    private boolean tvRefresh() {
        if (isTvRefresh) {
            setBackgroundColor(tvBackgroundColor);
            setTextSize(fontSize);
            setTextColor(tvFontColor);
            SystemConfig.D(TAG,DebugEnable,"title:" + title);
            setText(this.title);
            tvParams = new FrameLayout.LayoutParams(tvWidth, tvHeight,tvGravity);
            tvParams.topMargin = marginTop;
            setLayoutParams(tvParams);
            isTvRefresh = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        boolean refreshResult = tvRefresh();
        mHandler.postDelayed(this,CUSTOM_LAYOUT_REFRESH_TIMEOUT_DEFAULT);
    }

}

