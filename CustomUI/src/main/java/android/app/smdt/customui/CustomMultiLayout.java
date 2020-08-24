package android.app.smdt.customui;

import android.Manifest;
import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.text.Title;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 1.这个布局中存在多个SurfaceView和多个imageView.同时多个
 * 2.每个surfaceView可以播放多个视频，
 * 3.每个视频可能关联多个图片。
 * 4.每一个图片需要指定对应的imageView来播放
 * 5.整体播放原则：以视频播放为准。
 */
public class CustomMultiLayout extends AbsoluteLayout {
    private static final String TAG = "multi";
    private static final boolean DebugEnabled = true;
    //1.布局的名称
    private String name;
    private int id;
    //1.定义多个SurfaceView
    //private List<VideoFrame> mVideoList;
    private List<AbsoluteLayout> mVideoList;
    private List<AbsoluteLayout> mCameraLayoutList;
    //2.定义多个ImageView;
    private List<ImageFrame> mImageList;
    private JSONObject mLayoutJSON;
    private BufferedReader mReader;
    private CustomLayout main;
    private Title.CustomText mLogTitle;

    public interface MainPlayerCallback {
        public void mainPlayerCompleted();
    }

    private MainPlayerCallback mainPlayerCallback;

    public void setPlayerListener(MainPlayerCallback callback) {
        mainPlayerCallback = callback;
    }

