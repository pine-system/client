package android.app.smdt.customui.subui.video;

import android.app.smdt.customui.SubLayout;
import android.app.smdt.customui.subui.image.CustomFullPicture;
import android.app.smdt.customui.subui.image.CustomPicture;
import android.app.smdt.customui.subui.utils.utils;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MyPlayer extends SurfaceView {
    private static final String TAG = MyPlayer.class.getName();
    private static final boolean Debug = true;

    private SurfaceHolder mHolder;
    private MediaPlayer mPlayer;
    private LinkedList<MyPlayerRes>mResList;
    private MyPlayerRes mCurPlayerRes;
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mCurPlayerRes = mResList.pop();
            mResList.addLast(mCurPlayerRes);
            try {
                mediaPlayer.setDataSource(mCurPlayerRes.getmPlayerPath());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

        }
    };
    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener =  new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {

        }
    };
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            mediaPlayer.reset();
            return false;
        }
    };
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mCurPlayerRes.refreshImage();
            mediaPlayer.start();
        }
    };
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {

        }
    };

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            mPlayer.setDisplay(surfaceHolder);
            if(!mResList.isEmpty()){
                mCurPlayerRes = mResList.pop();
                String mFilePath = mCurPlayerRes.getmPlayerPath();
                mResList.addLast(mCurPlayerRes);
                try {
                    mPlayer.setDataSource(mFilePath);
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            mPlayer.reset();
            mPlayer.release();
        }
    };


    public MyPlayer(Context context, JSONObject o, List<AbsoluteLayout> mImageList) {
        super(context);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        mResList = new LinkedList<>();
        try {
            JSONArray arr = o.getJSONArray("link");
            for(int index = 0; index < arr.length(); index ++){
                MyPlayerRes mRes = new MyPlayerRes(arr.getJSONObject(index),mImageList);
                mResList.addLast(mRes);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mHolder = getHolder();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(mOnPreparedListener);
        mPlayer.setOnCompletionListener(mOnCompletionListener);
        mPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mPlayer.setOnErrorListener(mOnErrorListener);
        mPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mHolder.addCallback(mCallback);
    }

    public void onDestroy(){
        if(mPlayer != null){
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        if(mResList == null){
            mResList.clear();
            mResList = null;
        }
    }
    class MyPlayerRes{
        private String mPlayerPath;
        private CustomFullPicture mImageLayout;
        private LinkedList<Drawable>mLinkDrawableList;
        public MyPlayerRes(JSONObject o,List<AbsoluteLayout>mImageList){
            mLinkDrawableList = new LinkedList<>();
            try {
                mPlayerPath = o.getString("path");
                int mImageID = o.getInt("connect");
                if(!mImageList.isEmpty()){
                  for(int index = 0; index < mImageList.size(); index ++){
                      AbsoluteLayout Layout = mImageList.get(index);
                      if(Layout.getId() == mImageID + SubLayout.CUSTOM_WINDOW_TYPE_IMAGE){
                         mImageLayout = (CustomFullPicture)Layout.getChildAt(0);
                      }
                  }
                }
                if(mImageLayout != null){
                    JSONArray imageArr = o.getJSONArray("list");
                    for(int index = 0; index < imageArr.length(); index ++){
                        String imagePath = utils.SDCARD_RES_PATH +  imageArr.getJSONObject(index).getString("path");
                        mLinkDrawableList.addLast(utils.Bitmap2Drawable(imagePath));
                    }
                    mImageLayout.Insert(mLinkDrawableList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        public String getmPlayerPath(){
            return mPlayerPath;
        }
        public void refreshImage(){

        }
    }
}
