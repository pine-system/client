package android.app.smdt.customui.subui.video;

import android.app.smdt.customui.subui.utils.utils;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedList;
import java.util.List;

public class CustomVideo extends FrameLayout {
    private static final String TAG = CustomVideo.class.getName();
    private static final boolean Debug = true;
    private MyPlayer myPlayer;

    private LinkedList<String> getLinkFiles(JSONObject o){
        LinkedList<String> mFileList = new LinkedList<>();
        try {
            JSONArray linkArray = o.getJSONArray("link");
            for(int index = 0; index < linkArray.length(); index ++){
                JSONObject path = linkArray.getJSONObject(index);
                mFileList.addLast((utils.SDCARD_RES_PATH + path.getString("path")));
            }
            return mFileList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public CustomVideo(@NonNull Context context, Drawable defaultDrawable, JSONObject o, List<AbsoluteLayout> imageList) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        myPlayer = new MyPlayer(context,o,imageList);
        addView(myPlayer);
    }

}
