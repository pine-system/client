package android.app.smdt.pine.Http;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.httputil.HttpUtil;
import android.app.smdt.pine.device.Device;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Login extends HttpImp{
    private static final String LOGIN_SERVLET_NAME = Device.REMOTE_HTTP_SERVER_PAGE + "Login";
    private static final String TAG = Login.class.getName();
    private static final boolean Debug = true;

    public Login(Device device){
       super(device);
    }

    public void onDestroy(){

    }
    Callback mLoginCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            device.setDeviceAuthorized(false);
            synchronized (mKey){
                mKey.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response){
            BufferedReader mReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            try {
                while((line = mReader.readLine()) != null){
                    sb.append(line);
                }
                mReader.close();
                mResponseJSON = new JSONObject(sb.toString());
                device.parseLoginJSON(mResponseJSON);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }finally {
                synchronized (mKey){
                    mKey.notify();
                }
            }
        }
    };

    private Callback mLoginAllCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            device.setDeviceAuthorized(false);
            synchronized (mKey){
                mKey.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //只是再次上传完整信息，可以不处理了
        }
    };
    public void start(){
        mHandler.post(this);
    }

    @Override
    public void run() {
        mHttpUtil.okHttpPostByJson(LOGIN_SERVLET_NAME,device.makeReqJSON(Device.HTTP_REQUEST_LOGIN_COMMAND),mLoginCallback);
        synchronized (mKey){
            try {
                mKey.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(device.getDeviceAuthorized()) {
            mHttpUtil.okHttpPostByJson(LOGIN_SERVLET_NAME, device.makeReqAllJSON(Device.HTTP_REQUEST_LOGIN_COMPLETE_ALL_INFO_COMMAND), mLoginAllCallback);
            synchronized (mKey) {
                try {
                    mKey.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
