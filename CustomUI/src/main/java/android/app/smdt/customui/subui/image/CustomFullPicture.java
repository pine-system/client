package android.app.smdt.customui.subui.image;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class CustomFullPicture extends CustomPicture {
    private static final String TAG = CustomFullPicture.class.getName();
    private static final boolean Debug = true;

    private static final int PIC_MODE = 1;
    private static final int[] PIC_FOUR_INDEX_ARRAY ={
            4
    };

    public CustomFullPicture(@NonNull Context context, int orient, Drawable defaultDrawable, JSONObject o) {
        super(context, orient, defaultDrawable,o);
        setMyImage(PIC_MODE,PIC_FOUR_INDEX_ARRAY);
        getLinkDrawables(o);
        if(PIC_FOUR_INDEX_ARRAY.length == mDrawableGroup.size()){
            for(int index = 0; index < PIC_FOUR_INDEX_ARRAY.length; index ++){
                insertImage(PIC_FOUR_INDEX_ARRAY[index],mDrawableGroup.get(index));
            }
        }
    }
    public void Insert(LinkedList<Drawable>mImageList){
        for(int index = 0; index < PIC_FOUR_INDEX_ARRAY.length; index ++){
            insertImage(PIC_FOUR_INDEX_ARRAY[index],mImageList);
        }
    }
}
