package android.app.smdt.pine.Http;

import android.app.smdt.httputil.HttpUtil;
import android.app.smdt.pine.device.Device;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;

public abstract class HttpImp implements Runnable{
    protected BufferedReader mReader;
    protected HttpUtil mHttpUtil;
    protected JSONObject mResponseJSON;
    protected JSONObject mRequestJSON;
    protected Object mKey;
    protected Device device;
    protected HandlerThread mHandlerThread;
    protected Handler mHandler;

    public HttpImp(Device device){
        this.device = device;
        mKey = new Object();
        mHttpUtil = new HttpUtil();
        try {
            mRequestJSON = device.makeReqJSON(Device.HTTP_REQUEST_LOGIN_COMMAND);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public boolean getDeviceAuthorized(){
        return device.getDeviceAuthorized();
    }
    public abstract void start() throws InterruptedException, JSONException;
}
