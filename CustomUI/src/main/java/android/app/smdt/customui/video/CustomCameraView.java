package android.app.smdt.customui.video;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomViewGroup;
import android.app.smdt.customui.utils.utils;
import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.widget.FrameLayout;

public class CustomCameraView implements Runnable {
    //1.常量定义
    private static final String TAG = CustomVideoView.class.getSimpleName();
    private static final boolean DebugEnabled = true;
    private static final int RECT_REFRESH_TIMEOUT = 200;
    public static final int CAMERA_FULL_SCREEN = 1;
    public static final int CAMERA_TWO_SCREEN = 2;
    public static final int CAMERA_FOUR_SCREEN = 3;
    private static final int CAMERA_MAX = 4;

    private static CustomCameraView mVideoView = null;
    private CustomSurfaceView[]  mSurfaceViews;
    private Camera[]  mCameras;
    private FrameLayout.LayoutParams mParams;
    private int parentWidth;
    private int parentHeight;
    private int nodeWidth;
    private int nodeHeight;
    private int gravity;
    private int sqrtNodeNum;
    private int totalNodeNum;

    private int type;
    private Handler mHandler;
    private boolean isRectRefresh = false;
    private int mScreenOrient;
    private int mCurSurface;
    private CustomCameraView(Context context, FrameLayout parent, int type, int mode){
        mScreenOrient = SystemConfig.getDisplayOrient(context,0);
        this.type = type;
        //1.根据模式mode判断是全屏还是2分屏、4分屏
        if(mode == CAMERA_FULL_SCREEN){
            totalNodeNum = 1;
        }else if(mode == CAMERA_TWO_SCREEN) {
            totalNodeNum = 2;
        }else if(mode == CAMERA_FOUR_SCREEN){
            totalNodeNum = CAMERA_MAX;
        }
        //2.准备surface.
        mSurfaceViews = new CustomSurfaceView[totalNodeNum];
        nodeWidth = FrameLayout.LayoutParams.WRAP_CONTENT;
        nodeHeight = FrameLayout.LayoutParams.WRAP_CONTENT;
        for(int index = 0; index < totalNodeNum; index ++) {
            if(totalNodeNum == 2 && (mScreenOrient == 2 || mScreenOrient == 0)){
                gravity = utils.getGravity(utils.GRAVITY_HORIZENTOR,totalNodeNum,index);
            }else if(totalNodeNum == 2 &&(mScreenOrient == 1 || mScreenOrient == 3)){
                gravity = utils.getGravity(utils.GRAVITY_VERTICAL,totalNodeNum,index);
            }else {
                gravity = utils.getGravity(utils.GRAVITY_HORIZENTOR, totalNodeNum, index);
            }
            mParams = new FrameLayout.LayoutParams(nodeWidth,nodeHeight,gravity);
            CustomSurfaceView  mSurface = new CustomSurfaceView(context);
            mSurfaceViews[index] = mSurface;
            parent.addView(mSurface,mParams);
        }
        //3.启动线程
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(this);
    }
    public static CustomCameraView getInstance(Context context,FrameLayout parent,int type,int mode){
        if(mVideoView == null){
            mVideoView = new CustomCameraView(context,parent,type,mode);
        }
        return mVideoView;
    }
    public void setVideoRect(int parentWidth,int parentHeight){
        if(this.parentWidth != parentWidth || this.parentHeight != parentHeight){
            this.parentWidth = parentWidth;
            this.parentHeight = parentHeight;
            if(totalNodeNum == 1 && (mScreenOrient == 0 || mScreenOrient == 2)){
                nodeWidth = parentWidth / 2;
                nodeHeight = parentHeight;
            }else if(totalNodeNum == 1 && (mScreenOrient == 1 || mScreenOrient == 3)){
                nodeWidth = parentWidth;
                nodeHeight = parentHeight / 2;
            }else {
                nodeWidth = parentWidth / sqrtNodeNum;
                nodeHeight = parentHeight / sqrtNodeNum;
            }
            isRectRefresh = true;
        }
    }

    @Override
    public void run() {
        if(isRectRefresh){
            for(int index = 0; index < mSurfaceViews.length; index ++){
                CustomSurfaceView mSurface  = mSurfaceViews[index];
                mParams =  (FrameLayout.LayoutParams)mSurface.getLayoutParams();
                mParams.width = nodeWidth;
                mParams.height = nodeHeight;
                mSurface.setLayoutParams(mParams);
            }
            isRectRefresh = false;
        }
        mHandler.postDelayed(this,RECT_REFRESH_TIMEOUT);
    }
    public boolean insertCamera(){
        if(type != CustomViewGroup.CUSTOM_TYPE_CAMERA){
            return false;
        }
        //1.进行逻辑判断
        if(totalNodeNum < 0 || totalNodeNum > CAMERA_MAX){
            SystemConfig.D(TAG,DebugEnabled,"insertCameraIntoCustomSurfaceViewById,index is error");
            return false;
        }
        int CameraCount = Camera.getNumberOfCameras();
        if(CameraCount <= 0 || CameraCount > totalNodeNum){
            SystemConfig.D(TAG,DebugEnabled,"insertCameraIntoCustomSurfaceViewById,camera index is error" ,"Camera count:" + CameraCount);
            return false;
        }
        //2.创建对应的相机
        mCameras = new Camera[CameraCount];
        for(int index = 0; index < CameraCount; index ++){
            mCameras[index] = Camera.open(index);
            CustomSurfaceView surface = mSurfaceViews[index];
            surface.closeCamera();
            if(surface == null){
                return false;
            }
            surface.bindCamera(mCameras[index]);
        }
        return true;
    }

}
