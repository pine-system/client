package android.app.smdt.pine;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.systemapi.SystemAPI;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root_pref, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 将所有的设置全部定义到这里
     */
    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
       private static final String TAG = SettingsFragment.class.getSimpleName();
       private static final boolean DebugEnabled = true;

        private static final String KEY_WATCHDOG = "watchdog";
        private static final String KEY_REBOOT = "power_reboot";
        private static final String KEY_POWEROFF = "power_off";
        private static final String KEY_INSTALL = "install";
        private static final String KEY_SERIAL = "serial";
        private static final String KEY_LCDOff = "lcd_off";
        private static final String KEY_TIME_ZONE = "time_zone";
        private static final String KEY_SCREENSHOT = "screenshop";

        private static final String KEY_POWER_SWITCH_OFF = "power_switch_off";
        private static final String KEY_POWER_SWITCH_ON = "power_switch_on";

        private SwitchPreferenceCompat mWatchdogPreference;
        private Preference mRebootPreference;
        private Preference mPowerOffPreference;
        private Preference mInstallAppPreference;
        private Preference mSerialPreference;
        private Preference mLCDOffPreference;
        private ListPreference mTimeZonePreference;
        private Preference mScreenshotPreference;
        private EditTextPreference  mPowerSwitchOffPreference;
        private EditTextPreference  mPowerSwitchOnPreference;
        private SystemAPI mSystemAPI;
        private Handler mHandler;
        private HandlerThread mHandlerThread;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SystemConfig.fullScreen(getActivity());
            mWatchdogPreference = (SwitchPreferenceCompat)findPreference(KEY_WATCHDOG);
           // mWatchdogPreference.setOnPreferenceClickListener(this);
            mWatchdogPreference.setOnPreferenceChangeListener(this);
            mRebootPreference = findPreference(KEY_REBOOT);
            mRebootPreference.setOnPreferenceClickListener(this);
            mPowerOffPreference = findPreference(KEY_POWEROFF);
            mPowerOffPreference.setOnPreferenceClickListener(this);
            mInstallAppPreference = findPreference(KEY_INSTALL);
            mInstallAppPreference.setOnPreferenceClickListener(this);
            mSerialPreference = findPreference(KEY_SERIAL);
            mSerialPreference.setOnPreferenceClickListener(this);
            mLCDOffPreference = findPreference(KEY_LCDOff);
            mLCDOffPreference.setOnPreferenceClickListener(this);
            mTimeZonePreference = findPreference(KEY_TIME_ZONE);
            mTimeZonePreference.setOnPreferenceChangeListener(this);
            mTimeZonePreference.setOnPreferenceClickListener(this);
            mScreenshotPreference = findPreference(KEY_SCREENSHOT);
            mScreenshotPreference.setOnPreferenceClickListener(this);

            mPowerSwitchOffPreference = findPreference(KEY_POWER_SWITCH_OFF);
            mPowerSwitchOffPreference.setOnPreferenceChangeListener(this);
            mPowerSwitchOnPreference = findPreference(KEY_POWER_SWITCH_ON);
            mPowerSwitchOnPreference.setOnPreferenceChangeListener(this);

            mSystemAPI = SystemAPI.create(getActivity());

            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
           String key = preference.getKey();
           if(key.equals(KEY_REBOOT)){
               reboot();
           }else if(key.equals(KEY_POWEROFF)){
               powerOff();
           }else if(key.equals(KEY_INSTALL)){
               installApp();
           }else if(key.equals(KEY_SERIAL)){
               showSerial();
           }else if(key.equals(KEY_LCDOff)){
               DisplayOff();
           }else if(key.equals(KEY_SCREENSHOT)){
               screenShot();
           }
            return false;
        }
        private Runnable mWatchdogThread = new Runnable() {
            @Override
            public void run() {
                mSystemAPI.feedWatchDog();
                mHandler.postDelayed(this,1000);
            }
        };
        String mPowerOn,mPowerOff;
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if(key.equals(KEY_WATCHDOG)){
                boolean isSystemWatchdog = (Boolean)newValue;
                setWatchDog(isSystemWatchdog);
            }else if(key.equals(KEY_TIME_ZONE)){
                String zone = (String)newValue;
                setTimeZone(zone);
            }else if(key.equals(KEY_POWER_SWITCH_ON)){
                mPowerOn = (String)newValue;
                preference.setSummary(mPowerOn);
                Log.e("api","powerOn:" + mPowerOn);
                try {
                    if(mPowerOff != null || mPowerOn != null) {
                        mSystemAPI.setOnOffTimer(mPowerOff, mPowerOn, true);
                    }else{
                        Toast.makeText(getActivity(),"power on and off time mustn't null",Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(key.equals(KEY_POWER_SWITCH_OFF)){
                mPowerOff = (String)newValue;
                Log.e("api","powerOff:" + mPowerOff);
                preference.setSummary(mPowerOff);
            }
            return true;
        }

        private void setWatchDog(boolean watchDogEnabled){
            mSystemAPI.setWatchDog(watchDogEnabled);
            if(watchDogEnabled) {
                mHandler.postDelayed(mWatchdogThread,1000);
            }else{
                mHandler.removeCallbacks(mWatchdogThread);
            }
        }
        private void setTimeZone(String zone){
            Calendar c = Calendar.getInstance();
            TimeZone tz  = TimeZone.getTimeZone(zone);
            Log.e(TAG,"time zone:" + tz.toString());
            c.setTimeZone(tz);
        }
        private void reboot(){
            PowerManager mPM = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
            mPM.reboot("reboot");

        }
        private void powerOff(){

        }
        private void installApp(){
            try {
                mSystemAPI.silentInstall("/sdcard/1.apk",getActivity());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        private void showSerial(){

        }
        private void DisplayOff(){
            mSystemAPI.displayDeviceSwitch(true);
        }
        private void screenShot(){
            mSystemAPI.screenShot("/sdcard/","23.png",getActivity());
        }
    }

}