package android.app.smdt.customui.subui.subtitle;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class CustomSubtitle extends FrameLayout {
    private static final String TAG = CustomSubtitle.class.getName();
    private static final boolean Debug = true;

    private MySubtitle mySubtitle;
    public CustomSubtitle(@NonNull Context context, int orient, JSONObject o) {
        super(context);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mySubtitle = new MySubtitle(context);
        addView(mySubtitle);
    }
}
