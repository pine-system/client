package android.app.smdt.pine;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomViewGroup;
import android.app.smdt.customui.text.Title;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedList;


public class SDCard extends BroadcastReceiver implements  Runnable{
    private static final String TAG = SDCard.class.getSimpleName();
    private static final boolean DebugEnabled = true;
    private static final int SDCARD_MORNITOR_TIMEOUT = 1000;
    //1.定义资源的路径
    private static final String SDCARD_PATH  = Environment.getExternalStorageDirectory().getPath();
    private static final String FOLDER_PATH =  SDCARD_PATH + File.separator +  "Nvtek";
    private static final String IMAGE_PATH  =  FOLDER_PATH +  File.separator + "images";
    private static final String VIDEO_PATH  =  FOLDER_PATH  + File.separator + "video";
    private static final String LOGO_PATH =   FOLDER_PATH  + File.separator + "logo";
    private static final String SUBTITLE_PATH = FOLDER_PATH  + File.separator + "subtitle";
    private static final String LAYOUT_PATH = FOLDER_PATH  + File.separator + "layout";
    private static final String DEBUG_PATH = FOLDER_PATH + File.separator + "debug";

    public static final int RESOURCE_TYPE_IMAGE = 0;
    public static final int RESOURCE_TYPE_VIDEO = RESOURCE_TYPE_IMAGE +1;
    public static final int RESOURCE_TYPE_LOGO = RESOURCE_TYPE_VIDEO + 1;
    public static final int RESOURCE_TYPE_SUBTITLE = RESOURCE_TYPE_LOGO + 1;
    public static final int RESOURCE_TYPE_LAYOUT = RESOURCE_TYPE_SUBTITLE + 1;
    public static final int RESOURCE_TYPE_MAX = RESOURCE_TYPE_LAYOUT + 1;
    //2.定义外部sd卡的路径
    private static final String EXTERNAL_SDCARD_PATH =  "/storage/";
    //3.定义内部数据链
    private LinkedList<File>mImageList;
    private LinkedList<File>mVideoList;
    private LinkedList<File>mLogoList;
    private LinkedList<File>mSubtitleList;
    private LinkedList<File>mLayoutList;

