package android.app.smdt.customui.subui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.Gravity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.LinkedList;

public class utils {
    public static final String SDCARD_RES_PATH = Environment.getExternalStorageDirectory() + File.separator + "nvtek" + File.separator;
    public static final int GRAVITY_VERTICAL = 1;
    public static final int GRAVITY_HORIZENTOR = 2;
    //1.获取对应的gravity
    public static int[] GravityNineTbl ={
            Gravity.TOP|Gravity.LEFT, Gravity.TOP | Gravity.CENTER, Gravity.TOP | Gravity.RIGHT,
            Gravity.CENTER|Gravity.LEFT, Gravity.CENTER | Gravity.CENTER, Gravity.CENTER | Gravity.RIGHT,
            Gravity.BOTTOM|Gravity.LEFT, Gravity.BOTTOM | Gravity.CENTER, Gravity.BOTTOM | Gravity.RIGHT,
    };
    public static int[] GravityFourTbl ={
            Gravity.TOP|Gravity.LEFT,  Gravity.TOP | Gravity.RIGHT,
            Gravity.BOTTOM|Gravity.LEFT, Gravity.BOTTOM | Gravity.RIGHT,
    };
    public static int[] GravityVerticalTwoTbl = {
            Gravity.TOP | Gravity.CENTER,
            Gravity.BOTTOM | Gravity.CENTER
    };
    public static int[] GravityHorizTwoTbl = {
            Gravity.CENTER | Gravity.LEFT,
            Gravity.CENTER | Gravity.RIGHT
    };
    public static int[] GravityOneTbl ={
            Gravity.CENTER | Gravity.CENTER
    };
    public static int getGravity(int orient,int totalNum,int index) {
        if (totalNum == 9) {
            return GravityNineTbl[index];
        } else if (totalNum == 4) {
            return GravityFourTbl[index];
        } else if (orient == GRAVITY_VERTICAL && totalNum == 2) {
            return GravityVerticalTwoTbl[index];
        } else if (orient == GRAVITY_HORIZENTOR && totalNum == 2) {
            return GravityHorizTwoTbl[index];
        }else if(totalNum ==  1){
            return GravityOneTbl[index];
        }else{
            return GravityOneTbl[0];
        }
    }

    private static BitmapFactory.Options getBitmapOption(int inSampeSize){
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampeSize;
        return options;
    }
    public static Drawable Bitmap2Drawable(String bmpPath){
        if(null == bmpPath){
            return null;
        }
        File bmpFile = new File(bmpPath);
        if(!bmpFile.exists() || bmpFile.isDirectory()){
            return null;
        }
        Bitmap mBitmap = BitmapFactory.decodeFile(bmpPath,getBitmapOption(1));
        return new BitmapDrawable(mBitmap);
    }
    public static LinkedList<Drawable> JSONImageParser(JSONObject o){
        LinkedList<Drawable> logoList;
        JSONArray arr;
        if(o ==  null || !o.has("link")){
            return null;
        }
        logoList = new LinkedList<>();
        try {
            arr = o.getJSONArray("link");
            for(int index = 0; index < arr.length(); index ++){
                JSONObject path = arr.getJSONObject(index);
                Drawable logoDraw = Bitmap2Drawable(SDCARD_RES_PATH + path.getString("path"));
                logoList.addLast(logoDraw);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            return logoList;
        }
    }
    //解析video的 json
    public static LinkedList<String> JSONSubtitleParser(JSONObject o){
        LinkedList<String> subtitleList;
        JSONArray arr;
        if(o == null || !o.has("link")){
            return null;
        }
        subtitleList = new LinkedList<>();
        try {
            arr = o.getJSONArray("link");
            for(int index = 0; index < arr.length(); index ++){
                JSONObject path = arr.getJSONObject(index);
                String mFilePath = SDCARD_RES_PATH + path.getString("path");
                subtitleList.addLast(mFilePath);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }finally{
            return subtitleList;
        }
    }

}
