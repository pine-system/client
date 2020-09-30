package android.app.smdt.pine.device;

import android.app.smdt.config.MD5Util;

import java.io.File;
import java.util.LinkedList;

public class FileAttr{
    String suffix;
    String mFileName;
    long mFileLength;
    String hashCode;
    String dest;
    public FileAttr(File file,String dest){
        this.dest = dest;
        mFileName = file.getName();
        suffix = mFileName.substring(mFileName.indexOf(".") + 1);
        mFileLength = file.length();
        hashCode = MD5Util.getFileMD5Code(file);
    }
    public boolean isInList(LinkedList<FileAttr>mList){
        for(int index = 0; index < mList.size(); index ++){
            FileAttr attr = mList.get(index);
            if(attr.mFileName.equals(mFileName) &&
                attr.hashCode ==  hashCode &&
                attr.mFileLength == mFileLength){
                return true;
            }
        }
        return false;
    }
    public String getFileName(){
        return mFileName;
    }
    public String getFileLength(){
        return mFileLength + "";
    }
    public String getHashcode(){
        return hashCode;
    }
    public String getDestpath(){
        return dest;
    }
}
