package android.app.smdt.config;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static java.net.NetworkInterface.getNetworkInterfaces;

public class SystemConfig {

    public static void D(String tag,Boolean enable,String... params){
        if(!enable){
            return;
        }
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < params.length; i++){
            sb.append(params[i]);
            sb.append("\n");
        }
        sb.append(tag + "info end.");
        sb.append("\n");
        Log.e(tag,sb.toString());
    }

    public static DisplayMetrics getDisplayMetricsById(Context context, int id) {
        DisplayMetrics metrics = new DisplayMetrics();
        DisplayManager DM = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        int displayCounts  = DM.getDisplays().length;
        if(id >=  displayCounts || id < 0){
            id = 0;
        }
        DM.getDisplays()[id].getRealMetrics(metrics);
        return metrics;
    }
    public static int getDisplayOrient(Context context,int id) {
        DisplayMetrics metrics = getDisplayMetricsById(context,id);
        if(metrics.widthPixels > metrics.heightPixels){
            return 0;
        }else{
            return 1;
        }

    }

    public static String getSystemSerial(){
        return Build.SERIAL;
    }
    public static boolean isNetworkConnected(Context context){
        ConnectivityManager CM = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  nwInfo  = CM.getActiveNetworkInfo();
        if(nwInfo != null){
            return nwInfo.isAvailable();
        }
        return false;
    }


    public static String getActiveNetworkType(Context context){
        ConnectivityManager CM = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  nwInfo  = CM.getActiveNetworkInfo();
        if(nwInfo != null && nwInfo.isAvailable()){
            return nwInfo.getTypeName();
        }
        return null;
    }
    public static String getActiveNetworkIP() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = ni.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }catch (SocketException e){
            e.printStackTrace();
        }
       return null;
    }
    public static String getActiveNetworkMACFromIP(){
        String mac_s= "";
        try {
            byte[] mac;
            NetworkInterface ne=NetworkInterface.getByInetAddress(InetAddress.getByName(getActiveNetworkIP()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
        } catch (Exception e) {
                e.printStackTrace();
        }

            return mac_s;
    }

    public static  String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs = hs.append("0").append(stmp);
            else {
                hs = hs.append(stmp);
            }
            if(n != len -1){
                hs.append(":");
            }
        }
        return String.valueOf(hs);
    }

    public static void fullScreen(Context context){
        View mDecorView = ((Activity)context).getWindow().getDecorView();
        int uiSystemFlag = mDecorView.getSystemUiVisibility();
        uiSystemFlag |=  View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiSystemFlag |=  View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiSystemFlag |=  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        mDecorView.setSystemUiVisibility(uiSystemFlag);
    }
    public static void hideActionBar(Context context){
        ActionBar actionBar = ((Activity)context).getActionBar();
        actionBar.hide();
    }
    public Rect getTextRect(String title, float fontSize){
        Paint mPaint=new TextPaint();
        mPaint.setTextSize(fontSize);
        mPaint.setAntiAlias(true);
        Rect mRect=new Rect();
        mPaint.getTextBounds(title,0,title.length(),mRect);
        return mRect;
    }
    public static boolean RequestPermission(Context context,String[] permissions,int requestCode){
        if(ContextCompat.checkSelfPermission(context,permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity)context,permissions,requestCode);
            return false;
        }else{
            return true;
        }
    }

    public static File[] listFilesAsPath(String path){
        if(path == null || path.trim().equals("")){
            return null;
        }
        File mFile = new File(path);
        if(!mFile.exists() || !mFile.isDirectory()){
            return null;
        }
        File[]  files = mFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String fileName = pathname.getName().toLowerCase();
                if(fileName.contains(".jpg") || fileName.contains(".jpeg") || fileName.contains(".png") || fileName.contains(".mp4") || fileName.contains(".mov")){
                    return true;
                }else {
                    return false;
                }
            }
        });
        if(files == null || files.length <= 0){
            return null;
        }
        return files;
    }
    public static FrameLayout getRoot(Context context){
        return ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
    }

    /**
     * 获取系统的property参数
     * @param name
     * @param def
     * @return
     */
    public static String PropertiesGet(String name,String def){
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            SystemProperties.newInstance();
            Method method = SystemProperties.getMethod("get",String.class,String.class);
            return (String)method.invoke(SystemProperties,name,def);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static int PropertiesGetInt(String name,int def){
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            SystemProperties.newInstance();
            Method method = SystemProperties.getMethod("getInt",String.class,Integer.class);
            return (int)method.invoke(SystemProperties,name,def);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static boolean PropertiesGetBoolean(String name,boolean def){
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            SystemProperties.newInstance();
            Method method = SystemProperties.getMethod("getInt",String.class,Boolean.class);
            return (boolean)method.invoke(SystemProperties,name,def);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
}
