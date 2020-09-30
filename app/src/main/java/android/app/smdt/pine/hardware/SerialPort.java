package android.app.smdt.pine.hardware;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    private static final String TAG =  "SerialPort";
    private static final boolean Debug = true;
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    static{
        System.loadLibrary("serial_port");
    }

    public SerialPort(File device, int baudrate, int flags) throws SecurityException,IOException{
        if(!device.canRead() || !device.canWrite()){
            Process su;
            try {
                su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if((su.waitFor() != 0) || !device.canRead() || !device.canWrite()){
                    throw new SecurityException();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        mFd = open(device.getAbsolutePath(),baudrate,flags);
        if(mFd == null){
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public InputStream getInputStream(){
        return mFileInputStream;
    }
    public OutputStream getOutputStream(){
        return mFileOutputStream;
    }

    private native static FileDescriptor open(String path,int baudrate,int flags);
    private native static void close();
}
