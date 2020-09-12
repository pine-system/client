package android.app.smdt.pine;

import android.app.smdt.config.SystemConfig;

import android.app.smdt.customui.CustomDialog;
import android.app.smdt.customui.KeyboardView;
import android.app.smdt.pine.resource.Network;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
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
public class GuiderActivity extends AppCompatActivity implements Network.networkStateCallback, View.OnClickListener,KeyboardView.CustomKeyboardCallback {
    //1.定义通用的常量
    private static final String TAG = "guider";
    private static final boolean DebugEnabled = true;

    //2.定义欢迎页、提示页
    private ImageView mWelcomeTV;
    private ImageView mOpenPasswdWin;
    private LinearLayout mDeviceIntro;
    private TextView mDeviceInfo;

    //3.获取本机设备的信息,作为全局变量
    private Network httpNetwork;
    private Timer mTimer;
    private static final int AUTO_PLAY_TIMES = 5;
    private int mCount = 0;
    private boolean isAuthorized = false;
    private Context context;
    private KeyboardView mKeyboard;
    private Handler mHandler;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        SystemConfig.fullScreen(this);
        //1.准备通用的实例
        httpNetwork = Network.getInstance(context);
        httpNetwork.setOnNetworkStateListener(this);
        //2.获取各个页面
        mWelcomeTV = (ImageView) findViewById(R.id.welcome_logo);
        mOpenPasswdWin = (ImageView)findViewById(R.id.open_passwd_win);
        mOpenPasswdWin.setOnClickListener(this);
        mDeviceIntro = (LinearLayout)findViewById(R.id.device_intro);
        mDeviceInfo = (TextView)mDeviceIntro.findViewById(R.id.device_info);
        //4.配置一个定时器
        mHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                //1.隐藏欢迎页
                if(mWelcomeTV != null && mWelcomeTV.getVisibility() == View.VISIBLE){
                    mWelcomeTV.setVisibility(View.GONE);
                }
                mDeviceInfo.setText(httpNetwork.getClientInfo());
                //2.首次进入系统，欢迎页后，显示帮助信息
                mOpenPasswdWin.setVisibility(View.VISIBLE);
                mDeviceIntro.setVisibility(View.VISIBLE);
            }
        };
        mTimer = new Timer();
        mTimer.schedule(AutoPlay,0,1000);
        SystemConfig.D(TAG,DebugEnabled,"onCreate");
    }

    @Override
    public void onBackPressed() {
        CustomDialog exitDialog = new CustomDialog(this,"是否退出系统","请确认一下");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //1.关闭timer
        mTimer.cancel();
        mTimer = null;
        //2。关闭network的线程
        httpNetwork.onDestroy();
        SystemConfig.D(TAG,DebugEnabled,"onDestroy");
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
    private static final int AUTO_PLAY_CLIENT_INFO = 1;
    private int autoPlayState = AUTO_PLAY_INIT;
    private TimerTask AutoPlay = new TimerTask() {
        @Override
        public void run() {
            switch(autoPlayState){
                case AUTO_PLAY_INIT:
                    // 让logo显示
                    if(mCount <= AUTO_PLAY_TIMES){
                        mCount ++;
                    }else{
                        mCount = 0;
                        mHandler.sendEmptyMessage(0);
                        autoPlayState = AUTO_PLAY_CLIENT_INFO;
                    }
                    break;
                case AUTO_PLAY_CLIENT_INFO:
                    if(mCount <= AUTO_PLAY_TIMES){
                        mCount ++;
                    }else{
                        if(isAuthorized){
                            customStartActivity("android.app.smdt.pine.MainActivity");
                        }else{
                            mCount = 0;
                            autoPlayState = AUTO_PLAY_INIT;
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.open_passwd_win:
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

    @Override
    public void networkState(int state) {

    }

    @Override
    public void loginCallback(boolean login) {

    }

    //侦听network
    @Override
    public void clientAuthorizedByServer(boolean isAuthorized){
        this.isAuthorized = isAuthorized;
    }
}
