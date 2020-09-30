package android.app.smdt.customui.subui.image;

import android.app.smdt.customui.subui.utils.utils;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static android.app.smdt.customui.subui.utils.utils.Bitmap2Drawable;

public abstract  class CustomPicture extends FrameLayout {
    protected static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private static final int MAX_PIC_NUM_DEFAULT = 9;
    private int mLayoutWidth;
    private int mLayoutHeight;
    private MyImage[] myImageArray;
    private int delay;
    protected Map<Integer, LinkedList<Drawable>> mDrawableGroup;
    public CustomPicture(@NonNull Context context, int orient, Drawable defaultDrawable, JSONObject o) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if(o.has("delay")){
            try {
                delay = o.getInt("delay");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mDrawableGroup = new HashMap<>();
        myImageArray = new MyImage[MAX_PIC_NUM_DEFAULT];
        for(int index = 0; index < myImageArray.length; index ++){
            myImageArray[index] =  new MyImage(context,defaultDrawable,delay);
            myImageArray[index] .setLayoutParams(new LayoutParams(0,0, utils.getGravity(orient,MAX_PIC_NUM_DEFAULT,index)));
            addView(myImageArray[index]);
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        mLayoutHeight =  MeasureSpec.getSize(heightMeasureSpec);
    }

    protected void setMyImage(int mode,int[] posTbl){
        int width = mLayoutWidth / mode;
        int height = mLayoutHeight / mode;
        for(int index = 0; index < posTbl.length; index ++){
            int pos =  posTbl[index];
            MyImage myImage = myImageArray[pos];
            LayoutParams mParam  = (LayoutParams) myImage.getLayoutParams();
            mParam.width = width;
            mParam.height = height;
            myImage.setLayoutParams(mParam);
        }
    }
    protected void insertImage(int index, LinkedList<Drawable> drawables){
        myImageArray[index].insert(drawables);
    }
    protected void getLinkDrawables(JSONObject o){
        LinkedList<Drawable>mDrawables = new LinkedList<>();
        try {
            JSONArray linksArray = o.getJSONArray("links");
            //1.links列表
            for(int index = 0; index < linksArray.length(); index ++){
                //2.link列表
                JSONArray linkArray = linksArray.getJSONArray(index);
                for(int link = 0; link < linkArray.length(); link++){
                    JSONObject path = linkArray.getJSONObject(index);
                    mDrawables.addLast(Bitmap2Drawable(SDCARD_PATH + path.getString("path")));
                }
                mDrawableGroup.put(index,mDrawables);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