    public CustomMultiLayout(Context context, ViewGroup parent, File mLayouFile, Title.CustomText log) {
        super(context);
        if (DebugEnabled) {
            mLogTitle = log;
            // mLogTitle.updateTitle(mLayouFile.getAbsolutePath());
        }
        //1.定义多层绝对布局
        setLayoutParams(new AbsoluteLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
        main = (CustomLayout) parent;
        main.InsertSubViewIntoCustomLayout(this);
        //2.初始化视频层、图片层列表
        mVideoList = new ArrayList<>();
        mImageList = new ArrayList<>();
        mCameraLayoutList = new ArrayList<>();
        //3.从json文件解析视频、图片信息
        layoutVideoFilePaser(context, mLayouFile);
        //4.加入到多层布局中
        for (int index = 0; index < mVideoList.size(); index++) {
            //SystemConfig.D(TAG,DebugEnabled,mVideoList.get(index).toString());
            addView(mVideoList.get(index));
        }
        for (int index = 0; index < mImageList.size(); index++) {
            addView(mImageList.get(index));
        }
        for (int index = 0; index < mCameraLayoutList.size(); index++) {
            addView(mCameraLayoutList.get(index));
        }

    }

    public void onDestroy() {
        //1.删除视频层的列表
        if (mVideoList != null) {
            for (int index = 0; index < mVideoList.size(); index++) {
                VideoFrame frame = (VideoFrame) mVideoList.get(index);
                frame.onDestroy();
                removeView(frame);
            }
            mVideoList.clear();
            mVideoList = null;
        }
        //2.删除图片层的列表
        if (mImageList != null) {
            for (int index = 0; index < mImageList.size(); index++) {
                ImageFrame frame = mImageList.get(index);
                frame.onDestroy();
                removeView(frame);
            }
            mImageList.clear();
            mImageList = null;
        }
        //3.删除相机层的列表
        if (mCameraLayoutList != null) {
            for (int index = 0; index < mCameraLayoutList.size(); index++) {
                CameraFrame frame = (CameraFrame) mCameraLayoutList.get(index);
                frame.onDestroy();
                removeView(frame);
            }
            mCameraLayoutList.clear();
            mCameraLayoutList = null;
        }
    }

    private void layoutVideoFilePaser(Context context, File layoutFile) {
        //0:文件来自assets目录

        //1.读取全部该文件，转换成json
        try {
            mReader = new BufferedReader(new FileReader(layoutFile));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = mReader.readLine()) != null) {
                sb.append(line);
            }
            mReader.close();
            SystemConfig.D(TAG, DebugEnabled, sb.toString());
            mLogTitle.updateTitle(sb.toString());
            mLayoutJSON = new JSONObject(sb.toString().trim());

            //2.解析json文件
            //2.1获取name 和 id
            name = mLayoutJSON.getString("name");
            id = mLayoutJSON.getInt("id");
            SystemConfig.D(TAG, DebugEnabled, name + ":" + id);
            mLogTitle.updateTitle("name:" + name + ",id:" + id);
            //3.解析image图片的布局
            JSONArray imageLayoutArray = mLayoutJSON.getJSONArray("image_layout_array");
            for (int imageLayoutArrayIndex = 0; imageLayoutArrayIndex < imageLayoutArray.length(); imageLayoutArrayIndex++) {
                JSONObject imageLayout = (JSONObject) imageLayoutArray.get(imageLayoutArrayIndex);

                ImageFrame mImageFrame = new ImageFrame(
                        context,
                        imageLayout.getInt("id"),
                        imageLayout.getInt("left"),
                        imageLayout.getInt("top"),
                        imageLayout.getInt("width"),
                        imageLayout.getInt("height")
                );
                if (imageLayout.has("images")) {
                    JSONArray mImageBuiltinLayoutArray = imageLayout.getJSONArray("images");
                    for (int imageBuiltinLayoutArrayIndex = 0; imageBuiltinLayoutArrayIndex < mImageBuiltinLayoutArray.length(); imageBuiltinLayoutArrayIndex++)
                        mImageFrame.insertImage((String) mImageBuiltinLayoutArray.get(imageBuiltinLayoutArrayIndex));
                }
                SystemConfig.D(TAG, DebugEnabled, "image frame:" + imageLayoutArrayIndex + "," + mImageFrame.toString());
                mImageList.add(mImageFrame);
            }

            //2 解析视频布局的数组
            JSONArray videoLayoutArray = mLayoutJSON.getJSONArray("video_layout_array");
            SystemConfig.D(TAG, DebugEnabled, "array size:" + videoLayoutArray.toString());
            for (int videoLayoutArrayIndex = 0; videoLayoutArrayIndex < videoLayoutArray.length(); videoLayoutArrayIndex++) {
                JSONObject mVideoLayout = (JSONObject) videoLayoutArray.get(videoLayoutArrayIndex);
                SystemConfig.D(TAG, DebugEnabled, "[" + videoLayoutArrayIndex + "]:" + mVideoLayout.toString());
                //2.1获取视频布局的尺寸。
                JSONObject mVideoLayoutRect = mVideoLayout.getJSONObject("video_layout_rect");

                String type = mVideoLayoutRect.getString("type");
                // SystemConfig.D(TAG,DebugEnabled,"video layout index:" + videoLayoutArrayIndex + ",type:" + type);
                AbsoluteLayout frame = null;
                if (type.equals("video")) {
                    //VideoFrame frame = new VideoFrame(
                    frame = new VideoFrame(
                            context,
                            videoLayoutArrayIndex + 10000,
                            mVideoLayoutRect.getInt("left"),
                            mVideoLayoutRect.getInt("top"),
                            mVideoLayoutRect.getInt("width"),
                            mVideoLayoutRect.getInt("height")
                    );
                } else if (type.equals("camera")) {
                    frame = new CameraFrame(
                            context,
                            mVideoLayoutRect.getInt("id"),
                            mVideoLayoutRect.getInt("left"),
                            mVideoLayoutRect.getInt("top"),
                            mVideoLayoutRect.getInt("width"),
                            mVideoLayoutRect.getInt("height")
                    );
                    mCameraLayoutList.add(frame);
                    continue;
                }
                if (null == frame) {
                    return;
                }
                mVideoList.add(frame);
                //2.2获取视频内容
                JSONArray mVideoContentArray = mVideoLayout.getJSONArray("video_content_array");
                for (int videoContentArrayIndex = 0; videoContentArrayIndex < mVideoContentArray.length(); videoContentArrayIndex++) {
                    JSONObject mVideoContentJSON = (JSONObject) mVideoContentArray.get(videoContentArrayIndex);
                    String mVideoPath = mVideoContentJSON.getString("path");
                    ((VideoFrame) frame).insertVideoContent(mVideoPath);
                    JSONArray mVideoLinkedImageJSON = (JSONArray) mVideoContentJSON.getJSONArray("linked");
                    for (int mVideoLinkedImageIndex = 0; mVideoLinkedImageIndex < mVideoLinkedImageJSON.length(); mVideoLinkedImageIndex++) {
                        JSONObject mLinkedImage = (JSONObject) mVideoLinkedImageJSON.get(mVideoLinkedImageIndex);
                        String imagePath = mLinkedImage.getString("path");
                        int destImageLayout = mLinkedImage.getInt("dest");
                        ((VideoFrame) frame).insertLinkedContent(imagePath, destImageLayout);
                    }
                }
            }
            SystemConfig.D(TAG, DebugEnabled, "parse json layout completed.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //创建一个相机的显示frame
    class CameraFrame extends AbsoluteLayout {
        private Context context;
        private SurfaceView mCameraSurface;
        private SurfaceHolder mCameraHolder;
        private AbsoluteLayout.LayoutParams mParams;
        private Camera mCamera;
        private Camera.Parameters mCamParameters;
        private List<Camera.Size> mCameraPreviewList;
        private List<Camera.Size> mCameraSupportedVideoList;
        private List<Camera.Size> mCameraSupportedPictureList;

        private CameraDevice mCameraDevice;

        private int getCameraNumber() {
            CameraManager mCameraMgr = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                return mCameraMgr.getCameraIdList().length;
            } catch (CameraAccessException e) {
                e.printStackTrace();
                return 0;
            }
        }

        private void startPreview(){
            final CaptureRequest.Builder mPreviewRequestBuilder;
            try {
                //设置了一个具有输出Surface的CaptureRequest.Builder。
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(mCameraHolder.getSurface());
                //创建一个CameraCaptureSession来进行相机预览。
                    mCameraDevice.createCaptureSession(Arrays.asList(mCameraHolder.getSurface()), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            SystemConfig.D(TAG,DebugEnabled,"preview prepared....1");
                            // 相机已经关闭
                            if (null == mCameraDevice) {
                                return;
                            }
                            // 会话准备好后，我们开始显示预览
                           // mCaptureSession = cameraCaptureSession;
                            SystemConfig.D(TAG,DebugEnabled,"preview prepared....");
                            try {
                                // 自动对焦应
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 闪光灯
                                //setAutoFlash(mPreviewRequestBuilder);
                                // 开启相机预览并添加事件
                                CaptureRequest mPreviewRequest = mPreviewRequestBuilder.build();
                                //发送请求
                                cameraCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, null);
                                SystemConfig.D(TAG,DebugEnabled,"preview ok....");
                               // Log.e(TAG," 开启相机预览并添加事件");
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                                SystemConfig.D(TAG,DebugEnabled,"preview the camera failed....");
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            SystemConfig.D(TAG,DebugEnabled,"preview prepared....2");
                        }
                    },null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
                SystemConfig.D(TAG,DebugEnabled,"preview the camera failed....");
            }
        }
        private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                mCameraDevice = cameraDevice;
                SystemConfig.D(TAG,DebugEnabled,"open the camera ok....");
                startPreview();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        };

