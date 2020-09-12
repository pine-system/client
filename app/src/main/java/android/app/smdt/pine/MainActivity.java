package android.app.smdt.pine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomLayout;
import android.app.smdt.customui.CustomMultiLayout;
import android.app.smdt.customui.CustomViewGroup;
import android.app.smdt.customui.text.Title;
import android.app.smdt.pine.resource.Network;
import android.app.smdt.pine.resource.UpdateResource;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity implements UpdateResource.UpdateResourceIntoSDCardCallback,SDCard.progressBarCallback{
    private static final int LAYOUT_MODE_INDEX = 0;
    //1.常量
    private static final String TAG  = "main";
    private static final boolean DebugEnabled = true;
    private static final int REQUEST_PERMISSION_CODE =  1000;
    //2.变量定义getInstance
    CustomLayout mainLayout;
    private Title mTitle;
    private Title.CustomText mLogTitle;
    private Title.CustomText mSdTitle;
    private Title.CustomText mMainTitle;//标题显示
    private Title.CustomText mTimeTitle;//时间显示
    private Title.CustomText mSubTitle;//底部的跑马灯显示
    private boolean isPermission;
    private SDCard sdcard;
    private Network network;


    private LinkedList<File>mVideoList;
    private LinkedList<File>mImageList;
    private LinkedList<File>mLogoList;
    private LinkedList<File>mSubtitleList;
    private LinkedList<File>mLayoutList;
    private CustomViewGroup mCustomViewGroup;
    private CustomMultiLayout multiLayout;
    private Handler mHandler;
    private Context context;
    private String[] permissions= {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.SYSTEM_ALERT_WINDOW
    };
    private UpdateResource mUpdateResource;
    private Object mKey;
    //双屏异显
    private ViceScreenDisplay mViceDisplay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1.创建自定义的桌面
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         mainLayout = new CustomLayout(this);
         setContentView(mainLayout);
        //2.将该桌面设置为全屏
         SystemConfig.fullScreen(this);
        //3.将该桌面请求为竖屏
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //4.获取显示title
        mTitle = Title.makeTitleInstance();
        //5.获取系统权限
        isPermission = SystemConfig.RequestPermission(this,permissions,REQUEST_PERMISSION_CODE);
        //6.处理主线程中的UI
        context = this;
        mKey = new Object();
        mHandler = new Handler(getMainLooper()){
          @Override
          public void handleMessage(Message msg){
              switch(msg.what){
                  case 0: //auto play stop
                      SystemConfig.D(TAG,DebugEnabled,"sd card update.");
                      if(multiLayout != null){
                          multiLayout.onDestroy();
                          mainLayout.RemoveSubViewIntoCustomLayout(multiLayout);
                          multiLayout = null;

                      }
                      if(mViceDisplay != null){
                          mViceDisplay.onDestroy();
                          mViceDisplay = null;
                      }
                      break;
                  case 1:// auto play start
                      RefreshResourceList();
                      multiLayout = new CustomMultiLayout(context,mainLayout,mLayoutList.get(LAYOUT_MODE_INDEX),mLogTitle);
                      autoViceScreenPlay();
                      break;
              }
          }
        };
    }
/*
    @Override
    public void onBackPressed() {
        CustomDialog exitDialog = new CustomDialog(this,"是否退出系统","请确认一下");
    }
*/
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SystemConfig.D(TAG,DebugEnabled,"orient:" + newConfig,"rotate:" + this.getWindowManager().getDefaultDisplay().getRotation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isPermission){
            //1.创建UpdateResource的单例。
         //   mUpdateResource = UpdateResource.makeUpdateResource(this,mKey);
         //   mUpdateResource.setOnUpdateResourceLinstener(this);
           // mKey.notify();
            //a)创建sdcard中的资源文件路径
            //b)实现资源拷贝
            //c)获取资源
            //d)退出
            //mLogTitle = mTitle.insertCustomText(this,mainLayout,"",mTitle.MAIN_TITLE);
           // sdcard = SDCard.getInstance(this,this);
            //4.检测网络
           // network = Network.getInstance(this,this,mLogTitle);
            //5.开始播放
           // RefreshResourceList();
            //autoPlay();
            //6.启动双屏
          //  autoViceScreenPlay();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(multiLayout != null) {
            multiLayout.onDestroy();
        }
        if(mViceDisplay != null) {
            mViceDisplay.onDestroy();
        }
        mainLayout.removeAllViews();
        sdcard.onDestroy();
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
    private float count = 0f;
    @Override
    public void setProgressBarValue(File file, long max, int cur) {
        count +=  cur;
        float ratio  =  count / max;
        DecimalFormat format = new DecimalFormat("#.00");
        String scale = format.format(ratio);
        String reslut = Float.parseFloat(scale) * 100 + "%";
        SystemConfig.D("SDCard",DebugEnabled,file.getAbsolutePath() + "ratio:" + reslut);
        mLogTitle.updateTitle(file.getName() + "   ratio:" + reslut );
    }

    @Override
    public void updateResourceCompleted() {
        mLogTitle.updateTitle("拷贝完成");
        mHandler.sendEmptyMessage(1);
    }

    @Override
    public void autoPlayStop() {
       mHandler.sendEmptyMessage(0);//当插入sd u盘，停止自动播放
    }



    private void RefreshResourceList(){
        mVideoList = sdcard.getVideoList();
        mImageList = sdcard.getImageList();
        mLogoList = sdcard.getmLogoList();
        mSubtitleList = sdcard.getmSubtitleList();
        mLayoutList = sdcard.getmLayoutList();
    }

    private void autoPlay(){
        if(multiLayout == null && !mLayoutList.isEmpty()) {
            SystemConfig.D(TAG,DebugEnabled,"start to play");
            Toast.makeText(this,"start to play",Toast.LENGTH_LONG).show();
            multiLayout = new CustomMultiLayout(this, mainLayout, mLayoutList.get(LAYOUT_MODE_INDEX),mLogTitle);
        }
    }
    private void autoViceScreenPlay(){
        if(mViceDisplay == null && !mVideoList.isEmpty()) {
            mViceDisplay = new ViceScreenDisplay(getApplicationContext(), mVideoList,mImageList);
            multiLayout.setPlayerListener(mViceDisplay);
        }
    }


    @Override
    public void updateResourceSDcardRatio(String fileName, String ratio) {

    }

    @Override
    public void updateResourceSDcardCompleted() {

    }

    @Override
    public void stopPlay() {

    }
}
