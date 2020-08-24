package android.app.smdt.pine;

import android.app.AlarmManager;
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
        try {
            JSONObject json = new JSONObject(jsonStr);
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR,json.getInt("year"));
            c.set(Calendar.MONTH,json.getInt("month") -1);
            c.set(Calendar.DAY_OF_MONTH,json.getInt("day"));
            c.set(Calendar.HOUR_OF_DAY,json.getInt("hour"));
            c.set(Calendar.MINUTE,json.getInt("minute"));
            c.set(Calendar.SECOND,json.getInt("second"));
            Log.e("sv","get the time:" + json.getInt("second")+".." + json.toString());
            long when = c.getTimeInMillis();
            //   mAM.setTime(when);
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            //mHeartBeatTimer.schedule(mHeartBeatTimeTask,0,HTTP_BEATHART_PEROID);
        }

    }

    public static  void setPowerOnOff(Context context,String jsonStr){
        int[] timeOffArray = new int[5];
        int[] timeOnArray = new int[5];
        if(jsonStr == null || jsonStr.equals("")){
            return;
        }
        try {
            Intent mIntent = new Intent("android.intent.action.setpoweronoff");
            JSONObject json = new JSONObject(jsonStr);
            JSONObject powerOff = json.getJSONObject("powerOff");
            timeOffArray[0] = powerOff.getInt("year");
            timeOffArray[1] = powerOff.getInt("month");
            timeOffArray[2] = powerOff.getInt("day");
            timeOffArray[3] = powerOff.getInt("hour");
            timeOffArray[4] = powerOff.getInt("min");
            mIntent.putExtra("timeOff",timeOffArray);
            JSONObject  powerOn = json.getJSONObject("powerOn");
            timeOnArray[0] = powerOn.getInt("year");
            timeOnArray[1] = powerOn.getInt("month");
            timeOnArray[2] = powerOn.getInt("day");
            timeOnArray[3] = powerOn.getInt("hour");
            timeOnArray[4] = powerOn.getInt("min");
            mIntent.putExtra("timeOn",timeOnArray);
            boolean enable = json.getBoolean("enable");
            context.sendBroadcast(mIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            //mHeartBeatTimer.schedule(mHeartBeatTimeTask,0,HTTP_BEATHART_PEROID);
        }
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
