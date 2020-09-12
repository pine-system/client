package android.app.smdt.pine;

import android.app.AlarmManager;
import android.app.smdt.systemapi.SystemAPI;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class Server  {
    public static final int COMMAND_FLAG_REBOOT = 0x00000004;
    public static final int COMMAND_FLAG_SET_SYSTEM_TIME = 0x00000008;
    public static final int COMMAND_FLAG_SET_POWERONOFF = 0x00000010;
    public static final int COMMAND_FLAG_SET_VOLUME = 0x00000002;
    public static final int COMMAND_FLAG_SCREENSHOT = 0x00000014;
    public static final int COMMAND_FLAG_DOWNLOAD_RESOURCE = 0x00000018;
    public static final int COMMAND_FLAG_DOWNLOAD_FIRMWARE = 0x00000020;
    public static final int COMMAND_FLAG_UPGRADE_APK = 0x00000022;
    public static final int COMMAND_FLAG_UPDATE_VOLUME = 0x00000024;

    /*******************************************远程命令响应方法****************************************/
    public static  void reboot(Context context){
        PowerManager mPM  = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mPM.reboot("reboot");
    }
    public static void updateCalendar(Context context,String jsonStr){
        Log.e("sv",jsonStr);
        if(jsonStr == null || jsonStr.equals("")){
            return;
        }
        AlarmManager mAM = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        String[] time = jsonStr.split("-");
        if(time != null) {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR, Integer.parseInt(time[0]));
            c.set(Calendar.MONTH, Integer.parseInt(time[1]) - 1);
            c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(time[2]));
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[3]));
            c.set(Calendar.MINUTE, Integer.parseInt(time[4]));
            c.set(Calendar.SECOND, Integer.parseInt(time[5]));
           // Log.e("sv", "get the time:" + Integer.parseInt(time[0]) + ".." + Integer.parseInt(time[0]));
            long when = c.getTimeInMillis();
            mAM.setTime(when);
        }

    }

    public static  void setPowerOnOff(Context context,String jsonStr){
        int[] timeOffArray = new int[5];
        int[] timeOnArray = new int[5];
        if(jsonStr == null || jsonStr.equals("")){
            return;
        }
        String[] factors = jsonStr.split(" ");
        //1.关机解析
        String[] time = factors[0].split("-");
        for(int index = 0; index < time.length; index ++){
            timeOffArray[index] = Integer.parseInt(time[index]);
            Log.e("Network","[" + index+"]:" + timeOffArray[index]);
        }
        time = factors[1].split("-");
        for(int index = 0; index < time.length; index ++){
            timeOnArray[index] = Integer.parseInt(time[index]);
            Log.e("Network","[" + index+"]:" + timeOnArray[index]);
        }
            Log.e("Network","set power off");
            Intent mIntent = new Intent("android.intent.action.setpoweronoff");
            mIntent.putExtra("timeoff",timeOffArray);
            mIntent.putExtra("timeon",timeOnArray);
            boolean enable = true;
            mIntent.putExtra("enable",enable);
            context.sendBroadcast(mIntent);

    }
    public static void ScreenCap(Context context){
        SystemAPI api = SystemAPI.create(context);
        api.screenShot("/sdcard/Nvtek/screenCap","0.1.png",context);
    }
    public static void SetVolume(Context context,String parameter){

    }
    public static void DownloadResource(Context context){

    }
    public static void DownloadFirmware(Context context){

    }
    public static void downloadApk(Context context){

    }
    public static void downLoad(InputStream ins){
        try {
            FileOutputStream fout = null;//= new FileOutputStream();
            byte[] buf = new byte[128];
            int leng;
            while((leng = ins.read(buf)) > 0){
                fout.write(buf,0,leng);
                fout.flush();
            }
            ins.close();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            // mHeartBeatTimer.schedule(mHeartBeatTimeTask,0,HTTP_BEATHART_PEROID);
        }

    }
}