    private static String[] mFolderTbl ={
        IMAGE_PATH,
        VIDEO_PATH,
        LOGO_PATH,
        SUBTITLE_PATH,
        LAYOUT_PATH
    };
    private progressBarCallback mProgressBarCallback;
    public interface progressBarCallback{
        public void setProgressBarValue(File file,long max,int cur);
        public void updateResourceCompleted();
        public void autoPlayStop();

    }
    /**
     * 获取指定目录中的文件。
     * @param type 文件类型,根据文件的type找到对应的文件路径
     * @return
     */
    private File[] getFileList(final int type,File folder){

        File[] fileList = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String mFileName = pathname.getName().toLowerCase();
                switch(type){
                    case RESOURCE_TYPE_IMAGE:
                    case RESOURCE_TYPE_LOGO:
                        if(mFileName.contains(".jpg")
                                || mFileName.contains(".jpeg")
                                || mFileName.contains(".png")
                                || mFileName.contains(".bmp")){
                            return true;
                        }else{
                            return false;
                        }
                    case RESOURCE_TYPE_VIDEO:
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
                    case RESOURCE_TYPE_SUBTITLE:
                    case RESOURCE_TYPE_LAYOUT:
                        if(mFileName.contains(".txt")){
                            return true;
                        }else{
                            return false;
                        }
                }
                return false;
            }
        });
        return fileList;
    }

    /**
     * 创建资源文件的目录
     */
    private void makeResourcesFolderInSDCard(){
        File mFile = new File(FOLDER_PATH);
        if(mFile.exists() && mFile.isDirectory()){
            return;
        }
        for(int index = 0; index < mFolderTbl.length; index ++){
            mFile = new File(mFolderTbl[index]);
            mFile.mkdirs();
        }
    }

    private void updateResourceList(final int type, LinkedList<File> mResList){
        if(type < RESOURCE_TYPE_IMAGE || type >= RESOURCE_TYPE_MAX ){
            mResList.clear();
            return;
        }
        File mFile = new File(mFolderTbl[type]);
        File[] lists = getFileList(type,mFile);
        mResList.clear();
        if(lists == null || lists.length <= 0){
            return;
        }

        for(int index = 0; index < lists.length; index ++){
            mResList.add(lists[index]);
        }

    }

    FileInputStream mFin = null;
    FileOutputStream mFout = null;
    private void updateResourceLib(String cardPath) throws IOException, InterruptedException {


        //1.判断插入的usb sdcard中是否存在Nvtek目录，如果没有，马上退出。
        if (cardPath == null) {
            return;
        }
        String Ext_SDCard_Folder = cardPath + File.separator + "Nvtek";
        SystemConfig.D(TAG,DebugEnabled,"ext_sdcard_folder:" + Ext_SDCard_Folder);
        if (Ext_SDCard_Folder == null) {
            return;
        }
        File mFile = new File(Ext_SDCard_Folder);
        if (!mFile.exists() || !mFile.isDirectory()) {
            SystemConfig.D(TAG, DebugEnabled, Ext_SDCard_Folder + " not existed, don't update the resources.");
            return;
        }
        //考虑播放的时候，插入sd卡，需要停止当前的播放，并进行拷贝
        mProgressBarCallback.autoPlayStop();
        //2.既然需要更新，那么将资源库中存在的资源文件一定要全部删除
        for(int i = 0; i < mFolderTbl.length; i++){
            File[] oldResFiles = new File(mFolderTbl[i]).listFiles();
            SystemConfig.D(TAG, DebugEnabled,"file[" + i + "]:" + oldResFiles.length);
            if(oldResFiles != null && oldResFiles.length > 0){
                for(int j = 0; j < oldResFiles.length; j++) {
                    oldResFiles[j].delete();
                }
            }
        }

        //3.根据当前外部sdcard中的nvtek目录下的资源，开始逐一进行更新。
        for (int i = 0; i < mFolderTbl.length; i++) {
            //2.1 配置外部sdcar中的文件目录
            String Ext_SDCard_Folder_SubFolder = Ext_SDCard_Folder + File.separator + mFolderTbl[i].substring(mFolderTbl[i].lastIndexOf("/"));
            SystemConfig.D(TAG,DebugEnabled,"ext sdcard sub folder:" + Ext_SDCard_Folder_SubFolder);
            mFile = new File(Ext_SDCard_Folder_SubFolder);
            if (!mFile.exists() || !mFile.isDirectory()) {
                continue;
            }
            //2.2获取外部sdcar中的对应目录中的文件
            File[] fileList = getFileList(i,mFile);
            if (fileList == null || fileList.length <= 0) {
                continue;
            }
            //3.3配置文件名
            for (int j = 0; j < fileList.length; j++) {
                String in_sdcard_file = mFolderTbl[i] + File.separator + fileList[j].getName();
                mFin = new FileInputStream(fileList[j]);
                mFout = new FileOutputStream(in_sdcard_file);
                byte[] buffer = new byte[1024*1024];
                int length;
                while ((length = mFin.read(buffer)) > 0) {
                    mFout.write(buffer, 0, length);
                    mFout.flush();
                    mProgressBarCallback.setProgressBarValue(fileList[j],fileList[j].length(),length);
                    try {
                        Thread.sleep(100);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                mFin.close();
                mFout.close();
            }
        }
        //3.更新资源库完成
        SystemConfig.D(TAG,DebugEnabled,"update resource library completed.");
        mProgressBarCallback.updateResourceCompleted();
    }
    private String[] mResourceFolders = {
        "image",
        "layout",
        "logo",
        "subtitle",
        "video"
    };
    private static final int ASSETS_FOLDER_TYPE_VIDEO = 1;
    private static final int ASSETS_FOLDER_TYPE_IMAGE = 0;
    private static final int ASSETS_FOLDER_TYPE_LAYOUT = 4;
    private static final int ASSETS_FOLDER_TYPE_LOGO = 2;
    private static final int ASSETS_FOLDER_TYPE_SUBTITLE = 3;
    private void updateAssetsResource2SDCard(int type){
        String mFolder;
        String[] mFileNameArr;
        InputStream mFin;
        OutputStream mFout;

        switch(type){
            case ASSETS_FOLDER_TYPE_VIDEO:
                    mFolder = "video";
                break;
            case ASSETS_FOLDER_TYPE_IMAGE:
                    mFolder = "image";
                break;
            case ASSETS_FOLDER_TYPE_LAYOUT:
                    mFolder = "layout";
                break;
            case ASSETS_FOLDER_TYPE_LOGO:
                    mFolder = "logo";
                break;
            case ASSETS_FOLDER_TYPE_SUBTITLE:
                    mFolder = "subtitle";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        try {
            mFileNameArr = context.getAssets().list(mFolder);
            if(mFileNameArr != null && mFileNameArr.length > 0){
                for(int index = 0; index < mFileNameArr.length; index ++) {
                    mFout = new FileOutputStream(mFolderTbl[type] + File.separator + mFileNameArr[index]);
                   // mFin = context.getAssets().openFd(mFolder + File.separator + mFileNameArr[index]).createInputStream();
                    mFin = context.getAssets().open(mFolder + File.separator + mFileNameArr[index]);
                    //start to copy
                    byte[] buf = new byte[1024];
                    int length;
                    while((length = mFin.read(buf)) > 0){
                        SystemConfig.D(TAG,DebugEnabled,"file name:" + mFileNameArr[index] + ",length:" + length);
                        mFout.write(buf,0,length);
                    }
                    mFout.close();
                    mFin.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateAssetsOrRawResourceToSdcard(){
        updateAssetsResource2SDCard(ASSETS_FOLDER_TYPE_VIDEO);
        updateAssetsResource2SDCard(ASSETS_FOLDER_TYPE_IMAGE);
        updateAssetsResource2SDCard(ASSETS_FOLDER_TYPE_LAYOUT);
        updateAssetsResource2SDCard(ASSETS_FOLDER_TYPE_LOGO);
        updateAssetsResource2SDCard(ASSETS_FOLDER_TYPE_SUBTITLE);
    }
    /**
     * 设置sdcard /usb 的侦测
     */
    private static SDCard mCard = null;
    private String mCardPath = null;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private Context context;
   private SDCard(Context context,progressBarCallback callback) {
       this.context = context;
       mProgressBarCallback = callback;
       //1.初始化文件的链接
       mImageList = new LinkedList<>();
       mVideoList = new LinkedList<>();
       mLogoList = new LinkedList<>();
       mSubtitleList = new LinkedList<>();
       mLayoutList = new LinkedList<>();
       //2.判断资源文件路径是否存在，不存在，马上创建
       makeResourcesFolderInSDCard();
       //3.更新资源文件到链表中
       updateResourceList(RESOURCE_TYPE_IMAGE,mImageList);
       updateResourceList(RESOURCE_TYPE_VIDEO,mVideoList);
       updateResourceList(RESOURCE_TYPE_LOGO,mLogoList);
       updateResourceList(RESOURCE_TYPE_SUBTITLE,mSubtitleList);
       updateResourceList(RESOURCE_TYPE_LAYOUT,mLayoutList);

       //4.注册监听 sd usb的插入
       IntentFilter mFilter = new IntentFilter();
       mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
       mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
       mFilter.setPriority(Integer.MAX_VALUE);
       mFilter.addDataScheme("file");
       context.registerReceiver(this,mFilter);
       //5.因为注册的监听，不能执行很长时间，所以启动一个线程，进行拷贝、更新链表
       mHandlerThread = new HandlerThread(TAG);
       mHandlerThread.start();
       mHandler = new Handler(mHandlerThread.getLooper());
       mHandler.post(this);
   }
    public static SDCard getInstance(Context context, progressBarCallback callback){
       if(mCard == null){
           mCard = new SDCard(context,callback);
       }
       return mCard;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
                mCardPath = data.getPath();
                SystemConfig.D(TAG,DebugEnabled,"sdcard insert,path:" + mCardPath);
                mHandler.post(this);
        }
    }

    @Override
    public void run() {
            if(mCardPath != null) {
                synchronized (mCardPath) {
                    try {
                        updateResourceLib(mCardPath);
                        updateResourceList(RESOURCE_TYPE_IMAGE, mImageList);
                        updateResourceList(RESOURCE_TYPE_VIDEO, mVideoList);
                        updateResourceList(RESOURCE_TYPE_LOGO, mLogoList);
                        updateResourceList(RESOURCE_TYPE_SUBTITLE, mSubtitleList);
                        updateResourceList(RESOURCE_TYPE_LAYOUT, mLayoutList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCardPath = null;
                }
            }else{
                if(mLayoutList.isEmpty()){
                    updateAssetsOrRawResourceToSdcard();
                    updateResourceList(RESOURCE_TYPE_IMAGE, mImageList);
                    updateResourceList(RESOURCE_TYPE_VIDEO, mVideoList);
                    updateResourceList(RESOURCE_TYPE_LOGO, mLogoList);
                    updateResourceList(RESOURCE_TYPE_SUBTITLE, mSubtitleList);
                    updateResourceList(RESOURCE_TYPE_LAYOUT, mLayoutList);
                    mProgressBarCallback.updateResourceCompleted();
                }
            }

    }
    public LinkedList<File> getMediaList(int type){
       LinkedList<File>tmp = null;
       switch(type){
           case RESOURCE_TYPE_IMAGE:
               tmp = mImageList;
               break;
           case RESOURCE_TYPE_VIDEO:
               tmp = mVideoList;
               break;
           case RESOURCE_TYPE_LOGO:
               tmp = mLogoList;
               break;
           case RESOURCE_TYPE_SUBTITLE:
               tmp = mSubtitleList;
               break;
           case RESOURCE_TYPE_LAYOUT:
               tmp = mLayoutList;
               break;
       }
       return  tmp;
    }
    public LinkedList<File> getVideoList(){
       return mVideoList;
    }
    public LinkedList<File> getImageList(){
       return mImageList;
    }
    public LinkedList<File> getmSubtitleList(){
       return mSubtitleList;
    }
    public LinkedList<File> getmLogoList(){
       return mLogoList;
    }
    public LinkedList<File> getmLayoutList(){
       return mLayoutList;
    }
    public void onDestroy(){
       if(mHandler != null){
           mHandler.removeCallbacks(this);
       }
       context.unregisterReceiver(this);
    }

}