        private boolean camera2Config(int id) {
            SystemConfig.D(TAG,DebugEnabled,"camera id:" + id + " config");
            CameraManager mCameraMgr = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                //for (String cameraId : mCameraMgr.getCameraIdList())
                {
                String[] cameraIdArr = mCameraMgr.getCameraIdList();
                if(id >= cameraIdArr.length || cameraIdArr == null){
                    SystemConfig.D(TAG,DebugEnabled,"Not any CameraId");
                    return false;
                }
                String cameraId = cameraIdArr[id];
                SystemConfig.D(TAG,DebugEnabled,"CameraId" + cameraId);
                    //1. 获取相机的相关参数
                    CameraCharacteristics characteristics = mCameraMgr.getCameraCharacteristics(cameraId);
                    //2.获取该相机的face
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    //3.获取该相机的stream 配置
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    //4.获取闪光灯
                    boolean flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    //5.打开相机
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return false;
                    }
                    mCameraMgr.openCamera(cameraId, mStateCallback, null);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return true;
        }
        private boolean cameraConfig(int id){
            //1.判断相机数量是否符合选择id
            int mCameraNum = Camera.getNumberOfCameras();
            SystemConfig.D(TAG,DebugEnabled,"camera num:" + mCameraNum + ",id:" + id);

            if(id < 0 || id > (mCameraNum -1)){
                return false;
            }
            //2.相机获取paramter
            mCamera = Camera.open(id);
            mCamParameters = mCamera.getParameters();
            mCameraPreviewList = mCamParameters.getSupportedPreviewSizes();
            mCameraSupportedVideoList = mCamParameters.getSupportedVideoSizes();
            mCameraSupportedPictureList = mCamParameters.getSupportedPictureSizes();

            mCamParameters.setPreviewSize(mCameraPreviewList.get(0).width,mCameraPreviewList.get(0).height);
            mCamera.setParameters(mCamParameters);
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setDisplayOrientation(0);
            mCameraHolder.addCallback(mCameraCallback);
            return true;
        };
        private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback(){

            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {

            }
        };
        private SurfaceHolder.Callback mCameraCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //4.在SurfaceView中显示
                try {
                    mCamera.setPreviewDisplay(holder);
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
        public CameraFrame(Context context,int id,int x,int y,int width,int height){
            super(context);
            this.context = context;
            setId(id);
            //1.设置布局
            mParams = new LayoutParams(width,height,x,y);
            setLayoutParams(mParams);
            setBackgroundColor(0xffff0000);
            //2.设置相机的surface
            mCameraSurface = new SurfaceView(context);
            mCameraSurface.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            mCameraHolder = mCameraSurface.getHolder();
            mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            addView(mCameraSurface);
            //SystemConfig.D(TAG,DebugEnabled,"----------------------------:" + id);
            cameraConfig(id);
        }
        public void onDestroy(){
            if(mCamera != null){
                mCamera.release();
                mCamera = null;
            }
        }
    }
    //创建一个播放用的frame。
    class VideoFrame extends AbsoluteLayout {
        //1.定义视频资源的列表
        private LinkedList<VideoContent> mVideoContentList;
        private VideoContent mVideoContent;
        private AbsoluteLayout.LayoutParams  mParams;
        private SurfaceView mSurface;
        private SurfaceHolder mHolder;
        private MediaPlayer mPlayer;
        private int mCurPostion = 0;
        private int mDuration = 0;
        private Context context;
        public VideoFrame(Context context,int id,int x,int y,int width,int height){
            super(context);
            this.context = context;
            setId(id);
            mVideoContentList = new LinkedList<>();
            //1.设置布局
            mParams = new LayoutParams(width,height,x,y);
            setLayoutParams(mParams);
            setBackgroundColor(0xff0000ff);
            //2.设置播放的surface
            mSurface = new SurfaceView(context);
            mSurface.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            addView(mSurface);
            mHolder = mSurface.getHolder();
            //3.创建媒体播放器
            mPlayer = new MediaPlayer();
          //  mPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
            mPlayer.setOnPreparedListener(mOnPreparedListener);
            mPlayer.setOnCompletionListener(mOnCompletionListener);
            mPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mPlayer.setOnErrorListener(mErrorListener);
            SystemConfig.D(TAG,DebugEnabled,"create a video class..");
            mHolder.addCallback(mPlayerCallback);

        }
        private MediaPlayer.OnErrorListener  mErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        };
        private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

            }
        };
        private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener(){

            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoContent.stopLinkedImagePlay();
                if(mp != null){
                    mp.reset();
                }
                mVideoContent = mVideoContentList.pop();
                if(mVideoContent != null && mVideoContent.getVideoPath() != null){
                    File mFile = new File(mVideoContent.getVideoPath());
                    if(!mFile.exists() || !mFile.isFile()){
                        return;
                    }
                    mVideoContent.startLinkedImagePlay();
                }else{
                    return;
                }
                SystemConfig.D(TAG,DebugEnabled,mVideoContent.getVideoPath());
                try {
                    mPlayer.setDataSource(mVideoContent.getVideoPath());
                    mPlayer.prepare();
                   // mainPlayerCallback.mainPlayerCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    mVideoContentList.addLast(mVideoContent);
                }

            }
        };
        private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayer.start();
            }
        };

        private SurfaceHolder.Callback mPlayerCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                SystemConfig.D(TAG,DebugEnabled,"surface create playback...");
                mVideoContent = mVideoContentList.pop();
                if(mVideoContent != null && mVideoContent.getVideoPath() != null){
                    File mFile = new File(mVideoContent.getVideoPath());
                    if(!mFile.exists() || !mFile.isFile()){
                        return;
                    }
                    mVideoContent.startLinkedImagePlay();
                }else{
                    return;
                }
                mPlayer.setDisplay(mHolder);
                try {
                    mPlayer.setDataSource(mVideoContent.getVideoPath());
                    mPlayer.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    mVideoContentList.addLast(mVideoContent);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        };

        public void insertVideoContent(String path){
            mVideoContent = new VideoContent(context,path);
            mVideoContentList.addLast(mVideoContent);
        }
        public void insertLinkedContent(String imagePath,int destImageLayoutId){
            mVideoContent.insertLinkedContent(imagePath,destImageLayoutId);
        }

        public void onDestroy(){
            mVideoContent.onDestroy();
            if(mPlayer != null) {
                mPlayer.reset();
                mPlayer.release();
                mPlayer = null;
            }
        }

        /**
         * 代表一个视频内容的节点
         * 1.该视频的路径
         * 2.跟该视频关联的图片
         */
        class VideoContent implements Runnable{
            private Context context;
            private String mVideoPath;
            private LinkedList<LinkedContent>mLinkedImage;
            private Handler  mHandler;
            private boolean isPlaying = false;
            //1.从json文件创建视频
            public VideoContent(Context context,String path){
                this.context = context;
                mVideoPath = path;
                mLinkedImage = new LinkedList<LinkedContent>();
                mHandler = new Handler(CustomMultiLayout.this.getContext().getMainLooper());
            }
            //2.从json文件中创建链接的关联图片
            public void insertLinkedContent(String imagePath,int destImageLayoutId){
                LinkedContent  mLinkedContent = new LinkedContent(imagePath,destImageLayoutId);
                mLinkedImage.add(mLinkedContent);
            }
            //3.视频播放时，获取视频的文件。当视频切换的时候，关联的图片也要马上更改
            public String getVideoPath(){
                return mVideoPath;
            }
            public void startLinkedImagePlay(){
                isPlaying = true;
                mHandler.post(this);
            }
            public void stopLinkedImagePlay(){
                isPlaying = false;
                mHandler.removeCallbacks(this);
            }
            @Override
            public void run() {
                if(isPlaying){
                    if(!mLinkedImage.isEmpty()){
                            LinkedContent mLinkedContent = mLinkedImage.pop();
                            mLinkedContent.playImage();
                            mLinkedImage.addLast(mLinkedContent);
                    }
                }
                mHandler.postDelayed(this,5 * 1000);
            }

            public void onDestroy(){
                mHandler.removeCallbacks(this);
                if(mLinkedImage != null){
                    mLinkedImage.clear();
                    mLinkedImage = null;
                }
            }
            class LinkedContent{
                private String path;
                private ImageFrame mImageFrame;
                //1.根据json文件，建立关联图片与播放图片的imageView。
                public LinkedContent(String imagePath,int dest){
                    path = imagePath;
                    for(int index = 0; index < mImageList.size(); index ++){
                        ImageFrame frame = mImageList.get(index);
                        if(frame.getId() ==  dest){
                            mImageFrame = frame;
                        }
                    }
                }
                //2.进入关联播放
                public void playImage(){
                    if(mImageFrame != null && path != null && !path.equals("")){
                        if(mImageFrame.getImageViewAutoPlayState()) {
                            mImageFrame.setAutoPlay(false);//进入关联播放状态
                        }
                        mImageFrame.putImageIntoImageView(path);
                    }
                }
            }
        }
    }
    class ImageFrame extends AbsoluteLayout implements Runnable{
        //设置autoplay ，那么没有关联图片的情况下，就播放内置的图片。一旦关联图片，由视频播放来控制
        private boolean mAutoPlay;
        private AbsoluteLayout.LayoutParams mParams;
        private int id;
        private LinkedList<String>mImageList;
        private ImageView mImageView;
        private Handler mHandler;
        public ImageFrame(Context context,int id,int left,int top,int width,int height) {
            super(context);
            this.id = id;
            setId(id);
            mImageList = new LinkedList<String>();
            mParams = new LayoutParams(width,height,left,top);
            setLayoutParams(mParams);
            mImageView = new ImageView(context);
            mImageView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            addView(mImageView);
            mHandler = new Handler(context.getMainLooper());
            //默认情况下为自动播放
            mAutoPlay = true;
            mHandler.post(this);
        }
        public void insertImage(String path){
            synchronized (mImageList) {
                mImageList.add(path);
            }
        }
        public void eraseImageBuffer(){
            if(!mImageList.isEmpty()){
                mImageList.clear();
            }
        }
        @Override
        public void run() {
            synchronized (mImageList) {
                if (!mImageList.isEmpty()) {
                    String imageName = mImageList.pop();
                    mImageView.setImageURI(Uri.parse(imageName));
                    mImageList.addLast(imageName);
                }
            }
            mHandler.postDelayed(this,5*1000);
        }
        public void onDestroy(){
            mHandler.removeCallbacks(this);
            if(mImageList != null){
                mImageList.clear();
                mImageList = null;
            }
        }
        //设置自动播放和关联播放,当自动播放为播放内部定义的图片，当关联播放，则mAutoPlay = false;

        public void setAutoPlay(boolean mAuto){
           if(mAutoPlay && !mAuto){
                mAutoPlay = false;
                mHandler.removeCallbacks(this);
            }else if(!mAutoPlay && mAuto){
                mAutoPlay = true;
                mHandler.post(this);
            }
        }
        public void putImageIntoImageView(String fileName){
            if(!mAutoPlay) {
                mImageView.setImageURI(Uri.parse(fileName));
            }
        }
        public boolean getImageViewAutoPlayState(){
            return mAutoPlay;
        }
    }
}
