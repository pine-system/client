package android.app.smdt.pine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.BaseLayout;
import android.app.smdt.customui.SubLayout;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    //1.常量
    private static final String TAG  = "main";
    private static final boolean DebugEnabled = true;
    private static final int REQUEST_PERMISSION_CODE =  1000;
    private static final int DISPLAY_DEVICE_MAX_NUM = 2;
    private static final boolean VICE_SCREEN_DISPLAY_ENABLE = true;
    //2.变量定义getInstance
    private BaseLayout mBaseLayout;
    private boolean isPermission;
    private SubLayout mainLayout;
    private SubLayout viceLayout;
    private ViceScreenDisplay mViceScreenDisplay;
    private Context context;
    private String[] permissions= {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.SYSTEM_ALERT_WINDOW
    };
    private MyApplication mApp;
    private ResourceFileManager mRFM;

    private boolean viceScreenDisplayEnabled(){
        DisplayManager mDM = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
        Display[] mDisplays = mDM.getDisplays();
        if(mDisplays.length ==  DISPLAY_DEVICE_MAX_NUM && VICE_SCREEN_DISPLAY_ENABLE){
            return true;
        }else{
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1.创建自定义的桌面
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         mBaseLayout = new BaseLayout(this);
         setContentView(mainLayout);
        //2.将该桌面设置为全屏
         SystemConfig.fullScreen(this);
        //3.将该桌面请求为竖屏
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        //4.获取系统权限
        isPermission = SystemConfig.RequestPermission(this,permissions,REQUEST_PERMISSION_CODE);
        //5.处理主线程中的UI
        mApp = (MyApplication)getApplication();
        mRFM = mApp.getResourceFileManager();
        this.context = this;
    }
    private void startPlay(){
        JSONObject mainScreenLayout =  mRFM.getScreenLayout(ResourceFileManager.MAIN_SCREEN_LAYOUT_INDEX);
        if(null != mainScreenLayout){
            if(null == mainLayout){
                mainLayout = new SubLayout(this, mainScreenLayout,0,getDrawable(R.drawable.no_resource));
                mBaseLayout.InsertSubViewIntoCustomLayout(mainLayout);
            }
        }
    }

    private void startViceScreenPlay(){
        JSONObject viceScreenLayout =  mRFM.getScreenLayout(ResourceFileManager.VICE_SCREEN_LAYOUT_INDEX);
        if(null != viceScreenLayout){
            if(null == viceLayout){
                viceLayout = new SubLayout(this,viceScreenLayout,1,getDrawable(R.drawable.no_resource));
                mViceScreenDisplay = new ViceScreenDisplay(this,ViceScreenDisplay.VICE_SCREEN_INDEX,viceLayout);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(isPermission){
            //1.根据ResourceFileManager 管理layout
            startPlay();
            //2.启动双屏显示
            if(viceScreenDisplayEnabled()){
                startViceScreenPlay();
            }
        }
    }
    @Override
    public void onBackPressed() {
        String title = getPackageName() + " " + getResources().getString(R.string.confirm);
        AlertDialog.Builder  mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(title);
        mBuilder.setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ((Activity)context).finish();
            }
        });
        mBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(true);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mBaseLayout.removeAllViews();
        if(mainLayout != null){
            mainLayout.onDestroy();
        }
        if(viceLayout != null){
            viceLayout.onDestroy();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      //  super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isPermission = true;
            }else{
                //未授权
            }
        }
    }
}
