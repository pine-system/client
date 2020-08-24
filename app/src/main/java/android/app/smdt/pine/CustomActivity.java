package android.app.smdt.pine;

import android.Manifest;
import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomDialog;
import android.app.smdt.customui.FloatWindow;
import android.app.smdt.customui.KeyboardView;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.health.TimerStat;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public abstract class CustomActivity extends AppCompatActivity implements View.OnClickListener,SDCard.progressBarCallback{
    protected static final int PASSWORD_SECURET_WINDOW_KEY_BASE = 3000;
    private static final int REQUEST_PERMISSION_CODE = 100;
    protected MyApplication mApp;
    private SDCard mSDCard;
    private boolean isPermission;
    protected abstract void onCreateTask();

    protected CustomDialog mFloatWin;

    private String[] permissions= {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
    };


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mApp = (MyApplication)getApplicationContext();
        onCreateTask();
        //1.设置全屏
        SystemConfig.fullScreen(this);
        //2.请求权限
        isPermission = SystemConfig.RequestPermission(this,permissions,REQUEST_PERMISSION_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isPermission = true;
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }
    public void onStart(){
        super.onStart();
        initTimer();//启动一个定时器
    }

    protected abstract void onResumeTask();

    public void onResume(){
        super.onResume();
        if(isPermission){
            //1.创建对话框，用以显示各种状态
           // mFloatWin = new CustomDialog(this,"test","test");
           // new KeyboardView(this,R.layout.float_window_layout);
            //2.创建sd卡的检测，这个功能在所有的activity中时必须存在的
            mSDCard = SDCard.getInstance(this,this);

            onResumeTask();
        }
    }

    public void onPause(){
        super.onPause();
        cancelTimerTask();
    }

    public void onDestroy(){
        super.onDestroy();
    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
    protected View getActivityRoot(int id){
        return this.getWindow().getDecorView().getRootView();
        //rootView.findViewById(android.R.id.content)
    }
    protected void startCustomApp(String className){
        if(className == null){
            return;
        }
        try {
            Class<?> mStartActivityClass = Class.forName(className);
            Intent mIntent = new Intent(this,mStartActivityClass);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void startCustomService(String className){
        if(className == null){
            return;
        }
        try {
            Class<?> mStartActivityClass = Class.forName(className);
            Intent mIntent = new Intent(this,mStartActivityClass);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(mIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void startCustomService(){

    }
    //设置定时器
    private Timer mTimer;
    private int mDelayTimeCount = 0;
    private int mDelayTimeTotalCount = 0;
    private void initTimer(){
        mTimer = new Timer();
    }
    protected abstract void timeClickCompleted();
    private TimerTask mDelayPlayTask = new TimerTask() {
        @Override
        public void run() {
            if(mDelayTimeCount < mDelayTimeTotalCount){
                mDelayTimeCount ++;
            }else{
                mDelayTimeCount = 0;
                timeClickCompleted();
            }
        }
    };
    protected void startTimerTask(long delay,long period,int count){
        if(mTimer == null){
           mTimer = new Timer();
        }
        mTimer.schedule(mDelayPlayTask,delay,period);
        mDelayTimeTotalCount = count;
    }
    protected void cancelTimerTask(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
    //SDCARD的状态的回显
    @Override
    public void setProgressBarValue(File file, long max, int cur) {
       // mFloatWin.setProgressBar(file,max,cur);
    }

    @Override
    public void updateResourceCompleted() {
       // mFloatWin.updateResourceCompleted();
    }

    @Override
    public void autoPlayStop() {

    }

    //悬浮窗请求
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 23) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {

                } else {

                }
            }
        }
        }
}
