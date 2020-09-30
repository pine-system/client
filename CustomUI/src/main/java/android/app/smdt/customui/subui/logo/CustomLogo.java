package android.app.smdt.customui.subui.logo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class CustomLogo extends FrameLayout {
    private static final String TAG = CustomLogo.class.getName();
    private static final boolean Debug = true;

    private MyLogo myLogo;
    public CustomLogo(Context context, int orient, Drawable defaultDrawable, JSONObject o) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        myLogo = new MyLogo(context);
        addView(myLogo);
    }
}
