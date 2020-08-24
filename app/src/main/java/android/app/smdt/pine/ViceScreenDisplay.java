package android.app.smdt.pine;

import android.app.Presentation;
import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomMultiLayout;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class ViceScreenDisplay implements CustomMultiLayout.MainPlayerCallback {
    private static final String TAG = "vice";
    private static final boolean DebugEnabled = true;
    private static final int VICE_SCREEN_NUM = 1;
    private DisplayManager mDM;
    private Display[] mDisplays;
    private DifferentDisplay mDifferentDisplay;
    private LinkedList<File>mFileList;
    private LinkedList<File>mImageList;
    public ViceScreenDisplay(Context context, LinkedList<File>mFileList,LinkedList<File>mImageList){
        mDM = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        mDisplays = mDM.getDisplays();

        SystemConfig.D(TAG,DebugEnabled,"Display num:" + mDisplays.length);
        if(mDisplays.length < 2){
            return;
        }
        this.mFileList = mFileList;
        this.mImageList = mImageList;
        Display mDisplay = mDisplays[VICE_SCREEN_NUM];


        mDifferentDisplay = new DifferentDisplay(context,mDisplay);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mDifferentDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY); //增加后出现权限错误
        }else{
            mDifferentDisplay.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

        }
        mDifferentDisplay.show();
       // SystemConfig.D(TAG,DebugEnabled,mDifferentDisplay.getDisplay().getWidth() +":" + mDifferentDisplay.getDisplay().getHeight());
       // SystemConfig.D(TAG,DebugEnabled,mDisplays[0].getWidth() +":" + mDisplays[0].getHeight());
    }
    public void onDestroy(){
        mDifferentDisplay.onDestroy();
        mDifferentDisplay = null;
    }

    @Override
    public void mainPlayerCompleted() {
        mDifferentDisplay.changeImage();
    }

    class DifferentDisplay extends Presentation implements Runnable{
        private ImageView mImage;
        private SurfaceView mSurfaceView;
        private SurfaceHolder mHolder;
        private MediaPlayer mPlayer;
        private Handler mHandler;
        public DifferentDisplay(Context outerContext, Display display) {
            super(outerContext, display);
        }

        public DifferentDisplay(Context outerContext, Display display, int theme) {
            super(outerContext, display, theme);
        }
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.different_layout);

            mImage = (ImageView)findViewById(R.id.vice_image);
            mSurfaceView = (SurfaceView)findViewById(R.id.vice_play);
           // mSurfaceView.setVisibility(View.VISIBLE);
            mHolder = mSurfaceView.getHolder();
            mPlayer = new MediaPlayer();
            mPlayer.setOnPreparedListener(mPreparedListener);
            mPlayer.setOnSeekCompleteListener(mSeekCompletedListener);
            mPlayer.setOnCompletionListener(mCompletionListener);
            mHolder.addCallback(mCallback);
            mHandler = new Handler(getContext().getMainLooper());
            mHandler.post(this);
        }

        private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayer.start();
            }
        };
        private MediaPlayer.OnSeekCompleteListener mSeekCompletedListener = new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {


            }
        };
        private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(mp != null){
                    mp.reset();
                }
                File mFile = mFileList.pop();
                mFileList.addLast(mFile);
                SystemConfig.D(TAG,DebugEnabled,"new File name:" + mFile.getAbsolutePath());
                try {
                    mPlayer.setDataSource(mFile.getAbsolutePath());
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mPlayer.setDisplay(mHolder);
                File mFile = mFileList.pop();
                mFileList.addLast(mFile);
                try {
                    mPlayer.setDataSource(mFile.getAbsolutePath());
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        };
        public void changeImage(){
            if(mImageList != null && !mImageList.isEmpty()){
                File mImageFile = mImageList.pop();
                mImage.setImageURI(Uri.parse(mImageFile.getAbsolutePath()));
                mImageList.addLast(mImageFile);
            }
        }
        @Override
        public void run() {
            if(mImageList != null && !mImageList.isEmpty()){
                File mImageFile = mImageList.pop();
                mImage.setImageURI(Uri.parse(mImageFile.getAbsolutePath()));
                mImageList.addLast(mImageFile);
            }
           // mHandler.postDelayed(this,5 * 1000);
        }
        public void onDestroy(){
            if(mPlayer != null){
                mPlayer.reset();
                mPlayer.release();
                mPlayer = null;
            }
            mHandler.removeCallbacks(this);
            hide();
        }
    }
}
