package android.app.smdt.pine.Http;

import android.app.smdt.pine.device.Device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Download extends HttpImp{
    private static final String DOWNLOAD_SERVLET_NAME = Device.REMOTE_HTTP_SERVER_PAGE + "download";
    private static final String TAG = Download.class.getName();
    private static final int DOWNLOAD_TIMEOUT =  100;
    private Object mCompleted;
    private boolean isDownloadTableSuccess = false;
    private boolean isDownloadItemSuccess = false;
    private boolean isDownloadAllItemSuccess = false;
    private Map<String,LinkedList<String>>mRemoteResourceMap;

    public interface DownloadCallback{
        public void downloadState(boolean success);
        public void downloadItemState(boolean success);
    }
    private LinkedList<DownloadCallback> mDownLoadListenterList;
    public void setOnDownloadListener(DownloadCallback callback){
        mDownLoadListenterList.addLast(callback);
    }
    public void dispatchDownloadListener(boolean success){
        for(DownloadCallback c: mDownLoadListenterList){
            c.downloadState(success);
        }
    }
    public void dispatchDownloadItemListener(boolean success){
        for(DownloadCallback c: mDownLoadListenterList){
            c.downloadItemState(success);
        }
    }
    JSONObject mResourceJSON;
    public Download(Device device) {
        super(device);
        mDownLoadListenterList = new LinkedList<>();
        mRemoteResourceMap = new HashMap<>();
        mCompleted = new Object();
    }
    //////////////////////////////////////////////下载资源项////////////////////////////////////////////////////////////////////
    private String mCurDownloadFileName;
    private Callback mDownloadItemCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            isDownloadItemSuccess = false;
            synchronized (mKey){
                mKey.notify();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            InputStream mFin = response.body().byteStream();
            FileOutputStream mFout = new FileOutputStream(Device.MAIN_SDCARD_PATH + File.separator + mCurDownloadFileName);
            byte[] buf = new byte[1024];
            int len;
            while((len = mFin.read(buf)) > 0){
                mFout.write(buf,0,len);
            }
            mFout.close();
            mFin.close();
            isDownloadItemSuccess = true;
            synchronized (mKey){
                mKey.notify();
            }
        }
    };
    //////////////////////////////////////////下载资源文件列表//////////////////////////////////////////////////////////////////////////////
    private Callback mDownloadTableCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            isDownloadTableSuccess = false;
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
                mResourceJSON = new JSONObject(sb.toString());
                isDownloadTableSuccess = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                if(mReader != null){
                    mReader.close();
                }
                synchronized (mKey){
                    mKey.notify();
                }
            }
        }
    };
    private boolean  downloadResourceTable(){
        mHttpUtil.okHttpPostByJson(DOWNLOAD_SERVLET_NAME,device.makeReqJSON(Device.HTTP_REQUEST_DOWNLOAD_TABLE),mDownloadTableCallback);
        synchronized (mKey){
            try {
                mKey.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                return isDownloadTableSuccess;
            }
        }
    }

    private boolean parseRemoteDownloadTable(){
        if(mResourceJSON == null){
            return false;
        }
        for(String mFolder : Device.mSubFolderSet){
            if(mResourceJSON.has(mFolder)){
                try {
                    JSONArray arr = mResourceJSON.getJSONArray(mFolder);
                    LinkedList<String> list = new LinkedList<String>();
                    for (int pos = 0; pos < arr.length(); pos++) {
                        String name = ((JSONObject) arr.get(pos)).getString("filename");
                        list.addLast(name);
                    }
                    mRemoteResourceMap.put(mFolder, list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!mRemoteResourceMap.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    private static final int DOWNLOAD_TABLE_CONTENT = 0;
    private static final int DOWNLOAD_PARSE_TABLE_CONTENT = 1;
    private static final int DOWNLOAD_RESOURCE_ITEM_PREPARE = 2;
    private static final int DOWNLOAD_RESOURCE_ITEM_START = 3;
    private static final int DOWNLOAD_COMPLETED = 4;
    private int downloadState = DOWNLOAD_TABLE_CONTENT;
    private LinkedList<String>mFileNameList;

    @Override
    public void run() {
        switch(downloadState){
            //1.发出获取下载资源的表。目的为了下一步好一条一条的下载
            case DOWNLOAD_TABLE_CONTENT:
                if(downloadResourceTable()){
                    downloadState = DOWNLOAD_PARSE_TABLE_CONTENT;
                }else{
                    downloadState = DOWNLOAD_COMPLETED;
                }
                break;
            //2.解析下载资源列表
            case DOWNLOAD_PARSE_TABLE_CONTENT:
                if(parseRemoteDownloadTable()){
                    downloadState = DOWNLOAD_RESOURCE_ITEM_PREPARE;
                }else{
                    downloadState = DOWNLOAD_COMPLETED;
                }
                break;
                //3.开始解析资源项
            case DOWNLOAD_RESOURCE_ITEM_PREPARE:
                if(!mRemoteResourceMap.isEmpty()) {
                    Iterator<String> mIte = mRemoteResourceMap.keySet().iterator();
                    if (mIte.hasNext()) {
                        String mItemName = mIte.next();
                        mFileNameList = mRemoteResourceMap.get(mItemName);
                        mRemoteResourceMap.remove(mItemName);
                        downloadState = DOWNLOAD_RESOURCE_ITEM_START;
                    }
                }else{
                    downloadState = DOWNLOAD_COMPLETED;
                }
                break;
                //4.开始下载
            case DOWNLOAD_RESOURCE_ITEM_START:
                if(!mFileNameList.isEmpty()){
                    mCurDownloadFileName = mFileNameList.pop();
                    mHttpUtil.okHttpPostByJson(DOWNLOAD_SERVLET_NAME,device.makeReqDownloadItemJSON(Device.HTTP_REQUEST_DOWNLOAD_ITEM,mCurDownloadFileName),mDownloadItemCallback);
                    synchronized (mKey){
                        try {
                            mKey.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    dispatchDownloadItemListener(isDownloadItemSuccess);
                    isDownloadAllItemSuccess &= isDownloadItemSuccess;
                }else {
                    downloadState = DOWNLOAD_RESOURCE_ITEM_PREPARE;
                }
                break;
            case DOWNLOAD_COMPLETED:
                dispatchDownloadListener(isDownloadAllItemSuccess);
                 return;
        }
        mHandler.postDelayed(this,DOWNLOAD_TIMEOUT);
    }

    @Override
    public void start() {
        //考虑下载时间过程可能很长，创建一个新的线程处理
        mHandler.post(this);
    }
}
