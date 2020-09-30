package android.app.smdt.pine.resource;

import android.app.Activity;
import android.app.smdt.pine.device.Device;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

public class SDCard extends DUPSDCard  {
    private static final String TAG = SDCard.class.getName();
    private static final Boolean Debug = true;


    private String mExtSDCard;
    public SDCard(Context context, Device device){
        super(context,device);
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mFilter.setPriority(Integer.MAX_VALUE);
        mFilter.addDataScheme("file");
        context.registerReceiver(mBroadcastReceiver,mFilter);
    }

    public boolean update(){
        return false;
    }
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mExtSDCard = intent.getData().getPath();
            File mFile = new File(mExtSDCard + File.separator + "nvtek");
            if(!mFile.exists() || !mFile.isDirectory()){
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String extFolder;
                    String inFolder;
                    for(int index = 0; index < mSubFolderSet.length; index ++){
                        extFolder = mExtSDCard + File.separator + mSubFolderSet[index];
                        inFolder = MAIN_SDCARD_RESOURCE_PATH + File.separator + mSubFolderSet[index];

                        String[] extFiles =  new File(extFolder).list(new FilenameFilter() {
                            @Override
                            public boolean accept(File file, String s) {
                                return true;
                            }
                        });
                        if(null == extFiles || extFiles.length <= 0){
                            continue;
                        }

                        for(String fileName : extFiles){
                            File mExtFile = new File(fileName);
                            String mExtFileName = mExtFile.getName();
                            File mInFile = new File(inFolder + File.separator + mExtFileName);
                            try {
                                mFin = new FileInputStream(mExtFile);
                                mFout = new FileOutputStream(mInFile);
                                start();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }
            }).start();
        }
    };

}
