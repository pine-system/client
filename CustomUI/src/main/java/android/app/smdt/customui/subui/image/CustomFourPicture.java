package android.app.smdt.customui.subui.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import org.json.JSONObject;

public class CustomFourPicture extends CustomPicture {
    private static final String TAG = CustomFourPicture.class.getName();
    private static final boolean Debug = true;
    private static final int PIC_MODE = 4;
    private static final int[] PIC_FOUR_INDEX_ARRAY ={
        0,2,6,8
    };

    public CustomFourPicture(@NonNull Context context, int orient, Drawable drawableDef, JSONObject o) {
        super(context,orient,drawableDef,o);
        setMyImage(PIC_MODE,PIC_FOUR_INDEX_ARRAY);
        getLinkDrawables(o);
        if(PIC_FOUR_INDEX_ARRAY.length == mDrawableGroup.size()){
            for(int index = 0; index < PIC_FOUR_INDEX_ARRAY.length; index ++){
                insertImage(PIC_FOUR_INDEX_ARRAY[index],mDrawableGroup.get(index));
            }
        }
    }
}
