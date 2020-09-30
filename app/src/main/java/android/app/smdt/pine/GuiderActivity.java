package android.app.smdt.pine;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.subui.KeyboardView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Guider activity:系统启动的第一阶段
 * 任务：
 *  1. 显示一张用户的图片，默认状态显示本机内部的Nvtek.png图片。考虑到应用的横、竖屏的关系。
 */
public class GuiderActivity extends AppCompatActivity implements View.OnClickListener,KeyboardView.CustomKeyboardCallback {
    //1.定义通用的常量
    private static final String TAG = "guider";
    private static final boolean DebugEnabled = true;
    private static final int WELCOME_PAGE_HIDE = 1;
    private static final int DEVICE_INTRO_SHOW = 2;
    private static final boolean AUTO_PLAY = true;
    private static final int AUTO_PLAY_TIMES = 5;
    //2.定义欢迎页、提示页
    private ImageView mWelcomeTV;
    private LinearLayout mDeviceIntro;
    private TextView mDeviceInfo;
    private ImageView mNaviPage;
    //3.获取本机设备的信息,作为全局变量
    private Timer mTimer;

    private int mCount = 0;
    private Context context;
    private KeyboardView mKeyboard;
    private Handler mHandler;
    private MyApplication myApp;
    private ResourceFileManager mRFM;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        SystemConfig.fullScreen(this);
        //1.准备通用的实例
        myApp = (MyApplication)getApplication();
        mRFM = myApp.getResourceFileManager();
        mRFM.makeResourceFolder();
        //2.获取各个页面
        mNaviPage = (ImageView)findViewById(R.id.navi_page);
        mNaviPage.setOnClickListener(this);
        mWelcomeTV = (ImageView) findViewById(R.id.welcome_logo);
        mDeviceIntro = (LinearLayout)findViewById(R.id.device_intro);
        mDeviceInfo = (TextView)mDeviceIntro.findViewById(R.id.device_info);
        //4.配置一个定时器
        mHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch(msg.what) {
                    case WELCOME_PAGE_HIDE:
                        if (null != mWelcomeTV && mWelcomeTV.getVisibility() == View.VISIBLE) {
                            mWelcomeTV.setVisibility(View.GONE);
                        }
                        mDeviceInfo.setText(myApp.getDevice().toString());
                        mDeviceIntro.setVisibility(View.VISIBLE);
                        mNaviPage.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
        mTimer = new Timer();
        mTimer.schedule(AutoPlay,0,1000);
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
    protected void onDestroy() {
        super.onDestroy();
        //1.关闭timer
        if(null != mTimer){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void customStartActivity(String app){
        if(app == null || app.trim().equals("")){
            return;
        }
        try {
            Class<?> clazz = Class.forName(app);
            Intent mIntent = new Intent(this,clazz);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mTimer.cancel();
            mTimer = null;
            startActivity(mIntent);
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final int AUTO_PLAY_INIT = 0;
    private static final int AUTO_PLAY_LOGIN = 1;
    private static final int AUTO_PLAY_RESOURCE_PREPARED = 2;
    private static final int AUTO_PLAY_RESOURCE_DUP = 3;
    private static final int AUTO_PLAY_RESOURCE_DUP_COMPLETED = 4;
    private static final int AUTO_PLAY_CLIENT_INFO = 5;
    private static final int AUTO_PLAY_START_MAIN_APP = 6;
    private static final int AUTO_PLAY_START_HEARTBEAT = 7;
    private static final int AUTO_PLAY_STOP = 8;
    private int autoPlayState = AUTO_PLAY_INIT;
    private TimerTask AutoPlay = new TimerTask() {
        @Override
        public void run() {
            Log.e("auto","auto play:" + autoPlayState);
            switch(autoPlayState){
                case AUTO_PLAY_INIT:
                    mRFM.startLogin();
                    autoPlayState = AUTO_PLAY_LOGIN;
                    break;
                case AUTO_PLAY_LOGIN: //login
                    mCount ++;
                    if(mRFM.getDeviceAuthorized() || (mCount > AUTO_PLAY_TIMES)){
                        mHandler.sendEmptyMessage(WELCOME_PAGE_HIDE);
                        autoPlayState = AUTO_PLAY_RESOURCE_PREPARED;
                        mCount = 0;
                    }
                    break;
                case AUTO_PLAY_RESOURCE_PREPARED:
                    if(!mRFM.getDeviceAuthorized()){
                        autoPlayState = AUTO_PLAY_CLIENT_INFO;
                    }else{
                        autoPlayState = AUTO_PLAY_RESOURCE_DUP;
                    }
                    break;
                case  AUTO_PLAY_RESOURCE_DUP:
                    mRFM.startResourceDup();
                    autoPlayState = AUTO_PLAY_RESOURCE_DUP_COMPLETED;
                    break;
                case  AUTO_PLAY_RESOURCE_DUP_COMPLETED:
                    if(mRFM.resourceDUPCompleted()){
                        autoPlayState = AUTO_PLAY_CLIENT_INFO;
                    }
                    break;
                case AUTO_PLAY_CLIENT_INFO:
                    if(mCount <= AUTO_PLAY_TIMES){
                        mCount ++;
                    }else {
                        mCount = 0;
                        autoPlayState = AUTO_PLAY_START_MAIN_APP;
                    }
                    break;
                case AUTO_PLAY_START_MAIN_APP:
                    if(AUTO_PLAY/* && mRFM.getDeviceAuthorized()*/){
                        customStartActivity("android.app.smdt.pine.MainActivity");
                    }else{
                        customStartActivity("android.app.smdt.pine.TestActivity");
                    }
                    autoPlayState = AUTO_PLAY_START_HEARTBEAT;
                    break;
                case AUTO_PLAY_START_HEARTBEAT:
                    mRFM.startHeartbeat();
                    autoPlayState = AUTO_PLAY_STOP;
                    break;
                case AUTO_PLAY_STOP:
                    mCount = 0;
                    mTimer.cancel();
                    mTimer = null;
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.navi_page:
                if(mTimer != null){
                    mTimer.cancel();
                }
                mKeyboard = new KeyboardView(this,R.layout.keyboard);
                mKeyboard.setOnKeyboardListener(this);
                break;
        }
    }
    @Override
    public void startCustomApp(int command) {
        switch(command){
                case KeyboardView.COMMAND_START_MAIN_ACTIVITY:
                    customStartActivity("android.app.smdt.pine.MainActivity");
                    break;
                case KeyboardView.COMMAND_START_SETUP_ACTIVITY:
                    customStartActivity("android.app.smdt.pine.SettingsActivity");
                    break;
        }
    }

}
