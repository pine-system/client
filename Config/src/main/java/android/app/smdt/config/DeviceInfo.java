package android.app.smdt.config;

import android.app.smdt.httputil.HttpUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class DeviceInfo implements  Runnable{
    private static final String TAG = DeviceInfo.class.getSimpleName();
    private static final Boolean DebugEnabled = true;

    private static final String HTTP_SERVER_PATH = "http://192.168.0.188";
    private static final String HTTP_SERVER_PORT = "8080";
    private static final String HTTP_SERVER_END = "pine";
    private static final String VERSION = "0.0.1";

    private String DUID;//本机的序列号，这个是唯一的ID
    private String UserID;//代表本机的用户ID
    private String GroupID;//代表本机的组ID
    private boolean isAuthorized;
    private String IPAddr;
    private String MAC;
    private String OS;
    private String version;
    private String Start;
    private String End;
    private String NetworkType;
    private boolean networkConnected;
    private static DeviceInfo info = null;

    private HttpUtil mHttpUtil;
    private Object key;
    private String mHttpServer;
    private int httpStatus;

    private SharedPreferences mSPF;
    private SharedPreferences.Editor mEditor;
    private static final String STORAGE_FILE ="device_info";

    private Context context;

    //启动一个定时器，每10秒重复一次
    private Handler mHandler;
    private int mCount = 0;//当开机进行注册的时候，如果发现未授权，进行10次判断
    private static final int LOGIN_MAX_TIMES =  2;

    public interface setOnLoginCallback{
        public void loginCompleted(int delay);
    }
    private LinkedList<setOnLoginCallback>mLoginCallbacks;
    private DeviceInfo(Context context){
        this.context = context;
        mLoginCallbacks = new LinkedList<>();
        DUID = SystemConfig.getSystemSerial();
        UserID = SystemConfig.PropertiesGet("ro.user.id","nvtek");
        GroupID = SystemConfig.PropertiesGet("ro.group.id","g0");
        OS = Build.PRODUCT;
        NetworkType = SystemConfig.getActiveNetworkType(context);
        if(null == NetworkType){
            IPAddr = null;
            MAC = null;
        }else{
            IPAddr = SystemConfig.getActiveNetworkIP();
            MAC = SystemConfig.getActiveNetworkMACFromIP();
            networkConnected = SystemConfig.isNetworkConnected(context);
        }
        version = VERSION;
        mHttpServer = HTTP_SERVER_PATH + ":" + HTTP_SERVER_PORT + File.separator + HTTP_SERVER_END + File.separator;
        httpStatus = 404;

        key = new Object();
        mHttpUtil = new HttpUtil();
        mSPF = context.getSharedPreferences(Context.STORAGE_SERVICE,Context.MODE_PRIVATE);
        mEditor = mSPF.edit();
        Start = mSPF.getString("start","2020-07-31");
        End = mSPF.getString("end","2020-08-01");
       // httpCommand("Login",mLoginCallback);
        mEditor.putBoolean("isAuthorized",isAuthorized);
        mEditor.commit();
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(this);
    }
    public static DeviceInfo getInstance(Context context){
        if(info == null){
            info = new DeviceInfo(context);
        }
        return info;
    }
    public boolean isAuthorized(){
        return isAuthorized;
    }
    public String networkType(){
        return NetworkType;
    }
    public boolean isNetworkConnected(){
        return networkConnected;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DUID:" + DUID);
        sb.append("\n");
        sb.append("UserID:" + UserID);
        sb.append("\n");
        sb.append("GroupID:" + GroupID);
        sb.append("\n");
        sb.append("NetworkType:" + NetworkType);
        sb.append("\n");
        sb.append("networkConnected:" + networkConnected);
        sb.append("\n");
        sb.append("IP:" + IPAddr);
        sb.append("\n");
        sb.append("MAC:" + MAC);
        sb.append("\n");
        sb.append("isAuthorized:" + isAuthorized);
        sb.append("\n");
        return sb.toString();
    }
    private JSONObject makeJSON(String cmd){
        JSONObject json = new JSONObject();
        try {
            json.put("command",cmd);
            json.put("user",UserID);
            json.put("group",GroupID);
            json.put("serial",DUID);
            json.put("ip",IPAddr);
            json.put("mac",MAC);
            json.put("os",OS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
    private void  httpCommand(String cmd,okhttp3.Callback callback){
        JSONObject command = makeJSON(cmd);
        mHttpServer += cmd;
        mHttpUtil.okHttpPostByJson(mHttpServer,command,callback);
        synchronized (key){
            try {
                SystemConfig.D(TAG,DebugEnabled,"http login,wait...");
                key.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private okhttp3.Callback mLoginCallback = new okhttp3.Callback(){

        @Override
        public void onFailure(Call call, IOException e) {
            synchronized (key){
                isAuthorized = false;
                SystemConfig.D(TAG,DebugEnabled,e.toString());
                key.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            synchronized (key){
                int respStatus  = response.code();
                BufferedReader mReader;
                String line = null;
                mReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                StringBuilder sb = new StringBuilder();
                while((line = mReader.readLine()) != null){
                    sb.append(line);
                }
                mReader.close();
                if(sb.toString() != null && !sb.toString().trim().equals("")){
                    try {
                        JSONObject o = new JSONObject(sb.toString());
                        isAuthorized = o.getBoolean("login_result");
                        if(isAuthorized) {
                            Start = o.getString("start_time");
                            End = o.getString("end_time");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                SystemConfig.D(TAG,DebugEnabled,"network ok");
                key.notify();
            }
        }
    };
    public void setOnLoignCallbackListener(setOnLoginCallback callback){
        mLoginCallbacks.addLast(callback);
    }


    @Override
    public void run() {
        if(!isAuthorized && (mCount < LOGIN_MAX_TIMES)){
            httpCommand("Login",mLoginCallback);
            mCount ++;
        }else{
            mCount = 0;
            isAuthorized = true;
            //发出需要这个的回调
            for(int index = 0; index < mLoginCallbacks.size(); index ++ ){
                mLoginCallbacks.get(index).loginCompleted(0);
            }
        }
        mHandler.postDelayed(this,5*1000);
    }

    public void onDestroy(){

    }
}
