package android.app.smdt.pine.device;

import android.app.smdt.config.SystemConfig;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Device {
    private static final String TAG = Device.class.getName();
    private static final boolean Debug = true;
    ///////////////////////////////////////本机对应的网络服务器/////////////////////////////////////////////////////////////
    private static final String REMOTE_HTTP_SERVER_ADDR = "http://192.168.0.105";
    private static final String REMOTE_HTTP_SERVER_PORT = "8080";
    private static final String REMOTE_PROJECT_NAME = "pine";
    public static final String REMOTE_HTTP_SERVER_PAGE = REMOTE_HTTP_SERVER_ADDR + ":" + REMOTE_HTTP_SERVER_PORT + File.separator
            + REMOTE_PROJECT_NAME + File.separator;
    public static int HTTP_REQUEST_LOGIN_COMMAND = 1;
    public static int HTTP_REQUEST_LOGIN_COMPLETE_ALL_INFO_COMMAND = 2;
    public static int HTTP_REQUEST_HEARTBEAT_COMMAND = 3;
    public static int HTTP_REQUEST_DOWNLOAD_TABLE = 4;
    public static int HTTP_REQUEST_DOWNLOAD_ITEM = 5;
    /////////////////////////////////////////////////SDCARD//////////////////////////////////////////////////////////////////////
    public static final String MAIN_SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "nvtek";
    public String EXT_SDCARD_PATH;
    public static final String[] mSubFolderSet ={
            "video",
            "image",
            "subtitle",
            "logo",
            "layout",
            "apk",
            "firmware"
    };
    /////////////////////////////////////////////////本机信息/////////////////////////////////////////////////////////////////////////
    private String user;
    private int gid;
    private String client;
    private String mac;
    private String ip;
    private String soc;
    private String os;
    private String valid;
    private String netType;
    private boolean isAuthorized;
    private boolean isConnected;
    private JSONObject mReqJSON = null;

    public JSONObject makeReqDownloadItemJSON(int command,String name){
        try {
            mReqJSON = new JSONObject();
            mReqJSON.put("command",command);
            mReqJSON.put("user",user);
            mReqJSON.put("gid",gid);
            mReqJSON.put("client",client);
            mReqJSON.put("fileName",name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mReqJSON;
    }
    public JSONObject makeReqJSON(int command) {
        try {
            mReqJSON = new JSONObject();
            mReqJSON.put("command",command);
            mReqJSON.put("user",user);
            mReqJSON.put("gid",gid);
            mReqJSON.put("client",client);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mReqJSON;
    }
    public JSONObject makeReqAllJSON(int command) {
        try {
            mReqJSON = new JSONObject();
            mReqJSON.put("command",command);
            mReqJSON.put("user",user);
            mReqJSON.put("gid",gid);
            mReqJSON.put("client",client);
            mReqJSON.put("mac",mac);
            mReqJSON.put("ip",ip);
            mReqJSON.put("soc",soc);
            mReqJSON.put("os",os);
            mReqJSON.put("net",netType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mReqJSON;
    }
    public Map<String,String> makeReqMap(){
        Map<String,String> map = new HashMap<>();
        map.put("user",user);
        map.put("gid",gid+"");
        map.put("client",client);
        return map;
    }

    public boolean parseLoginJSON(JSONObject o) {
        if(null == o){
            return false;
        }
        try {
            if(o.has("valid")) {
                valid = o.getString("valid");
            }
            String[] dates = valid.split(" ");
            long start = SystemConfig.getDateSystemTime(dates[0]);
            long end = SystemConfig.getDateSystemTime(dates[1]);
            long cur = Calendar.getInstance().getTimeInMillis();
            if(cur >= start  && cur <= end){
                isAuthorized = true;
            }
            return true;
        } catch (JSONException e) {
                e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("user:" + user + "\n");
        sb.append("gid:" + gid + "\n");
        sb.append("client:" + client + "\n");
        sb.append("soc:" + soc + "\n");
        sb.append("os:" + os + "\n" );
        sb.append("ip:" + ip + "\n");
        sb.append("mac:" + mac + "\n");
        sb.append("net type:" + netType +"\n");
        return sb.toString();
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Device(Context context){
        mReqJSON = new JSONObject();
        client = SystemConfig.getSystemSerial();
        user = SystemConfig.PropertiesGet("ro.user.id","nvtek");
        gid = Integer.parseInt(SystemConfig.PropertiesGet("ro.group.id","1"));
        soc = Build.HARDWARE;
        os = Build.PRODUCT;
        isConnected = SystemConfig.isNetworkConnected(context);
        if(netType != null){
            netType = SystemConfig.getActiveNetworkType(context);
            ip = SystemConfig.getActiveNetworkIP();
            mac = SystemConfig.getActiveNetworkMACFromIP();
        }
    }
    public void setDeviceAuthorized(boolean enable){
        isAuthorized = enable;
    }
    public boolean getDeviceAuthorized(){
        return isAuthorized;
    }
}
