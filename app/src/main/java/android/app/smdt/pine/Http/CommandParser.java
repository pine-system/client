package android.app.smdt.pine.Http;

import android.app.AlarmManager;
import android.app.smdt.systemapi.SystemAPI;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class CommandParser  {
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
    public static void updateSystemTime(Context context,JSONObject o) throws JSONException {
        if(null == o){
            return;
        }
        AlarmManager mAM = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR,o.getInt("year"));
        c.set(Calendar.MONTH, o.getInt("month") -1);
        c.set(Calendar.DAY_OF_MONTH, o.getInt("day"));
        c.set(Calendar.HOUR_OF_DAY, o.getInt("hour"));
        c.set(Calendar.MINUTE, o.getInt("min"));
        c.set(Calendar.SECOND, o.getInt("sec"));
        long when = c.getTimeInMillis();
        mAM.setTime(when);
    }

    public static  void setPowerOnOff(Context context,JSONObject o) throws JSONException {
        int[] timeOffArray = new int[5];
        int[] timeOnArray = new int[5];
        timeOffArray[0] = o.getInt("power_off_year");
        timeOffArray[1] = o.getInt("power_off_month");
        timeOffArray[2] = o.getInt("power_off_day");
        timeOffArray[3] = o.getInt("power_off_hour");
        timeOffArray[4] = o.getInt("power_off_min");

        timeOnArray[0] = o.getInt("power_on_year");
        timeOnArray[1] = o.getInt("power_on_month");
        timeOnArray[2] = o.getInt("power_on_day");
        timeOnArray[3] = o.getInt("power_on_hour");
        timeOnArray[4] = o.getInt("power_on_min");
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
    public static void SetVolume(Context context,JSONObject o) throws JSONException {
        int soundIndex = o.getInt("sound");
        AudioManager mAM = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mAM.setStreamVolume(AudioManager.STREAM_MUSIC,soundIndex,0);
    }
    public static void DownloadResource(Context context){

    }
    public static void DownloadFirmware(Context context){

    }
    public static void downloadApk(Context context){

    }

}
