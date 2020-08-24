package android.app.smdt.customui;

import android.app.smdt.config.SystemConfig;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

public class Camera2Frame extends AbsoluteLayout {
    private static final String TAG = "Camera2";
    private static final boolean DebugEnabled = true;
    private AbsoluteLayout.LayoutParams mFrameLayoutParams;
    private TextureView mTextureView;
    private FrameLayout.LayoutParams mParams;
    private CameraManager mCameraMgr;
    private String mCameraIdName;
    private String[] mCameraIdArray;
    private int mCameraId;

    //启动一个线程，侦测当前相机状态
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    public Camera2Frame(Context context, int id,int left,int top,int width,int height){
        super(context);
        //1.设置本布局的尺寸
        SystemConfig.D(TAG,DebugEnabled,"width:" + width +",height:" + height + ",left:" + left + ",top:" + top);
        mFrameLayoutParams = new LayoutParams(width,height,left,height);
        setLayoutParams(mFrameLayoutParams);
        setBackgroundColor(0xffff0000);
        //2。设置TextureView
        mTextureView = new TextureView(context);
        mParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        mTextureView.setLayoutParams(mParams);
       // addView(mTextureView);
        //3。开始相机类
        mCameraId = id;
        mCameraMgr = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        //4.启动线程，开始侦测本ID的相机
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
       // mHandler.post(mCameraHandler);
    }
    private Runnable mCameraHandler = new Runnable() {
        @Override
        public void run() {
            try {
                mCameraIdArray = mCameraMgr.getCameraIdList();
                if(mCameraIdName == null) {
                    mCameraIdName = mCameraIdArray[mCameraId];

                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            //SystemConfig.D(TAG,DebugEnabled,"Camera num:" + mCameraIdArray.length);
            mHandler.postDelayed(this,1000);//每一秒侦测一下。
        }
    };
}
