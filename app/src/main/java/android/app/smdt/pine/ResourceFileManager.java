package android.app.smdt.pine;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.pine.Http.Download;
import android.app.smdt.pine.Http.Heartbeat;
import android.app.smdt.pine.Http.Login;
import android.app.smdt.pine.Http.Upload;
import android.app.smdt.pine.device.Device;
import android.app.smdt.pine.device.FileAttr;
import android.app.smdt.pine.resource.Asset;
import android.app.smdt.pine.resource.SDCard;
import android.content.Context;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;

public class ResourceFileManager implements Download.DownloadCallback{
    private static final String TAG = ResourceFileManager.class.getName();
    private static final boolean Debug = true;
    private static final int DEFAULT_LAYOUT_INDEX = 0;

    private static final String RESOURCE_PATH = Device.MAIN_SDCARD_PATH;
    public static final int SDCARD_RESOURCE_TYPE_VIDEO = 0;
    public static final int SDCARD_RESOURCE_TYPE_IMAGE = 1;
    public static final int SDCARD_RESOURCE_TYPE_LAYOUT = 2;
    public static final int SDCARD_RESOURCE_TYPE_LOGO = 3;
    public static final int SDCARD_RESOURCE_TYPE_SUBTITLE = 4;
    public static final int SDCARD_RESOURCE_TYPE_APK = 5;
    public static final int SDCARD_RESOURCE_TYPE_FIRMWARE = 6;
    public static final int SDCARD_RESOURCE_TYPE_CATPION = 7;

    private LinkedList<FileAttr>mVideoList;
    private LinkedList<FileAttr>mImageList;
    private LinkedList<FileAttr>mCaptionList;
    private LinkedList<FileAttr>mLogoList;
    private LinkedList<FileAttr>mAppList;
    private LinkedList<FileAttr>mFwList;
    private LinkedList<FileAttr>mLayoutList;
    public static boolean isFileType(File file, int type){
        String mFileName = file.getName().toLowerCase();
        switch(type){
            case SDCARD_RESOURCE_TYPE_IMAGE:
            case SDCARD_RESOURCE_TYPE_LOGO:
                if(mFileName.contains(".jpg")
                        || mFileName.contains(".jpeg")
                        || mFileName.contains(".png")
                        || mFileName.contains(".bmp")){
                    return true;
                }else{
                    return false;
                }
            case SDCARD_RESOURCE_TYPE_VIDEO:
                if(mFileName.contains(".mov")
                        || mFileName.contains(".mkv")
                        || mFileName.contains(".avi")
                        || mFileName.contains(".mpg")
                        || mFileName.contains(".mp4")
                        || mFileName.contains(".mpeg")){
                    return true;
                }else{
                    return false;
                }
            case SDCARD_RESOURCE_TYPE_LAYOUT:
            case SDCARD_RESOURCE_TYPE_SUBTITLE:
            case SDCARD_RESOURCE_TYPE_CATPION:
                if(mFileName.contains(".txt")){
                    return true;
                }else{
                    return false;
                }
            case SDCARD_RESOURCE_TYPE_APK:
                if(mFileName.contains(".apk")){
                    return true;
                }else{
                    return false;
                }
        }
        return false;
    }
    private LinkedList<FileAttr> initResList(String mFolder, final int type){
        if(null == mFolder){
            return null;
        }
        LinkedList<FileAttr> mList = new LinkedList<FileAttr>();
        mList.clear();
        String mResFolder = RESOURCE_PATH +  File.separator + mFolder;
        File mFile = new File(mResFolder);
        if(!mFile.exists() || !mFile.isDirectory()){
            return mList;
        }
        String[] mFileList = mFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return isFileType(file,type);
            }
        });
        if(null == mFileList || mFileList.length <= 0){
            return mList;
        }
        for(int index = 0; index < mFileList.length; index ++){
            FileAttr attr = new FileAttr(new File(mFileList[index]),null);
            if(!attr.isInList(mList)){
                mList.addLast(attr);
            }
        }
        return mList;
    }



    public LinkedList<FileAttr> getVideoList(){
        return mVideoList;
    }
    public LinkedList<FileAttr> getImageList(){
        return mImageList;
    }
    public LinkedList<FileAttr> getCaptionList(){
        return mCaptionList;
    }
    public LinkedList<FileAttr> getLogoList(){
        return mLogoList;
    }
    public LinkedList<FileAttr> getAppList(){
        return mAppList;
    }
    public LinkedList<FileAttr> getFirmwareList(){
        return mFwList;
    }
    public static final int MAIN_SCREEN_LAYOUT_INDEX = 0;
    public static final int VICE_SCREEN_LAYOUT_INDEX = 1;
    public JSONObject getScreenLayout(int index){
        JSONObject mLayoutJSON = null;
        if(mLayoutList == null || mLayoutList.isEmpty()){
            return null;
        }
        if(index < 0  || index >= mLayoutList.size()){
            index = 0;
        }
        BufferedReader mReader = null;
        String line = null;
        StringBuffer sb = new StringBuffer();
        try {
            mReader = new BufferedReader(new FileReader(mLayoutList.get(DEFAULT_LAYOUT_INDEX).getFileName()));
            while ((line = mReader.readLine()) != null) {
                sb.append(line);
            }
            mReader.close();
            mLayoutJSON = new JSONObject(sb.toString().trim());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }finally {
            return mLayoutJSON;
        }
    }

    private Context context;
    private Login mLogin;
    private Heartbeat mHeartbeat;
    private Download mDownload;
    private Upload mUpload;
    private Asset mAsset;
    private SDCard mSD;
    private Device mDevice;
    private boolean isDownloadCompleted;

    public ResourceFileManager(Context context, Login mLogin, Heartbeat mHeartbeat, Download mDownload, Upload mUpload,Asset mAsset,SDCard mSD,Device mDevice){
            this.context = context;
            this.mLogin = mLogin;
            this.mHeartbeat = mHeartbeat;
            this.mDownload = mDownload;
            this.mUpload = mUpload;
            this.mAsset = mAsset;
            this.mSD = mSD;
            this.mDevice = mDevice;
            isDownloadCompleted = false;
    }
    //1.准备好资源文件夹
    public void makeResourceFolder(){
        File mFile = new File(RESOURCE_PATH);
        if(mFile.exists() && mFile.isDirectory()){
            return;
        }
        for(int index = 0; index < Device.mSubFolderSet.length; index ++){
            String mFolder = RESOURCE_PATH + File.separator + Device.mSubFolderSet[index];
            mFile = new File(mFolder);
            mFile.mkdirs();
        }
    }
    public boolean getDeviceAuthorized(){
        return mDevice.getDeviceAuthorized();
    }
    public void startLogin(){
        mLogin.start();
    }
    public void startHeartbeat(){
        mHeartbeat.start();
    }
    public void startResourceDup()  {
        mDownload.start();
    }
    public boolean resourceDUPCompleted(){
        return isDownloadCompleted;
    }

    @Override
    public void downloadState(boolean success) {
        isDownloadCompleted = success;
    }
    private boolean mResourceCompleted = false;
    @Override
    public void downloadItemState(boolean success) {
        if(!success){
            try {
                mAsset.update();
                mResourceCompleted = true;
            } catch (IOException e) {
                e.printStackTrace();
                mResourceCompleted = false;
            }
        }
    }
}
