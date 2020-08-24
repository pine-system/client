package android.app.smdt.customui.image;

import android.app.ActionBar;
import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.utils.utils;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class CustomImageView implements  Runnable{
    //1.通用常量定义
    public static final int MODE_NINE_PICTURE = 3;
    public static final int MODE_FOUR_PICTURE = 2;
    public static final int MODE_FULLSCREEN_PICTURE = 1;
    private static final boolean DebugEnabled = true;
    private static final String TAG = CustomImageView.class.getSimpleName();
    private static final int REFRESH_TIMEOUT_MAX = 200;
    private static final int UPDATE_IMAGE_CONTENT_DEFAULT  = 5 *  1000;

    //2.变量定义
    private static CustomImageView mCustomImageView = null;
    private Handler mHandler;
    private ImageView[] images; //定义一排ImageView,
    private int mImagesCount = 0;
    private FrameLayout.LayoutParams imageParam;
    private int gravity = 0;
    private int nodeWidth,nodeHeight;
    private LinkedList<File> mImageFileList;
    private boolean isRectRefresh =  false;
    private boolean isContentRefresh = false;
    private int sqrtCount = 0;
    private Timer updateImageTimer;
    private int updateImageContentTimeOut;
    private int parentWidth,parentHeight;


    //实现动画效果
    Animation animation;
    private CustomImageView(Context context,FrameLayout parent,int mode){
        //1.获取当前系统的屏幕信息
        DisplayMetrics metrics = SystemConfig.getDisplayMetricsById(context,0);
        int orient = SystemConfig.getDisplayOrient(context,0);
        SystemConfig.D(TAG,DebugEnabled,"metrics:" + metrics +",orient:" + orient);
        //2.根据显示的mode,判断当前显示的布局，全屏、4分屏、9宫格
        if(mode == MODE_NINE_PICTURE) {
            mImagesCount = 9;
        }else if(mode == MODE_FOUR_PICTURE) {
            mImagesCount = 4;
        }else if(mode ==  MODE_FULLSCREEN_PICTURE){
            mImagesCount = 1;
        }
        images = new ImageView[mImagesCount];
        //2.根据分屏信息，设置一个ImageView的宽高。
        sqrtCount = ((int) Math.sqrt(mImagesCount));
        nodeWidth = FrameLayout.LayoutParams.WRAP_CONTENT;
        nodeHeight = FrameLayout.LayoutParams.WRAP_CONTENT;
        animation = AnimationUtils.makeInAnimation(context,true);
     //   TranslateAnimation ta = new TranslateAnimation(
      //          Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,-1f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0F);
      //  ta.setDuration(1000);
       // animation.addAnimation(ta);
        for(int index = 0; index < mImagesCount; index ++) {
            ImageView image = new ImageView(context);
            gravity = utils.getGravity(0,mImagesCount, index);
            imageParam = new FrameLayout.LayoutParams(nodeWidth, nodeHeight, gravity);
            image.setLayoutParams(imageParam);
            image.setBackgroundColor(0xff00ff00 + index * 100);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            parent.addView(image);
            images[index] = image;
        }
        //5.配置线程
        mImageFileList = new LinkedList<>();
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(this);
        updateImageTimer = new Timer();
        updateImageTimer.schedule(updateImageContentTask,0,UPDATE_IMAGE_CONTENT_DEFAULT);
    }

    /**
     * 产生一个实例，配置好基本的信息。
     * @param context
     * @param mode
     * @return
     */
        public static CustomImageView getInstance(Context context,FrameLayout parent,int mode){
            if(mCustomImageView == null){
                mCustomImageView = new CustomImageView(context,parent,mode);
            }
            return mCustomImageView;
        }

        public boolean insertContent(LinkedList<File>resList){
            if(images == null || images.length < 0){
                return false;
            }
            if(resList == null || resList.isEmpty()){
                SystemConfig.D("main",true,"" + resList.size());
                return false;
            }
            mImageFileList = resList;
            return true;
        }
    @Override
    public void run() {
        if(isRectRefresh){
            for(int index = 0; index < mImagesCount; index ++) {
                imageParam = (FrameLayout.LayoutParams) images[index].getLayoutParams();
                imageParam.width = nodeWidth;
                imageParam.height = nodeHeight;
                images[index].setLayoutParams(imageParam);
            }
            isRectRefresh = false;
        }
        if(isContentRefresh){
            for(int index = 0; index < mImagesCount; index ++){
                File mFile = mImageFileList.pop();
              //  images[index].setVisibility(View.INVISIBLE);
                images[index].startAnimation(animation);
                images[index].setImageURI(Uri.fromFile(mFile));
                mImageFileList.addLast(mFile);
            }
            isContentRefresh = false;
        }

        mHandler.postDelayed(this,REFRESH_TIMEOUT_MAX);
    }
         private TimerTask updateImageContentTask = new TimerTask() {
            @Override
            public void run() {
                isContentRefresh = true;
            }
        };

        public void setNodeRect(int parentWidth,int parentHeight){
            if(this.parentWidth != parentWidth || this.parentHeight != parentHeight){
                this.parentWidth = parentWidth;
                this.parentHeight = parentHeight;
                nodeWidth = parentWidth / sqrtCount;
                nodeHeight = parentHeight / sqrtCount;
                isRectRefresh = true;
            }
        }

        public void setUpdateImageContentTimeOut(int timeOut){
            this.updateImageContentTimeOut = timeOut;
             isContentRefresh = true;
        }
}
