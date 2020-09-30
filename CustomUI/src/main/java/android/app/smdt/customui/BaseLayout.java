package android.app.smdt.customui;

import android.app.smdt.config.SystemConfig;
import android.content.Context;
import android.graphics.Color;
import android.os.Debug;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;

public class BaseLayout extends FrameLayout implements Runnable {
    private static String TAG = BaseLayout.class.getSimpleName();
    private static boolean DebugEnabled  = false;

    private static final int CUSTOM_LAYOUT_DEFAULT_WIDTH = LayoutParams.MATCH_PARENT;
    private static final int CUSTOM_LAYOUT_DEFAULT_HEIGHT =  LayoutParams.MATCH_PARENT;
    private static final int CUSTOM_LAYOUT_DEFAULT_GRAVITY = Gravity.CENTER;
    private static final int CUSTOM_LAYOUT_DEFAULT_BACKGROUND_COLOR  = Color.BLACK;
    private static final int CUSTOM_LAYOUT_REFRESH_TIMEOUT_DEFAULT = 200;//刷新率为200毫秒
    private LayoutParams mParams;
    private int CustomLayoutWidth;
    private int CustomLayoutHeight;
    private int CustomLayoutGravity;
    private int CustomLayoutBackgroundColor;
    private boolean isRefresh = false;
    private Handler mHandler;
    private Context mContext;

    private LinkedList<View> subViewList;
    public BaseLayout(@NonNull Context context) {
        super(context);
        init(context);
    }
    public int getCustomLayoutGravity() {
        return CustomLayoutGravity;
    }

    public void setCustomLayoutGravity(int gravity) {
        if(this.CustomLayoutGravity != gravity) {
            this.CustomLayoutGravity = gravity;
            isRefresh = true;
        }
    }

    public int getCustomLayoutWidth() {
        return CustomLayoutWidth;
    }

    public void setCustomLayoutWidth(int customWidth) {
        if(CustomLayoutWidth != customWidth) {
            CustomLayoutWidth = customWidth;
            isRefresh = true;
        }
    }

    public int getCustomLayoutHeight() {
        return CustomLayoutHeight;
    }

    public void setCustomLayoutBackgroundColor(int backgroundColor){
        Log.e("custom","color:" + this.CustomLayoutBackgroundColor + "," + backgroundColor);
        if(this.CustomLayoutBackgroundColor  != backgroundColor){
            this.CustomLayoutBackgroundColor = backgroundColor;
            isRefresh = true;
        }
    }
    public void setCustomLayoutHeight(int customHeight) {
        if(CustomLayoutHeight != customHeight) {
            CustomLayoutHeight = customHeight;
            isRefresh = true;
        }
    }
    private void init(Context context){
        mContext = context;
        setCustomLayoutHeight(CUSTOM_LAYOUT_DEFAULT_HEIGHT);
        setCustomLayoutWidth(CUSTOM_LAYOUT_DEFAULT_WIDTH);
        setCustomLayoutGravity(CUSTOM_LAYOUT_DEFAULT_GRAVITY);
        setCustomLayoutBackgroundColor(CUSTOM_LAYOUT_DEFAULT_BACKGROUND_COLOR);
         mParams = new LayoutParams(CustomLayoutWidth,CustomLayoutHeight,CustomLayoutGravity);
        setLayoutParams(mParams);
        setBackgroundColor(CustomLayoutBackgroundColor);
        subViewList = new LinkedList<>();
        mHandler = new Handler(context.getMainLooper());
        //1.启动主线程
        mHandler.post(this);
    }
    private boolean refreshLayout(){
        if(isRefresh){
            if(mParams != null){
                mParams = new LayoutParams(CustomLayoutWidth,CustomLayoutHeight,CustomLayoutGravity);
                setLayoutParams(mParams);
            }
            setBackgroundColor(CustomLayoutBackgroundColor);
            isRefresh = false;
            return true;
        }else{
            return false;
        }
    }
    private boolean refreshSubView(){
        boolean isOldSubView = false;
        int index;
        int listIndex;
        boolean isValidView = false;
        //1.判断当前的子图的列表是否有数据，如果没有，就将子图全部移除。
        if(subViewList == null || subViewList.size() <= 0){
            if(getChildCount() > 0) {
                removeAllViews();
            }
            return false;
        }
        //2.子view表不为空，而父view中也没有子view,直接将所有的子view表放入到父亲里面
        SystemConfig.D(TAG,DebugEnabled,"builtin view count:" + getChildCount(),"subview list:" + subViewList.size());
        if(getChildCount() == 0 ){
            for(listIndex = 0; listIndex < subViewList.size(); listIndex ++){
                addView(subViewList.get(listIndex));
            }
            return true;
        }

        //3.删除当前在子图列表中没有的图。
        for(index = 0; index < getChildCount(); index ++){
            View mBuiltinSubView = getChildAt(index);
            for(listIndex = 0; listIndex < subViewList.size(); listIndex ++){
                View mSubView = subViewList.get(listIndex);
                if(mSubView.getId() == mBuiltinSubView.getId()){
                    isValidView = true;
                    break;
                }
            }
            if(!isValidView){
                removeView(mBuiltinSubView);
            }else{
                isValidView = false;
            }
        }

        //4.增加当前子图列表中没有的图。
            //3.1.目前view中没有子view;
           if(getChildCount() == 0){
                for(listIndex = 0; listIndex < subViewList.size(); listIndex ++){
                    addView(subViewList.get(listIndex));
                }
                return true;
           }
           //3.2 目前的View中已经有了子view.
        for(listIndex = 0; listIndex < subViewList.size(); listIndex ++){
            View subView = subViewList.get(listIndex);

            if(getChildCount() <= 0){
                addView(subView);
                continue;
            }
            for(index = 0; index < getChildCount(); index ++){
                View mBuiltinView = getChildAt(index);
                if(mBuiltinView.getId() == subView.getId()){
                    isOldSubView = true;
                    break;
                }
            }
            if(!isOldSubView) {
                addView(subView);
            }else{
                isOldSubView = false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        Long time  = System.currentTimeMillis();
        boolean refreshResult = refreshLayout();
        synchronized (subViewList) {
           refreshResult = refreshSubView();
        }
        long timeOut =  System.currentTimeMillis() - time;
        SystemConfig.D(TAG,DebugEnabled,"Refresh result:" + refreshResult + "sub view count:" + getChildCount());
        mHandler.postDelayed(this,CUSTOM_LAYOUT_REFRESH_TIMEOUT_DEFAULT);
    }
    public boolean InsertSubViewIntoCustomLayout(View v){
        boolean isOldView =  false;
        if(v == null || subViewList == null){
            return false;
        }
        synchronized (subViewList) {
            int id = v.getId();
            for (int index = 0; index < subViewList.size(); index++) {
                View subView = subViewList.get(index);
                if (subView.getId() == id) {
                    isOldView = false;
                    return false;
                }

            }
            isOldView = true;
            subViewList.addLast(v);
        }
        return isOldView;
    }
    public boolean RemoveSubViewIntoCustomLayout(View v){
        boolean isOldView = false;
        if(v == null || subViewList == null || subViewList.isEmpty()){
            return false;
        }
        synchronized (subViewList) {
            for (int index = 0; index < subViewList.size(); index++) {
                View subView = subViewList.get(index);
                if (subView.getId() == v.getId()) {
                    subViewList.remove(subView);
                    return true;
                }
            }
        }
        return false;
    }
}
