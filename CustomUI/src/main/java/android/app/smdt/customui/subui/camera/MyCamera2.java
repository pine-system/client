package android.app.smdt.customui.subui.camera;

import android.Manifest;
import android.app.smdt.config.SystemConfig;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.AbsoluteLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;

public class MyCamera2 extends SurfaceView {
    private static final String TAG = MyCamera2.class.getName();
    private static final boolean DebugEnabled = true;

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
                    // 相机已经关闭
                    if (null == mCameraDevice) {
                        return;
                    }
                    // 会话准备好后，我们开始显示预览
                    // mCaptureSession = cameraCaptureSession;
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

    public MyCamera2(Context context,int id) {
        super(context);
        CameraManager mCameraMgr = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
                {
                    String[] cameraIdArr = mCameraMgr.getCameraIdList();
                    if(id >= cameraIdArr.length || cameraIdArr == null){
                        return;
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
                    return;
                }
                mCameraMgr.openCamera(cameraId, mStateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
