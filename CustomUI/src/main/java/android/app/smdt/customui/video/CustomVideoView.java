package android.app.smdt.customui.video;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomViewGroup;
import android.app.smdt.customui.utils.utils;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class CustomVideoView implements Runnable{
    private static final int RECT_REFRESH_TIMEOUT = 200;
    public static final int VIDEO_FULL_SCREEN = 1;
    public static final int VIDEO_FOUR_SCREEN = 2;

    private static final String TAG = CustomVideoView.class.getSimpleName();
    private static final boolean DebugEnabled = true;
    private static final int VIDEO_MAX = 4;

    private static CustomVideoView mVideoView = null;
    private CustomSurfaceView[]  mSurfaceViews;
    private int mCurSurface = 0;
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

    private LinkedList<File>mResList;
    private CustomVideoView(Context context, FrameLayout parent, int type,int mode){
        this.type = type;
        //1.根据模式mode判断是全屏还是九宫格
        if(mode == VIDEO_FULL_SCREEN){
            totalNodeNum = 1;
            sqrtNodeNum = 1;
        }else{
            totalNodeNum = VIDEO_MAX;
            sqrtNodeNum = 2;
        }
        //2.准备surface.
        mSurfaceViews = new CustomSurfaceView[totalNodeNum];
        nodeWidth = FrameLayout.LayoutParams.WRAP_CONTENT;
        nodeHeight = FrameLayout.LayoutParams.WRAP_CONTENT;
        for(int index = 0; index < totalNodeNum; index ++) {
            gravity = utils.getGravity(0,totalNodeNum,index);
            mParams = new FrameLayout.LayoutParams(nodeWidth,nodeHeight,gravity);
            CustomSurfaceView  mSurface = new CustomSurfaceView(context);
            mSurfaceViews[index] = mSurface;
            parent.addView(mSurface,mParams);
        }
        //3.启动线程
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(this);
    }
    public static CustomVideoView getInstance(Context context,FrameLayout parent,int type,int mode){
        if(mVideoView == null){
            mVideoView = new CustomVideoView(context,parent,type,mode);
        }
        return mVideoView;
    }
    public void setVideoRect(int parentWidth,int parentHeight){
        if(this.parentWidth != parentWidth || this.parentHeight != parentHeight){
            this.parentWidth = parentWidth;
            this.parentHeight = parentHeight;
            nodeWidth = parentWidth / sqrtNodeNum;
            nodeHeight = parentHeight / sqrtNodeNum;
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

    public boolean insertContent(LinkedList<File>resList){
        for(int i = 0; i < mSurfaceViews.length; i++){
            CustomSurfaceView surface = mSurfaceViews[i];
            if(surface.getMediaPlayer() != null){
                surface.resetPlayer();
            }else{
                MediaPlayer player = new MediaPlayer();
                surface.bindPlayer(player,resList);
            }
        }
        return true;
    }


}
