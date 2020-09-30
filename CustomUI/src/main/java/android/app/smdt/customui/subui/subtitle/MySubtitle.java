package android.app.smdt.customui.subui.subtitle;

import android.app.smdt.customui.subui.utils.utils;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.LinkedList;

public class MySubtitle extends androidx.appcompat.widget.AppCompatTextView {
    private static final String TAG = MySubtitle.class.getName();
    private static final boolean Debug = true;

    private LinkedList<String>mSubtitleList;
    public MySubtitle(Context context, JSONObject o) {
        super(context);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mSubtitleList = utils.JSONSubtitleParser(o);
    }
}
