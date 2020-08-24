package android.app.smdt.customui;

import android.annotation.SuppressLint;
import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.image.CustomImageView;
import android.app.smdt.customui.video.CustomCameraView;
import android.app.smdt.customui.video.CustomVideoView;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *1.创建一个自带父级的viewGroup.
 */

public class CustomViewGroup extends FrameLayout implements Runnable{
    private static final String TAG = CustomViewGroup.class.getSimpleName();
    private static final Boolean DebugEnabled = true;
    private static final int CUSTOM_IMAGE_LAYOUT_TIMEOUT_MAX = 200;
    public static final int CUSTOM_TYPE_IMAGE = 1;
    public static final int CUSTOM_TYPE_VIDEO = 2;
    public static final int CUSTOM_TYPE_CAMERA = 3;
    public static final int DISPLAY_MODE_FULL_SCREEN =  CustomImageView.MODE_FULLSCREEN_PICTURE;
    public static final int DISPLAY_MODE_FOUR_SCREEN = CustomImageView.MODE_FOUR_PICTURE;
    public static final int DISPLAY_MODE_NINE_SCREEN = CustomImageView.MODE_NINE_PICTURE;
    private static final int LAYOUT_WIDTH_DEFAULT = LayoutParams.MATCH_PARENT;
    private static final int LAYOUT_HEIGHT_DEFAULT = LayoutParams.MATCH_PARENT;
    private static final int IMAGE_DEFAULT_GRAVITY = Gravity.CENTER;
    private static final int IMAGE_DEFAULT_BACKGROUND_COLOR = Color.BLACK;



    private Handler mHandler;
    private FrameLayout.LayoutParams mParams;
    private int layoutWidth;
    private int layoutHeight;
    private int layoutGravity;
    private int layoutBackColor;
    private boolean isLayoutRefresh;
    private Context context;

    private int type;
    private int displayMode;
    private CustomImageView mCustomImageView;
    private CustomVideoView mCustomVideoView;
    private CustomCameraView mCustomCameraView;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        layoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        layoutHeight =  MeasureSpec.getSize(heightMeasureSpec);
        if(type == CUSTOM_TYPE_IMAGE) {
            mCustomImageView.setNodeRect(layoutWidth, layoutHeight);
        }else if(type == CUSTOM_TYPE_VIDEO ){
            mCustomVideoView.setVideoRect(layoutWidth,layoutHeight);
        }else if(type == CUSTOM_TYPE_CAMERA){
            mCustomCameraView.setVideoRect(layoutWidth,layoutHeight);
        }
    }
    public CustomViewGroup(Context context, CustomLayout parent){
        super(context);
    }
    public CustomViewGroup(Context context, CustomLayout parent,JSONObject json){
        this(context,parent);
    }
    public CustomViewGroup(@NonNull Context context, CustomLayout parent,int type,int displayMode) {
        super(context);
        this.type = type;
        this.displayMode = displayMode;
        this.context = context;
        setLayoutWidth(LAYOUT_WIDTH_DEFAULT);
        setLayoutHeight(LAYOUT_HEIGHT_DEFAULT);
        setLayoutGravity(IMAGE_DEFAULT_GRAVITY);
        setLayoutBackColor(IMAGE_DEFAULT_BACKGROUND_COLOR);
        mParams = new FrameLayout.LayoutParams(layoutWidth,layoutHeight,Gravity.CENTER);
        setLayoutParams(mParams);
        setBackgroundColor(layoutBackColor);
        isLayoutRefresh = true;
        mHandler = new Handler(context.getMainLooper());
        parent.InsertSubViewIntoCustomLayout(this);
        if(type == CUSTOM_TYPE_IMAGE) {
            mCustomImageView = CustomImageView.getInstance(context, this, displayMode);
        }else if(type == CUSTOM_TYPE_VIDEO){
            mCustomVideoView = CustomVideoView.getInstance(context,this,type,displayMode);
        }else if(type == CUSTOM_TYPE_CAMERA){
            mCustomCameraView = CustomCameraView.getInstance(context,this,type,displayMode);
        }
        mHandler.post(this);
    }

    public void run() {
        if(isLayoutRefresh){
            mParams = new LayoutParams(layoutWidth,layoutHeight,layoutGravity);
            setLayoutParams(mParams);
            setBackgroundColor(layoutBackColor);
            isLayoutRefresh = false;
        }
        mHandler.postDelayed(this,CUSTOM_IMAGE_LAYOUT_TIMEOUT_MAX);
    }

    public void setLayoutWidth(int layoutWidth) {
        this.layoutWidth = layoutWidth;
        isLayoutRefresh = true;
    }

    public void setLayoutHeight(int layoutHeight) {
        this.layoutHeight = layoutHeight;
        isLayoutRefresh = true;
    }

    public void setLayoutGravity(int layoutGravity) {
        this.layoutGravity = layoutGravity;
        isLayoutRefresh = true;
    }

    public void setLayoutBackColor(int layoutBackColor) {
        this.layoutBackColor = layoutBackColor;
        isLayoutRefresh = true;
    }
    public void insertImageContent(LinkedList<File>resList) {
        if (resList == null || resList.isEmpty()) {
            SystemConfig.D(TAG, DebugEnabled, "resource list is empty.");
            return;
        }
        mCustomImageView.insertContent(resList);
    }
    public void insertVideoContent(LinkedList<File>resList) {
        if (resList == null || resList.isEmpty()) {
            SystemConfig.D(TAG, DebugEnabled, "resource list is empty.");
            return;
        }
        mCustomVideoView.insertContent(resList);
    }
    public void insertCameraContent(){
        mCustomCameraView.insertCamera();
    }
























}
