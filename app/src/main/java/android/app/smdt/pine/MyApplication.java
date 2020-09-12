package android.app.smdt.pine;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();
    private static final boolean DebugEnabled = true;
   // private DeviceInfo mDeviceInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        //1.查看本机授权状态
       // mDeviceInfo = DeviceInfo.getInstance(this);
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
/*
    public DeviceInfo  getDeviceInfo(){
        return mDeviceInfo;
    }
    public DisplayMetrics getDisplayMetrics(){
        return SystemConfig.getDisplayMetricsById(this,0);
    }
    public boolean isAuthorized(){
        return mDeviceInfo.isAuthorized();
    }
    public boolean isConnected(){
        return mDeviceInfo.isNetworkConnected();
    }
    public String networkType(){
        return mDeviceInfo.networkType();
    }

 */
}
