package android.app.smdt.pine.resource;

import android.app.smdt.pine.device.Device;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public abstract class DUPSDCard {
    private static final String TAG = DUPSDCard.class.getName();
    protected static final String MAIN_SDCARD_RESOURCE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "nvtek";
    protected static final String[] mSubFolderSet ={
            "video",
            "image",
            "subtitle",
            "logo",
            "layout",
            "apk",
            "firmware"
    };
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private Context context;
    protected FileOutputStream mFout;
    protected InputStream mFin;
    private Object mKey;
    protected abstract  boolean update() throws IOException;

    protected boolean dupSDCard() {
        if(mFout == null || mFin == null){
            return false;
        }
        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = mFin.read(buf)) > 0){
                mFout.write(buf,0,len);
            }
            mFout.close();
            mFin.close();
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public DUPSDCard(Context context, Device device){
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        this.context = context;
        mKey = new Object();
    }

    private Runnable dupThread = new Runnable() {
        @Override
        public void run() {
            synchronized (mKey) {
                dupSDCard();
                mKey.notify();
            }
        }
    };
    public void start(){
        synchronized (mKey) {
            mHandler.post(dupThread);
            try {
                mKey.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
