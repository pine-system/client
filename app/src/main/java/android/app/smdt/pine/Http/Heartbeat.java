package android.app.smdt.pine.Http;

import android.app.smdt.pine.device.Device;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Heartbeat extends HttpImp implements Runnable{
    private static final String HEARTBEAT_SERVLET_NAME = Device.REMOTE_HTTP_SERVER_PAGE + "heartbeat";
    private static final String TAG = Heartbeat.class.getName();
    private static final int HEARTBEAT_TIMEOUT = 10 * 1000;
    private static final boolean Debug = true;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public interface HeartbeatCallback{
        public void heartbeatNetworkDisconnet();
    }
    private LinkedList<HeartbeatCallback> mHeartbeatCallbackList;
    private Context context;
    public Heartbeat(Context context,Device device) {
        super(device);
        this.context = context;
        mHeartbeatCallbackList = new LinkedList<>();
        mHandlerThread = new HandlerThread(TAG);
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void start() {
        mHandler.post(this);
    }
    private Callback mHeartbeatCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            dispatchHeartbeatNewworkDisconnect();
            synchronized (mKey){
                mKey.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            mReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            String line;
            StringBuffer sb = new StringBuffer();
            while((line = mReader.readLine()) != null){
                sb.append(line);
            }
            mReader.close();
            try {
                mResponseJSON = new JSONObject(sb.toString());
                switch(mResponseJSON.getInt("command")){
                    case CommandParser.COMMAND_FLAG_REBOOT:
                        CommandParser.reboot(context);
                        break;
                    case CommandParser.COMMAND_FLAG_SET_SYSTEM_TIME:
                        CommandParser.updateSystemTime(context,mResponseJSON);
                        break;
                    case CommandParser.COMMAND_FLAG_SET_POWERONOFF:
                        CommandParser.setPowerOnOff(context,mResponseJSON);
                        break;
                    case CommandParser.COMMAND_FLAG_SCREENSHOT:
                        CommandParser.ScreenCap(context);
                        break;
                    case CommandParser.COMMAND_FLAG_DOWNLOAD_RESOURCE:
                        CommandParser.DownloadResource(context);
                        break;
                    case CommandParser.COMMAND_FLAG_DOWNLOAD_FIRMWARE:
                        CommandParser.DownloadFirmware(context);
                        break;
                    case CommandParser.COMMAND_FLAG_UPGRADE_APK:
                        CommandParser.downloadApk(context);
                        break;
                    case CommandParser.COMMAND_FLAG_UPDATE_VOLUME:
                        break;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    @Override
    public void run() {
            mHttpUtil.okHttpPostByJson(HEARTBEAT_SERVLET_NAME,device.makeReqJSON(Device.HTTP_REQUEST_HEARTBEAT_COMMAND),mHeartbeatCallback);
            synchronized (mKey){
                try {
                    mKey.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mHandler.postDelayed(this,HEARTBEAT_TIMEOUT);
    }
    public void stop(){
        mHandler.removeCallbacks(this);
    }
    public void setOnHeartbeatListener(HeartbeatCallback callback){
        mHeartbeatCallbackList.add(callback);
    }
    private void dispatchHeartbeatNewworkDisconnect(){
        for(int index = 0; index < mHeartbeatCallbackList.size(); index++){
            mHeartbeatCallbackList.get(index).heartbeatNetworkDisconnet();
        }
    }
}
