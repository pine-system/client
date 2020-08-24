package android.app.smdt.pine;

import android.app.smdt.config.DeviceInfo;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;

public class WelComeActivity extends CustomActivity implements DeviceInfo.setOnLoginCallback,SDCard.progressBarCallback{
    //1.定义通用常量
    private static final String TAG = WelComeActivity.class.getSimpleName();
    private static final Boolean DebugEnabled = true;
    //2.定义子View的变量
    private TextView mWelcomeTV;//欢迎界面中的ZoomLine
    private ImageView mOpenPasswdWin;//欢迎界面中左上图标，可以打开密码窗口
    private LinearLayout mDeviceIntro;//欢迎页中设备信息的父亲。
    private TextView mDeviceInfoTv;//欢迎界面中间的本机设备的基本信息
    private GridLayout mPasswdWin;//欢迎界面下面的键盘数据
    private EditText mPasswdEt;//装载输入密码
    private String mPasswordValue = "";//密码
    @Override
    protected void onCreateTask() {
        //1.设置root view
        setContentView(R.layout.activity_main);
        //2.获取各个页面
        mWelcomeTV = (TextView)findViewById(R.id.welcome_logo);
        mOpenPasswdWin = (ImageView)findViewById(R.id.open_passwd_win);
        mOpenPasswdWin.setOnClickListener(this);
        mDeviceIntro = (LinearLayout)findViewById(R.id.device_intro);
        mDeviceInfoTv = (TextView)findViewById(R.id.device_info);
        mDeviceInfoTv.setText(mApp.getDeviceInfo().toString());
        mPasswdWin = (GridLayout) findViewById(R.id.passwd_win);
        setSecureWinListener();
        //3.设置login回调,
        mApp.getDeviceInfo().setOnLoignCallbackListener(this);
    }

    @Override
    protected void onResumeTask() {

    }


    //发送一个定时任务后，时间到后的回调
    @Override
    protected void timeClickCompleted() {
        //1.定时时间到了,启动主activity
        startCustomApp(getPackageName() + "." + "MainActivity");
        //2.取消定时器
        cancelTimerTask();
    }

    private void setSecureWinListener(){
        mPasswdEt = (EditText)mPasswdWin.findViewById(R.id.passwd_value);
        mPasswdEt.setText(mPasswordValue);
        for(int index = 0; index < mPasswdWin.getChildCount(); index ++){
            if(mPasswdWin.getChildAt(index) instanceof Button){
                Button btn = (Button)mPasswdWin.getChildAt(index);
                btn.setId(PASSWORD_SECURET_WINDOW_KEY_BASE + index);
                btn.setOnClickListener(this);
            }
        }
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.open_passwd_win:
                mPasswdEt.setText("");
                if(mPasswdWin.getVisibility() == View.INVISIBLE) {
                    mPasswdWin.setVisibility(View.VISIBLE);
                    cancelTimerTask();
                }else{
                    mPasswdWin.setVisibility(View.INVISIBLE);
                    startTimerTask(0,1000,5);//再次启动
                }
                return;
        }
        int keyVal;
        mPasswordValue = mPasswdEt.getText().toString();
        if(id >= PASSWORD_SECURET_WINDOW_KEY_BASE && id <= (PASSWORD_SECURET_WINDOW_KEY_BASE  + 3)){
            keyVal = id - PASSWORD_SECURET_WINDOW_KEY_BASE;
            mPasswordValue += keyVal;
        }else if(id >= (PASSWORD_SECURET_WINDOW_KEY_BASE + 5) && id <= (PASSWORD_SECURET_WINDOW_KEY_BASE  + 7)){
            keyVal = id - PASSWORD_SECURET_WINDOW_KEY_BASE -1;
            mPasswordValue += keyVal;
        }else if(id >= (PASSWORD_SECURET_WINDOW_KEY_BASE + 9) && id <= (PASSWORD_SECURET_WINDOW_KEY_BASE  + 11)){
            keyVal = id - PASSWORD_SECURET_WINDOW_KEY_BASE -2;
            mPasswordValue += keyVal;
        }else if(id == (PASSWORD_SECURET_WINDOW_KEY_BASE + 12)){
            keyVal = 0;
            mPasswordValue += keyVal;
        }else if(id == (PASSWORD_SECURET_WINDOW_KEY_BASE + 4)){
            int len = mPasswordValue.length()-1;
            if(len < 0){
                len = 0;
            }
            mPasswordValue = mPasswordValue.substring(0,len);
        }else if(id == (PASSWORD_SECURET_WINDOW_KEY_BASE + 8)){
            if(mPasswordValue.equals("1111")){
                startCustomApp(getPackageName() + "." + "MainActivity");
                return;
            }
            if(mPasswordValue.equals("0000")){

                return;
            }
        }
        mPasswdEt.setText(mPasswordValue);
    }

    @Override
    public void loginCompleted(int delay) {
        //1.隐藏欢迎页
        if(mWelcomeTV != null && mWelcomeTV.getVisibility() == View.VISIBLE){
            mWelcomeTV.setVisibility(View.GONE);
        }
        mDeviceInfoTv.setText(mApp.getDeviceInfo().toString());
        //2.首次进入系统，欢迎页后，显示帮助信息
        mOpenPasswdWin.setVisibility(View.VISIBLE);
        mDeviceIntro.setVisibility(View.VISIBLE);
        //3.为实现信息展示，启动一个定时器，5秒后就进入播放的主页面
        startTimerTask(0,1000,5);// 5 * 1000;
    }

    @Override
    public void setProgressBarValue(File file, long max, int cur) {

    }

    @Override
    public void updateResourceCompleted() {

    }

    @Override
    public void autoPlayStop() {

    }
}
