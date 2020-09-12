package android.app.smdt.pine.resource;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.text.Title;
import android.app.smdt.httputil.HttpUtil;
import android.app.smdt.pine.MyApplication;
import android.app.smdt.pine.Server;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Network implements Runnable{
    //1.常量定义
    private static final String TAG = Network.class.getSimpleName();
    private static final boolean DebugEnabled = true;
    private static final int NETWORK_SCAN_TIMEOUT = 5*1000;
    //2.定义服务器的常量信息
    private static final String REMOTE_HTTP_SERVER_ADDR = "http://192.168.0.105";
    private static final String REMOTE_HTTP_SERVER_PORT = "8080";
    private static final String REMOTE_PROJECT_NAME = "pine";
    private static final String LOGIN_SERVLET_NAME = "Login";
    private static final String HEARTBEAT_SERVLET_NAME = "HeartBeat";
    private static final String SCREENSHOT_SERVLET_NAME ="ScreenShot";
    private static final String REMOTE_HTTP_SERVER_LOGIN_PAGE = REMOTE_HTTP_SERVER_ADDR + ":" + REMOTE_HTTP_SERVER_PORT + File.separator
            + REMOTE_PROJECT_NAME + File.separator + LOGIN_SERVLET_NAME;
    private static final String REMOTE_HTTP_SERVER_HEARTBEAT_PAGE = REMOTE_HTTP_SERVER_ADDR + ":" + REMOTE_HTTP_SERVER_PORT + File.separator
            + REMOTE_PROJECT_NAME + File.separator + HEARTBEAT_SERVLET_NAME;
    private static final String REMOTE_HTTP_SERVER_SCREENSHOT_PAGE = REMOTE_HTTP_SERVER_ADDR + ":" + REMOTE_HTTP_SERVER_PORT + File.separator
            + REMOTE_PROJECT_NAME + File.separator + SCREENSHOT_SERVLET_NAME;


    //2.变量定义
    private static Network network = null;
    private HttpUtil httpUtil;
    private ConnectivityManager mCM;
    private NetworkInfo mActiveNetworkInfo;
    private int mActiveNetworkType;
    private String mActiveNetworkName;
    private boolean isConnected;

    //3.本机信息
    private String client;//本机的序列号，这个是唯一的ID
    private String user;//代表本机的用户ID
    private int gid;//代表本机的组ID
    private boolean isAuthorized;
    private String ip;
    private String mac;
    private String os;
    private String soc;
    private String valid;
    private int id;


    //定义一个状态机，不断的往复操作
    private static final int MACHINE_STATE_IDLE = 0;//未知网络状态
    private static final int MACHINE_STATE_INIT = 1;//网络已连接
    private static final int MACHINE_STATE_LOGIN = 2;//网络注册
    private static final int MACHINE_STATE_UPDATE_INFO = 3;
    private static final int MACHINE_STATE_HEAR_BEAT = 4;

    private int machineState = MACHINE_STATE_IDLE;
    //3.线程实现
    private Handler mHandler;
    private Context context;
    private Object key;//实现同步外网连接

    private networkStateCallback callback;
    private HandlerThread mHandlerThread;

    public interface networkStateCallback{
        public void networkState(int state);
        public void loginCallback(boolean login);
        public void clientAuthorizedByServer(boolean isAuthorized);
    }
    private List<networkStateCallback>networkStateList;
    public void setOnNetworkStateListener(networkStateCallback callback){
        networkStateList.add(callback);
    }
    private Network(Context context){
        //1.基本变量配置
        this.context = context;
        key = new Object();
        networkStateList = new ArrayList<>();
        //2.配置本机信息
        client = SystemConfig.getSystemSerial();
        user = SystemConfig.PropertiesGet("ro.user.id","nvtek");
        gid = Integer.parseInt(SystemConfig.PropertiesGet("ro.group.id","1"));
        soc = Build.HARDWARE;
        os = Build.PRODUCT;
        //3.准备启动
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
    public String getClientInfo(){
            StringBuffer sb = new StringBuffer();
            sb.append("user:" + user);
            sb.append("\n");
            sb.append("gid:" + gid);
            sb.append("\n");
            sb.append("client:" + client);
            sb.append("\n");
            sb.append("soc:" + soc);
            sb.append("\n");
            sb.append("os:" + os);
            sb.append("\n");
            return sb.toString();
    }
    private JSONObject makeLoginJSON(){
        JSONObject json = new JSONObject();
        try {
            json.put("user",user);
            json.put("gid",gid);
            json.put("client",client);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
    private JSONObject updateClientJSON(){
        JSONObject json = new JSONObject();
        try {
            json.put("user",user);
            json.put("gid",gid);
            json.put("client",client);
            json.put("mac",mac);
            json.put("ip",ip);
            json.put("soc",soc);
            json.put("os",os);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
    JSONObject mCommandJSON;
    @Override
    public void run() {
        //一直判断本机网络连接状态
        switch(machineState){
            case MACHINE_STATE_IDLE:
                isConnected = SystemConfig.isNetworkConnected(context);
                if(isConnected){
                    mac = SystemConfig.getActiveNetworkMACFromIP();
                    ip = SystemConfig.getActiveNetworkIP();
                    SystemConfig.D(TAG,DebugEnabled,"net connected. enter machined_state_init");
                    machineState = MACHINE_STATE_INIT;
                }
                break;
            case MACHINE_STATE_INIT:
                mCommandJSON = makeLoginJSON();
                machineState = MACHINE_STATE_LOGIN;
                SystemConfig.D(TAG,DebugEnabled,"enter the login");
                break;
            case MACHINE_STATE_LOGIN:
                httpLogin(mCommandJSON);
                if(isLogin){
                    machineState = MACHINE_STATE_UPDATE_INFO;
                }else{
                    machineState = MACHINE_STATE_IDLE;
                }
                break;
            case MACHINE_STATE_UPDATE_INFO:
                if(isAuthorized){
                    for(int index = 0; index < networkStateList.size(); index ++){
                        networkStateList.get(index).clientAuthorizedByServer(true);
                    }
                    mCommandJSON = updateClientJSON();
                    httpLogin(mCommandJSON);
                    machineState = MACHINE_STATE_HEAR_BEAT;

                }else{
                    machineState = MACHINE_STATE_IDLE;
                }
                break;
            case MACHINE_STATE_HEAR_BEAT:
                isConnected = SystemConfig.isNetworkConnected(context);
                if(isConnected){
                    mCommandJSON = makeLoginJSON();
                    httpHeartBeat(mCommandJSON);
                }else{
                    machineState = MACHINE_STATE_IDLE;
                }
                break;
        }

        mHandler.postDelayed(this,NETWORK_SCAN_TIMEOUT);
    }

    private boolean isLogin = false;
    private okhttp3.Callback mLoginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            SystemConfig.D(TAG,DebugEnabled,"login request failed!");
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
            SystemConfig.D(TAG,DebugEnabled,"reponse:" + sb.toString());
            if(sb.toString() != null && !sb.toString().trim().isEmpty()){
                try {
                    JSONObject o = new JSONObject(sb.toString());
                    valid = o.getString("valid");
                    isAuthorized = o.getBoolean("isAuthorized");
                    isLogin = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    isLogin = false;
                }
            }else{
                isLogin = false;
            }
            //通知同步
            synchronized (key) {
                key.notify();
            }
        }
    };
    private boolean httpLogin(JSONObject mlogin){
        JSONObject jsonLogin = mlogin;
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
            SystemConfig.D(TAG,DebugEnabled,"sb:" + sb.toString());
            if(sb.toString() != null && !sb.toString().trim().equals("")){
                try {
                    JSONObject o = new JSONObject(sb.toString());
                    //SystemConfig.D(TAG,DebugEnabled,"command" + o.getInt("command")+",parameter:" + o.getString("parameter"));
                    switch(o.getInt("command")){
                        case Server.COMMAND_FLAG_REBOOT:
                            Server.reboot(context);
                           // SystemConfig.D(TAG,DebugEnabled,"reboot");
                            break;
                        case Server.COMMAND_FLAG_SET_SYSTEM_TIME:
                            //SystemConfig.D(TAG,DebugEnabled,"set time");
                            Server.updateCalendar(context,o.getString("parameter"));
                            break;
                        case Server.COMMAND_FLAG_SET_POWERONOFF:
                            //SystemConfig.D(TAG,DebugEnabled,"power on off");
                            Server.setPowerOnOff(context,o.getString("parameter"));
                            break;
                        case Server.COMMAND_FLAG_SCREENSHOT:
                            Server.ScreenCap(context);
                            break;
                        case Server.COMMAND_FLAG_SET_VOLUME:
                            Server.SetVolume(context,o.getString("parameter"));
                            break;
                        case Server.COMMAND_FLAG_UPDATE_VOLUME:
                            Server.SetVolume(context,o.getString("parameter"));
                            break;
                        case Server.COMMAND_FLAG_DOWNLOAD_RESOURCE:
                            Server.DownloadResource(context);
                            break;
                        case Server.COMMAND_FLAG_DOWNLOAD_FIRMWARE:
                            Server.DownloadFirmware(context);
                            break;
                        case Server.COMMAND_FLAG_UPGRADE_APK:
                            Server.downloadApk(context);
                            break;
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
    private void httpHeartBeat(JSONObject heartBeatJSON){
        JSONObject jsonHeartBeat = heartBeatJSON;
        httpUtil.okHttpPostByJson(REMOTE_HTTP_SERVER_HEARTBEAT_PAGE,jsonHeartBeat,mHearBeatCallback);
        synchronized (key) {
            try {
                key.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private Callback mScreenShotCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        }
    };
    private void httpUploadImage(String path){
        if(path == null){
            return;
        }
        File mFile = new File(path);
        if(!mFile.exists() || !mFile.canRead()){
            return;
        }
        httpUtil.okHttpPostByFile(REMOTE_HTTP_SERVER_SCREENSHOT_PAGE,mFile,mScreenShotCallback);
    }
    public boolean isNetworkConnected(){
        return isConnected;
    }

    public void onDestroy(){
       // mHandler.removeCallbacks(this);
    }
}
