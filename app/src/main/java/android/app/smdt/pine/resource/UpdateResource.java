package android.app.smdt.pine.resource;

import android.app.smdt.config.MD5Util;
import android.app.smdt.config.SystemConfig;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Response;

/**
 * 功能：1。创建 sdcard Nvtek的目录
 *      2。拷贝 assert目录中的资源
 *      3。拷贝 raw目录中的资源
 *      4。拷贝 sdcard中的资源
 *      5。拷贝 服务器中的资源
 */
public class UpdateResource extends BroadcastReceiver {
    private static final String TAG = "update";
    private static final boolean DebugEnabled = true;
    private static final String SDCARD_MAIN_FOLDER = "/sdcard/Nvtek";
    private static final String[] SubFolders = {
            "video",
            "image",
            "layout",
            "logo",
            "subtitle"
    };

    private InputStream mFin;
    private FileOutputStream mFout;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private List<UpdateResourceIntoSDCardCallback> UpdateResourceCallbackList;

    private LinkedList<FileAttr> mVideoList;
    private LinkedList<FileAttr> mImageList;
    private LinkedList<FileAttr> mLayoutList;
    private LinkedList<FileAttr> mLogoList;
    private LinkedList<FileAttr> mSubtitleList;
    private static UpdateResource mUpdate = null;
    private File mFile;
    private Context context;
    private Object mKey;
    private Network mNetwork;
    //设置更新过程的回调
    public interface UpdateResourceIntoSDCardCallback{
        public void updateResourceSDcardRatio(String fileName,String ratio);
        public void updateResourceSDcardCompleted();
        public void stopPlay();
    }
    //1。主线程。
    public static UpdateResource makeUpdateResource(Context context,Object key){
        if(null == mUpdate){
            mUpdate = new UpdateResource(context,key);
        }
        return mUpdate;
    }
    private UpdateResource(Context context,Object key){
        mKey = key;
        this.context = context;
        UpdateResourceCallbackList = new ArrayList<>();
        //1.每次开机，进入系统需要判断sdcard根目录中的Nvtek是否存在。
        mFile = new File(SDCARD_MAIN_FOLDER);
        if(!mFile.exists()){
            //2.创建SDCard中的Nvtek目录
            mFile.mkdirs();
            for(int index = 0; index < SubFolders.length; index ++){
                String mSubFolderPath = SDCARD_MAIN_FOLDER + File.separator + SubFolders[index];
                mFile = new File(mSubFolderPath);
                if(!mFile.exists()){
                    mFile.mkdirs();
                }
            }
        }
        //3.初始化资源列表
        mVideoList = new LinkedList<>();
        mImageList = new LinkedList<>();
        mLayoutList = new LinkedList<>();
        mLogoList = new LinkedList<>();
        mSubtitleList = new LinkedList<>();
        //4.考虑到获取资源文件信息需要大量的时间，故此启动一个线程来。
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        //5.注册监听 sd usb的插入
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mFilter.setPriority(Integer.MAX_VALUE);
        mFilter.addDataScheme("file");
        context.registerReceiver(this,mFilter);
        //6。注册监听 network的命令
        mNetwork = Network.getInstance(context);
        //7.启动线程
        mHandler.post(UpdateResourceThread);
    }
    //2。新建线程作为更新资源的主线程
    /**
     * 扫描Nvtek/目录中的所有文件
     */
    private static final int SDCARD_RESOURCE_TYPE_VIDEO = 0;
    private static final int SDCARD_RESOURCE_TYPE_IMAGE = 1;
    private static final int SDCARD_RESOURCE_TYPE_LAYOUT = 2;
    private static final int SDCARD_RESOURCE_TYPE_LOGO = 3;
    private static final int SDCARD_RESOURCE_TYPE_SUBTITLE = 4;
    private boolean isFileType(File file, int type){
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
                if(mFileName.contains(".txt")){
                    return true;
                }else{
                    return false;
                }
        }
        return false;
    }
    private LinkedList<FileAttr> scanResourceFolder(String path,final int fileType){
        LinkedList<FileAttr> mFileList;
        //1.文件判断
        if(null == path){
            return null;
        }
        File mFile = new File(path);
        if(!mFile.exists() || !mFile.canRead()){
            return null;
        }
        //2.开始获取FileAttr
        String[] mFileArray = mFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return isFileType(file,fileType);
            }
        });
        mFileList = new LinkedList<>();

        if(null == mFileArray){
            return mFileList;
        }
        for(int index = 0; index < mFileArray.length; index ++){
            FileAttr attr = new FileAttr(new File(mFileArray[index]));
            if(attr.isContainedInList(mFileList)){
                mFileList.add(attr);
            }
        }
        return mFileList;
    }

    private void UpdateResourceList(){
        mVideoList = scanResourceFolder(SDCARD_MAIN_FOLDER + File.separator + SubFolders[SDCARD_RESOURCE_TYPE_VIDEO],
                SDCARD_RESOURCE_TYPE_VIDEO);
        mImageList = scanResourceFolder(SDCARD_MAIN_FOLDER + File.separator + SubFolders[SDCARD_RESOURCE_TYPE_IMAGE],
                SDCARD_RESOURCE_TYPE_IMAGE);
        mLayoutList = scanResourceFolder(SDCARD_MAIN_FOLDER + File.separator + SubFolders[SDCARD_RESOURCE_TYPE_LAYOUT],
                SDCARD_RESOURCE_TYPE_LAYOUT);
        mLogoList = scanResourceFolder(SDCARD_MAIN_FOLDER + File.separator + SubFolders[SDCARD_RESOURCE_TYPE_LOGO],
                SDCARD_RESOURCE_TYPE_LOGO);
        mSubtitleList = scanResourceFolder(SDCARD_MAIN_FOLDER + File.separator + SubFolders[SDCARD_RESOURCE_TYPE_SUBTITLE],
                SDCARD_RESOURCE_TYPE_SUBTITLE);
        
        for(int li = 0; li < UpdateResourceCallbackList.size(); li ++){
            UpdateResourceCallbackList.get(li).updateResourceSDcardCompleted();
        }
    }
    //拷贝Assets中的资源文件到SDCard
    private void UpdateAssetResource2SDCard(){
        for(int index = 0; index < SubFolders.length; index ++){
            try {
                String[] mFileNameArray  = context.getAssets().list(SubFolders[index]);
                if(null != mFileNameArray && mFileNameArray.length > 0){
                    for(int p = 0; p < mFileNameArray.length; p ++) {
                        mFout = new FileOutputStream(SDCARD_MAIN_FOLDER + File.separator + SubFolders[index] + File.separator + mFileNameArray[p]);
                        mFin = context.getAssets().open(SubFolders[index] + File.separator + mFileNameArray[p]);
                        updateResourceFileIntoSDCard(mFileNameArray[p],mFout,mFin);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Runnable UpdateResourceThread = new Runnable() {
        @Override
        public void run() {
            synchronized (mKey) {
                try {
                    mKey.wait();
                    UpdateResourceList();
                    if (mLayoutList.isEmpty()) {
                        UpdateAssetResource2SDCard();
                        UpdateResourceList();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    //3.当有USB或者SD卡插入的时候，启动一个线程来来同步SD卡中的资源文件到内部SD卡中
    class UpdateResourceExternSDCard2SDCardThread implements Runnable{
        String mExternalSDCardPath;
        public UpdateResourceExternSDCard2SDCardThread(String path){
            mExternalSDCardPath = path;
        }
        @Override
        public void run() {
            try {
                UpdateExternalSDCardIntoSDCard(mExternalSDCardPath);
                mHandler.post(UpdateResourceThread);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        UpdateResourceExternSDCard2SDCardThread  mExternSDCardThread =
                new UpdateResourceExternSDCard2SDCardThread(data.getPath());
        new Thread(mExternSDCardThread).start();
    }

    //3.获取网络更新的资源文件，网络更新分为两步，
    // 1。文件信息

    //2。同步更新
    private void UpdateServerResourceIntoSDCard(String fileName,String inSDcardPath,Response response) throws IOException {
        mFin = response.body().byteStream();
        mFout = new FileOutputStream(inSDcardPath);
        updateResourceFileIntoSDCard(fileName,mFout,mFin);
        for(int li = 0; li < UpdateResourceCallbackList.size(); li ++){
            UpdateResourceCallbackList.get(li).updateResourceSDcardCompleted();
        }
        UpdateResourceList();
        mHandler.post(UpdateResourceThread);
    }
    //4.获取外部SDCard的资源文件
    private void UpdateExternalSDCardIntoSDCard(String path) throws IOException {
        //1.判断插入的usb sdcard中是否存在Nvtek目录，如果没有，马上退出。
        if (path == null) {
            return;
        }
        String Ext_SDCard_Folder = path + File.separator + "Nvtek";

        if (Ext_SDCard_Folder == null) {
            return;
        }
        File mFile = new File(Ext_SDCard_Folder);
        if (!mFile.exists() || !mFile.isDirectory()) {
            SystemConfig.D(TAG, DebugEnabled, Ext_SDCard_Folder + " not existed, don't update the resources.");
            return;
        }
        //考虑播放的时候，插入sd卡，需要停止当前的播放，并进行拷贝
        for(int index = 0; index < UpdateResourceCallbackList.size(); index ++){
            UpdateResourceCallbackList.get(index).stopPlay();
        }
        //2.既然需要更新，那么将资源库中存在的资源文件一定要全部删除
        for(int i = 0; i < SubFolders.length; i++){
            File[] oldResFiles = new File(SDCARD_MAIN_FOLDER + File.separator +  SubFolders[i]).listFiles();
            SystemConfig.D(TAG, DebugEnabled,"file[" + i + "]:" + oldResFiles.length);
            if(oldResFiles != null && oldResFiles.length > 0){
                for(int j = 0; j < oldResFiles.length; j++) {
                    oldResFiles[j].delete();
                }
            }
        }

        //3.根据当前外部sdcard中的nvtek目录下的资源，开始逐一进行更新。
        for (int i = 0; i < SubFolders.length; i++) {
            //2.1 配置外部sdcar中的文件目录
            String Ext_SDCard_Folder_SubFolder = Ext_SDCard_Folder + File.separator + SubFolders[i];
            SystemConfig.D(TAG,DebugEnabled,"ext sdcard sub folder:" + Ext_SDCard_Folder_SubFolder);
            mFile = new File(Ext_SDCard_Folder_SubFolder);
            if (!mFile.exists() || !mFile.isDirectory()) {
                continue;
            }
            //2.2获取外部sdcar中的对应目录中的文件
            File[] fileList = mFile.listFiles();
            if (fileList == null || fileList.length <= 0) {
                continue;
            }
            //3.3配置文件名
            for (int j = 0; j < fileList.length; j++) {
                String in_sdcard_file = SDCARD_MAIN_FOLDER + File.separator + SubFolders[i] + File.separator + fileList[j].getName();
                mFin = new FileInputStream(fileList[j]);
                mFout = new FileOutputStream(in_sdcard_file);
                updateResourceFileIntoSDCard(fileList[j].getName(),mFout,mFin);
            }
        }
        //3.更新资源库完成
        SystemConfig.D(TAG,DebugEnabled,"update resource library completed.");
        for(int li = 0; li < UpdateResourceCallbackList.size(); li ++){
            UpdateResourceCallbackList.get(li).updateResourceSDcardCompleted();
        }
    }

    private boolean updateResourceFileIntoSDCard(String fileName,FileOutputStream fout,InputStream in) throws IOException {
        if(fout == null || in == null){
            return false;
        }
        DecimalFormat format = new DecimalFormat("#.00");
        int mCount = 0;
        int total = in.available();
        byte[] buf = new byte[1024];
        int len;
        float ratio;
        while((len = in.read(buf)) > 0){
            fout.write(buf,0,len);
            mCount +=  len;
            for(int index = 0; index < UpdateResourceCallbackList.size(); index ++){
                ratio = mCount / total;
                String scale = format.format(ratio);
                String result = Float.parseFloat(scale) * 100 + "%";
                UpdateResourceCallbackList.get(index).updateResourceSDcardRatio(fileName,result);
            }
        }
        fout.close();
        mFin.close();
        return true;
    }
    public void setOnUpdateResourceLinstener(UpdateResourceIntoSDCardCallback callback){
        UpdateResourceCallbackList.add(callback);
    }
    public void onDestroy(){
        UpdateResourceCallbackList.clear();
        UpdateResourceCallbackList = null;
    }
    //设定文件属性类
    class FileAttr{
        String mFileName;
        long mFileLength;
        String hashCode;
        public FileAttr(File file){
            mFileName = file.getName();
            mFileLength = file.length();
            hashCode = MD5Util.getFileMD5Code(file);
        }
        public boolean isContainedInList(LinkedList<FileAttr>list){
            if(list == null){
                return false;
            }
            for(int index = 0; index < list.size(); index ++){
                FileAttr attr = list.get(index);
                if(attr.mFileName.equals(mFileName)
                    && attr.mFileLength == attr.mFileLength
                    && attr.hashCode.equals(attr.hashCode)){
                    return false;
                }
            }
            return true;
        }
    }
}
