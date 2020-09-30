package android.app.smdt.customui.subui.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

public class MyCamera extends SurfaceView {
    private static final String TAG = MyCamera.class.getName();
    private static final boolean Debug = true;

    private int mCameraNumbers;
    private Camera mCamera;
    private SurfaceHolder mCameraHolder;
    private Camera.Parameters mCamParameters;
    private List<Camera.Size> mCameraPreviewList;
    private List<Camera.Size> mCameraSupportedVideoList;
    private List<Camera.Size> mCameraSupportedPictureList;
    public MyCamera(Context context,int id) {
        super(context);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mCameraHolder = getHolder();
        mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCameraNumbers = android.hardware.Camera.getNumberOfCameras();
        if(id < 0 || id > (mCameraNumbers -1)){
            return;
        }
        mCamera = android.hardware.Camera.open(id);
        mCamParameters = mCamera.getParameters();
        mCameraPreviewList = mCamParameters.getSupportedPreviewSizes();
        mCameraSupportedVideoList = mCamParameters.getSupportedVideoSizes();
        mCameraSupportedPictureList = mCamParameters.getSupportedPictureSizes();

        mCamParameters.setPreviewSize(mCameraPreviewList.get(0).width,mCameraPreviewList.get(0).height);
        mCamera.setParameters(mCamParameters);
        mCamera.setPreviewCallback(mPreviewCallback);
        mCamera.setDisplayOrientation(0);
        mCameraHolder.addCallback(mCameraCallback);
    }
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
}
