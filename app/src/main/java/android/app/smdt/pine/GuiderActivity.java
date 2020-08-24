package android.app.smdt.pine;

import android.app.smdt.config.DeviceInfo;
import android.app.smdt.config.SystemConfig;

import android.app.smdt.customui.CustomDialog;
import android.app.smdt.customui.KeyboardView;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.View;

import android.view.Window;
import android.widget.Button;

import android.widget.EditText;

import android.widget.GridLayout;
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
public class GuiderActivity extends AppCompatActivity implements DeviceInfo.setOnLoginCallback, View.OnClickListener,KeyboardView.CustomKeyboardCallback {
    //1.定义通用的常量
    private static final String TAG = GuiderActivity.class.getSimpleName();
    private static final boolean DebugEnabled = true;

    //2.定义欢迎页、提示页
    private ImageView mWelcomeTV;
    private ImageView mOpenPasswdWin;
    private LinearLayout mDeviceIntro;
    private TextView mDeviceInfo;

    //3.获取本机设备的信息,作为全局变量
    private MyApplication mApp;
    private Timer mTimer;
    private static final int AUTO_PLAY_TIMES = 5;
    private int mCount = 0;

    private Context context;
    private KeyboardView mKeyboard;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        SystemConfig.fullScreen(this);
        //1.准备通用的实例
        mApp = (MyApplication)getApplication();
        //2.获取各个页面
        mWelcomeTV = (ImageView) findViewById(R.id.welcome_logo);
        mOpenPasswdWin = (ImageView)findViewById(R.id.open_passwd_win);
        mOpenPasswdWin.setOnClickListener(this);
        mDeviceIntro = (LinearLayout)findViewById(R.id.device_intro);
        mDeviceInfo = (TextView)mDeviceIntro.findViewById(R.id.device_info);
        mDeviceInfo.setText(mApp.getDeviceInfo().toString());
        //3.设置login回调,
        mApp.getDeviceInfo().setOnLoignCallbackListener(this);
        //4.配置一个定时器
        mTimer = new Timer();
        mTimer.schedule(AutoPlay,0,1000);
    }

    @Override
    public void onBackPressed() {
        CustomDialog exitDialog = new CustomDialog(this,"是否退出系统","请确认一下");
    }

    private void customStartActivity(String app){
        if(app == null || app.trim().equals("")){
            return;
        }
        try {
            Class<?> clazz = Class.forName(app);
            Intent mIntent = new Intent(this,clazz);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mIntent);
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private TimerTask AutoPlay = new TimerTask() {
        @Override
        public void run() {
            if(mApp.isAuthorized()){
                if(mCount <= AUTO_PLAY_TIMES){
                    mCount ++;
                }else {
                    mCount = 0;
                    customStartActivity("android.app.smdt.pine.MainActivity");
                    mTimer.cancel();
                    mTimer = null;
                }
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
                    mTimer = null;
                    mCount = 0;
                }
                mKeyboard = new KeyboardView(this,R.layout.keyboard);
                mKeyboard.setOnKeyboardListener(this);
                break;
        }
    }
    @Override
    public void loginCompleted(int delay) {
        //1.隐藏欢迎页
        if(mWelcomeTV != null && mWelcomeTV.getVisibility() == View.VISIBLE){
            mWelcomeTV.setVisibility(View.GONE);
        }
       mDeviceInfo.setText(mApp.getDeviceInfo().toString());
        //2.首次进入系统，欢迎页后，显示帮助信息
        mOpenPasswdWin.setVisibility(View.VISIBLE);
        mDeviceIntro.setVisibility(View.VISIBLE);
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
