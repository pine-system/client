package android.app.smdt.customui.subui.camera;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomCamera extends FrameLayout {
    private static final String TAG = CustomCamera.class.getName();
    private static final boolean Debug = true;
    private MyCamera myCamera;
   // private MyCamera2 myCamera;
    public CustomCamera(@NonNull Context context, int orient, Drawable defaultDrawable, JSONObject o) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        try {
            myCamera = new MyCamera(context,o.getInt("id"));
            addView(myCamera);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
