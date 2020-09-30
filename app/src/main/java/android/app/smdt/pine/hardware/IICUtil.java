package android.app.smdt.pine.hardware;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IICUtil {
    private static final String TAG = "i2c";
    private static final boolean Debug = true;
    private FileDescriptor mFd;
    private FileInputStream mInput;
    private FileOutputStream mOut;

    /**
     *
     * @param i2cDev i2c device number
     * @param i2cAddr i2c device address
     */
    public IICUtil(File i2cDev, int i2cAddr) throws IOException, InterruptedException {
        if(!i2cDev.canWrite() || !i2cDev.canRead()){
            Process su;
            su = Runtime.getRuntime().exec("system/xbin/su");
            String cmd = "chmod 666 " + i2cDev.getAbsolutePath() + "\n" + "exit\n";
            su.getOutputStream().write(cmd.getBytes());
            if(su.waitFor() != 0 || !i2cDev.canWrite() || !i2cDev.canRead()){
                throw new SecurityException();
            }
        }
        mFd = open(i2cDev.getAbsolutePath());
        if(mFd ==  null){
            throw new SecurityException();
        }
        mInput = new FileInputStream(mFd);
        mOut = new FileOutputStream(mFd);
    }
    public InputStream getFileInputStream(){
        return mInput;
    }
    public OutputStream getFileOutputStream(){
        return mOut;
    }
    public native static FileDescriptor open(String i2cDev);
    public native static void close();
}
