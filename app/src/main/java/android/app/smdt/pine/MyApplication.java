package android.app.smdt.pine;

import android.app.Application;
import android.app.smdt.pine.Http.Download;
import android.app.smdt.pine.Http.Heartbeat;
import android.app.smdt.pine.Http.HttpImp;
import android.app.smdt.pine.Http.Login;
import android.app.smdt.pine.Http.Upload;
import android.app.smdt.pine.device.Device;
import android.app.smdt.pine.resource.Asset;
import android.app.smdt.pine.resource.SDCard;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class MyApplication extends Application {
    private Device mDevice;
    private Login mLogin;
    private Heartbeat mHeartbeat;
    private Download mDownload;
    private Upload mUpload;
    private ResourceFileManager mRFM;
    private Asset mAsset;
    private SDCard mSD;
    @Override
    public void onCreate() {
        super.onCreate();
        mDevice = new Device(this);
        mLogin = new Login(mDevice);
        mHeartbeat = new Heartbeat(this,mDevice);
        mDownload = new Download(mDevice);
        mUpload = new Upload(mDevice);
        mAsset = new Asset(this,mDevice);
        mSD = new SDCard(this,mDevice);
        mRFM = new ResourceFileManager(this,mLogin,mHeartbeat,mDownload,mUpload,mAsset,mSD,mDevice);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public Device getDevice(){
        return mDevice;
    }
    public Login getLogin(){
        return mLogin;
    }
    public Heartbeat getHeartbeat(){
        return mHeartbeat;
    }
    public ResourceFileManager getResourceFileManager(){
        return mRFM;
    }

}
