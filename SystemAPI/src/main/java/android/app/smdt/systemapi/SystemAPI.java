package android.app.smdt.systemapi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;
public class SystemAPI {
    /**
     *
     * Create a konka jar lib.
     */
        public static final String TAG = "konka";
        public static final String COMMAND_END = " \n" + " exit\n";
        public static final String COMMAND_INSTALL_PARAM = " --user 0 ";
        public static final int COMMAND_TBL_SILENT_INSTALL = 0;
        public static final int COMMAND_TBL_SCREENCAP = 1;
        public static final int COMMAND_TBL_SET_SYSTEM_TIME = 2;
        private static SystemAPI api = null;
        private Intent mIntent;
        private Context context;
        String[] COMMAND_TBL = {
                "pm install -i ",
                "screencap -p ",
        };

        private SystemAPI(Context context){
            this.context = context;
        }
        private void setCommand(String cmd) {
            Runtime runtime = Runtime.getRuntime();
            try{
                Process process = runtime.exec(cmd);
                InputStream errorInput = process.getErrorStream();
                InputStream inputStream = process.getInputStream();
                OutputStream out  = process.getOutputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String error = "";
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
                while ((line = bufferedReader.readLine()) != null) {
                    error += line;
                }
                if(result.equals("Success")){
                    Log.i("sky", "install: Success");
                }else{
                    Log.i("sky", "install: error"+error);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * create
         * create a instance of Konka
         */
        public static SystemAPI create(Context context){
            if(api == null){
                api  = new SystemAPI(context);
            }
            return api;
        }
        private int getStringMillion(String time){
            String[] tmp = time.split(":");
            int hour,min;
            hour = Integer.parseInt(tmp[0]);
            min = Integer.parseInt(tmp[1]);
            return hour * 60 + min;
        }
        private long getSystemMillion(int year,int month,int day,int hour,int min){
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,month -1);
            c.set(Calendar.DAY_OF_MONTH,day);
            c.set(Calendar.HOUR_OF_DAY,hour);
            c.set(Calendar.MINUTE,min);
            return c.getTimeInMillis();
        }
		/**
		 * parameter: time the setting time, format:    hour:min
		 * parameter: isNextDay. power on time must be later than the power off time.so defined the isNestDay.
		*/
        private int[] str2Arr(String time,boolean isNextDay){
            int[] times = new int[5];
            int year,month,day,hour,min;
            if(time == null || !time.contains(":")){
                return null;
            }
            String[] tmp = time.split(":");
            if(tmp == null){
                return null;
            }
            hour = Integer.parseInt(tmp[0]);
            min = Integer.parseInt(tmp[1]);
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
            times[0] = year;
            times[1] = month + 1;
			if(isNextDay){
				 day += 1;
			}
			times[2] = day;
            times[3] = hour;
            times[4] = min;
            return times;
        }
        /**
         * set system power on or off as time.
         * @param offTime
         * @param onTime
         * @param enable
         */
        public void setOnOffTimer(String offTime, String onTime, boolean enable) throws IOException {
            Log.e("api","enter the :" + offTime + ":" + onTime);
            int[] timeon = new int [5];
            int[] timeoff = new int[5];
            if(offTime == null || onTime == null){
                throw new IOException("offTime or onTime is null");
            }

            if(getStringMillion(offTime) >= getStringMillion(onTime)){
                timeon = str2Arr(onTime,true);
            }else{
                timeon = str2Arr(onTime,false);
            }
			Log.e("api","enter the :" + offTime + ":" + onTime);
            timeoff = str2Arr(offTime,false);
            mIntent = new Intent("android.intent.action.gz.setpoweronoff");
            mIntent.putExtra("timeon", timeon);
            mIntent.putExtra("timeoff", timeoff);
            mIntent.putExtra("enable",enable); //使能开关机功能，　设为false,则为关闭，true为打开
            context.sendBroadcast(mIntent);
        }

        /**
         *
         * @param path
         * @param name
         * @param context
         */
        public void screenShot (String path, String name, Context context){
            String cmd = COMMAND_TBL[COMMAND_TBL_SCREENCAP] + path + name;
            setCommand(cmd);
        }

        /**
         * 需要添加权限:<uses-permission android:name="android.permission.INSTALL_PACKAGES"
         * @param apkPath
         * @param context
         */
        public void silentInstall (String apkPath, Context context) throws FileNotFoundException{
            String packageName = ((Activity)context).getPackageName();
            Log.e("api","packageName:" + packageName + ",apkPath:" + apkPath);
            String cmd = COMMAND_TBL[COMMAND_TBL_SILENT_INSTALL] + packageName + COMMAND_INSTALL_PARAM + apkPath + COMMAND_END;
            setCommand(cmd);

        }

        /**
         *
         * @param context
         * @param year
         * @param month
         * @param day
         * @param hour
         * @param minute
         */
        public void setSystemTime(Context context, int year, int month, int day, int hour,int minute){
            Intent intent = new Intent("android.intent.action.setTime");
            intent.putExtra("year",year);
            intent.putExtra("month",month);
            intent.putExtra("day",day);
            intent.putExtra("hour",hour);
            intent.putExtra("min",minute);
            context.sendBroadcast(intent);
        }

        /**
         * //val =0,关闭watchdog，系统开始喂狗
         * // val = 1,　打开watchdog，系统停止喂狗，客户apk要自己喂狗
         * // val = 2, 清除watchdog（喂狗操作）, 时间间隔，系统不喂狗的情况下，客户的apk两次喂狗的时间间隔不能超过1分钟，否则看门狗会复位主控芯片。
         * @param enable
         */
        public void setWatchDog(boolean enable){
            mIntent = new Intent("android.intent.action.watchdog");
            if(enable){
                mIntent.putExtra("val",1 );
            }else{
                mIntent.putExtra("val",0 );
            }
            context.sendBroadcast(mIntent);
        }

    /**
     *
     */
        public void feedWatchDog (){
            Intent intent = new Intent("android.intent.action.watchdog");
            intent.putExtra("val",2 );
            this.context.sendBroadcast(intent);
        }

        public void displayDeviceSwitch(boolean enabled){
            String switchAction;
            if(enabled){
                switchAction ="android.action.adtv.sleep";
            }else{
                switchAction = "android.action.adtv.wakeup";
            }
            Log.e("api","action:" + switchAction);
            Intent mIntent = new Intent(switchAction);
            context.sendBroadcast(mIntent);
        }
        public void setBrightness(int brightness){
            Settings.System.putInt(context.getContentResolver(),"screeen_brightness",brightness);
        }
        public int getBrightness() throws Settings.SettingNotFoundException {
            return Settings.System.getInt(context.getContentResolver(),"screen_brightness");
        }
        public void setVolume(int type,int vol){
            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(type,vol,0);
        }
        public int[] getVolume(){
            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            int[] rs = new int[]{0,0};
            rs[1] = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            rs[0] = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
            return rs;
        }
    }


