package android.app.smdt.customui.video;

import android.app.smdt.config.SystemConfig;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Debug;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CustomSurfaceView  extends SurfaceView {
    private static final String TAG = CustomSurfaceView.class.getSimpleName();
    private static final boolean DebugEnabled = true;
    public static final int DATA_MODE_MEDIA_PLAYER_SOURCE = 1;
    public static final int DATA_MODE_CAMERA_SOURCE = 2;
    private SurfaceHolder mHolder;
    private FrameLayout.LayoutParams mParams;
    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private List<Camera.Size> mSupportedPictureSize;
    private List<Camera.Size> mSupportedPreviewSize;
    private List<Camera.Size> mSupportedVideoSize;
    private MediaPlayer mPlayer;
    public CustomSurfaceView(Context context) {
        super(context);
        mHolder = getHolder();
        setZOrderMediaOverlay(false);
        setZOrderOnTop(false);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            return false;
        }
        switch(event.getKeyCode()){
            case KeyEvent.KEYCODE_BACK:
                SystemConfig.D(TAG, DebugEnabled,"press back key.");
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    public void closeCamera(){
        if(mCamera != null) {
            mCamera.stopPreview();
            mHolder.removeCallback(mCameraCallback);
            mCamera.release();
            mCamera = null;
        }
    }
    public void bindCamera(Camera camera){
        if(camera == null){
            return;
        }
        mCameraParameters = camera.getParameters();
        camera.setParameters(mCameraParameters);
        mCamera = camera;
        mHolder.addCallback(mCameraCallback);
    }

    public void stopCamera(Camera camera){
        mCamera.stopPreview();
    }

    private SurfaceHolder.Callback mCameraCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
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

/********************************************************************************************************/
    private LinkedList<File>resList;
    public MediaPlayer getMediaPlayer(){
        return mPlayer;
    }
    public void closePlayer(){
        if(mPlayer != null){
            mHolder.removeCallback(mPlayerCallback);
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }
    public void resetPlayer(){
        if(mPlayer != null){
            mPlayer.reset();
            try {
                File mFile = resList.pop();
                mPlayer.setDataSource(mFile.getAbsolutePath());
                resList.addLast(mFile);
                mPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private MediaPlayer.OnCompletionListener  onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            File mFile = resList.pop();
            SystemConfig.D(TAG,DebugEnabled,"mp completion, mFile:" + mFile.getAbsolutePath());
            try {
                mPlayer.reset();
                mPlayer.setDataSource(mFile.getAbsolutePath());
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                resList.addLast(mFile);
            }
        }
    };
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mPlayer.start();
        }
    };
    public void bindPlayer(MediaPlayer player, LinkedList<File> resList){
        if(player != null){
            mPlayer = player;
            player.setOnPreparedListener(onPreparedListener);
            player.setOnCompletionListener(onCompletionListener);
            mHolder.addCallback(mPlayerCallback);
            this.resList = resList;
        }
    }

    private SurfaceHolder.Callback mPlayerCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            File mFile = resList.getFirst();
            try {
            mPlayer.setDataSource(mFile.getAbsolutePath());
            mPlayer.setDisplay(mHolder);
            mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                resList.addLast(mFile);
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };
}
