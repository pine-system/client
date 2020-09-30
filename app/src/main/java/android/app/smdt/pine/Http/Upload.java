package android.app.smdt.pine.Http;

import android.app.smdt.pine.device.Device;
import android.app.smdt.pine.device.FileAttr;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Upload extends HttpImp implements Runnable{
    private static final int HTTP_UPLOAD_TIMEOUT = 1 * 1000;
    private static final String UPLOAD_SERVLET_NAME = Device.REMOTE_HTTP_SERVER_PAGE + "upload";
    private static final String TAG = Upload.class.getName();
    private static final boolean Debug = true;
    private String mCurUploadFileName;
    private LinkedList<FileAttr>mUploadFileList;
    public Upload(Device device) {
        super(device);
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mUploadFileList = new LinkedList<>();
    }
    public void insertFile(String mFileName,String mDestFilePath){
        File mFile = new File(mFileName);
        FileAttr attr = new FileAttr(mFile,mDestFilePath);
        mUploadFileList.addLast(attr);
    }
    @Override
    public void start() {
        mHandler.post(this);
    }

    public void stop(){

    }

    private Callback mUploadCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            synchronized (mKey){
                mKey.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            BufferedReader mReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
            String line;
            StringBuffer sb = new StringBuffer();
            while((line = mReader.readLine()) != null){
                sb.append(line);
            }
            try {
                JSONObject o  = new JSONObject(sb.toString());
                synchronized (mKey){
                    mKey.notify();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void run() {
        if(mUploadFileList != null && !mUploadFileList.isEmpty()){
            FileAttr attr = mUploadFileList.getFirst();
            Map<String,String>mParams = device.makeReqMap();
            mCurUploadFileName = attr.getFileName();
            mParams.put("filename",attr.getFileName());
            mParams.put("filelength",attr.getFileLength());
            mParams.put("hashcode",attr.getHashcode());
            mParams.put("destpath",attr.getDestpath());
            mHttpUtil.upLoadByMultipartAsync(UPLOAD_SERVLET_NAME,mParams,new File(attr.getFileName()),mUploadCallback);
            synchronized (mKey){
                try {
                    mKey.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mHandler.postDelayed(this,HTTP_UPLOAD_TIMEOUT);
        }
    }
}
