package android.app.smdt.pine.resource;

import android.app.smdt.pine.device.Device;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Asset extends DUPSDCard{
    private static final String TAG = Asset.class.getName();
    private static final boolean Debug = true;
    private Context context;
    public Asset(Context context, Device device){
        super(context,device);
    }
    public boolean update() throws IOException {
        for(String mFolder : Device.mSubFolderSet){
            String[] mAssetFileArray = context.getAssets().list(mFolder);
            if(null == mAssetFileArray || mAssetFileArray.length <= 0){
                continue;
            }
            for(String mFileName : mAssetFileArray){
                mFin = context.getAssets().open(mFolder + File.separator + mFileName);
                mFout = new FileOutputStream(mFolder + File.separator + mFileName);
                start();
            }
        }
        return true;
    }

}
