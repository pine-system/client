package android.app.smdt.pine.resource;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.text.Title;
import android.app.smdt.httputil.HttpUtil;
import android.app.smdt.pine.MyApplication;
import android.app.smdt.pine.Server;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Network implements Runnable{
    //1.常量定义
    private static final String TAG = Network.class.getSimpleName();
    private static final boolean DebugEnabled = true;
    private static final int NETWORK_SCAN_TIMEOUT = 5*1000;
    //2.定义服务器的常量信息
    private static final String REMOTE_HTTP_SERVER_ADDR = "http://192.168.0.122";
    private static final String REMOTE_HTTP_SERVER_PORT = "8080";
    private static final String REMOTE_PROJECT_NAME = "Pine";
    private static final String LOGIN_SERVLET_NAME = "Login";
    private static final String HEARTBEAT_SERVLET_NAME = "HeartBeat";
    private static final String REMOTE_HTTP_SERVER_LOGIN_PAGE = REMOTE_HTTP_SERVER_ADDR + ":" + REMOTE_HTTP_SERVER_PORT + File.separator
            + REMOTE_PROJECT_NAME + File.separator + LOGIN_SERVLET_NAME;
    private static final String REMOTE_HTTP_SERVER_HEARTBEAT_PAGE = REMOTE_HTTP_SERVER_ADDR + ":" + REMOTE_HTTP_SERVER_PORT + File.separator
            + REMOTE_PROJECT_NAME + File.separator + HEARTBEAT_SERVLET_NAME;


    //2.变量定义
    private static Network network = null;
    private HttpUtil httpUtil;
    private ConnectivityManager mCM;
    private NetworkInfo mActiveNetworkInfo;
    private int mActiveNetworkType;
    private String mActiveNetworkName;
    private boolean isConnected;


    //定义一个状态机，不断的往复操作
    private static final int MACHINE_STATE_IDLE = 0;//未知网络状态
    private static final int MACHINE_STATE_INIT = 1;//网络已连接
    private static final int MACHINE_STATE_LOGIN = 2;//网络注册
    private static final int MACHINE_STATE_HEAR_BEAT = 3;

    private int machineState = MACHINE_STATE_IDLE;
    //3.线程实现
    private Handler mHandler;
    private Context context;
    private Object key;//实现同步外网连接
    private Title.CustomText log;
    private String mConnectLog;
    private networkStateCallback callback;
    private HandlerThread mHandlerThread;
    //4.全局变量
    private MyApplication mApp;
    public interface networkStateCallback{
        public void loginCallback(boolean login);
    }
    private Network(Context context){
        //1.基本变量配置
        this.context = context;
        mApp = (MyApplication)context.getApplicationContext();
        key = new Object();
        //2.判断当前网络是否连接。
        isConnected = mApp.isConnected();
        if(isConnected) {
            mConnectLog = "网络连接正常" + ",当前网络连接为:" + mApp.networkType();
        }else{
            mConnectLog = "网络未连接,请查看网络";
        }

        httpUtil = new HttpUtil();
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(this);
    }
    public static Network getInstance(Context context){
        if(network == null){
            network = new Network(context);
        }
        return network;
    }

    @Override
    public void run() {
        //一直判断本机网络连接状态
        isConnected = SystemConfig.isNetworkConnected(context);
        if(isConnected) {
            switch (machineState) {
                case MACHINE_STATE_IDLE:
                    machineState = MACHINE_STATE_INIT;
                    break;
                case MACHINE_STATE_INIT:
                        String ip = SystemConfig.getActiveNetworkIP();

                        String mac = SystemConfig.getActiveNetworkMACFromIP();

                        machineState = MACHINE_STATE_LOGIN;
                        break;
                case MACHINE_STATE_LOGIN:
                    if(httpLogin()){
                        machineState = MACHINE_STATE_HEAR_BEAT;
                    }
                    callback.loginCallback(isLogin);
                    break;
                case MACHINE_STATE_HEAR_BEAT:
                    httpHeartBeat();
                    break;
            }
        }else{
            machineState = MACHINE_STATE_IDLE;
        }
        mHandler.postDelayed(this,NETWORK_SCAN_TIMEOUT);
    }

    private boolean isLogin = false;
    private okhttp3.Callback mLoginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            SystemConfig.D(TAG,DebugEnabled,"network is not connected.");
            isLogin = false;
            synchronized (key) {
                key.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            int respStatus  = response.code();
            BufferedReader mReader;
            String line = null;
            mReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            StringBuilder sb = new StringBuilder();
            while((line = mReader.readLine()) != null){
                sb.append(line);
            }
            mReader.close();
            if(sb.toString() != null && sb.toString().trim().equals("")){
                try {
                    JSONObject o = new JSONObject(sb.toString());
                    if(o.get("result").equals("ok")){
                        isLogin = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                isLogin = false;
            }else{
                isLogin = false;
            }
            //通知同步
            synchronized (key) {
                key.notify();
            }
        }
    };
    private boolean httpLogin(){
        JSONObject jsonLogin = null;// = version.makeCommandRequest(Version.COMMAND_LOGIN);
        httpUtil.okHttpPostByJson(REMOTE_HTTP_SERVER_LOGIN_PAGE,jsonLogin,mLoginCallback);
        synchronized (key){
            try {
                key.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return isLogin;
    }
    private boolean isHeartBeat;
    private Callback mHearBeatCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            SystemConfig.D(TAG,DebugEnabled,"network is not connected.");
            synchronized (key){
                key.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String line = null;
            StringBuffer sb = new StringBuffer();
            BufferedReader mRead = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            while((line = mRead.readLine()) != null){
                sb.append(line);
            }
            if(sb.toString() != null && sb.toString().trim().equals("")){
                try {
                    JSONObject o = new JSONObject(sb.toString());
                    if(o.getString("command").equals("reboot")){
                        Server.reboot(context);
                    }else if(o.getString("command").equals("powerOnOff")){
                        Server.setPowerOnOff(context,o.getString("poweronoff"));
                    }else if(o.getString("command").equals("updateTime")){
                        Server.updateCalendar(context,o.getString("settime"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            synchronized (key){
                key.notify();
            }
        }
};
    private void httpHeartBeat(){
        JSONObject jsonHeartBeat = null;// = version.makeCommandRequest(Version.COMMAND_HEART_BEAT);
        httpUtil.okHttpPostByJson(REMOTE_HTTP_SERVER_LOGIN_PAGE,jsonHeartBeat,mHearBeatCallback);
        synchronized (key) {
            try {
                key.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean isNetworkConnected(){
        return isConnected;
    }
}
