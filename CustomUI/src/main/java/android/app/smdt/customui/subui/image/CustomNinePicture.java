package android.app.smdt.customui.subui.image;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class CustomNinePicture extends CustomFullPicture{
    private static final String TAG = CustomNinePicture.class.getName();
    private static final boolean Debug = true;
    private static final int PIC_MODE = 4;
    private static final int[] PIC_FOUR_INDEX_ARRAY ={
            0,1,2,3,4,5,6,7,8
    };

    public CustomNinePicture(@NonNull Context context, int orient, Drawable drawableDef, JSONObject o) {
        super(context,orient,drawableDef,o);
        getLinkDrawables(o);
        if(PIC_FOUR_INDEX_ARRAY.length == mDrawableGroup.size()){
            for(int index = 0; index < PIC_FOUR_INDEX_ARRAY.length; index ++){
                insertImage(PIC_FOUR_INDEX_ARRAY[index],mDrawableGroup.get(index));
            }
        }
    }
}
