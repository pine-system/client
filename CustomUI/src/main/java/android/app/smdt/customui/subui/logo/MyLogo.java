package android.app.smdt.customui.subui.logo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class MyLogo extends androidx.appcompat.widget.AppCompatImageView implements Runnable{
    private static final String TAG =  MyLogo.class.getName();
    private static final boolean Debug = true;
    private Handler mHandler;
    private LinkedList<Drawable> mLogoList;
    public MyLogo(Context context, JSONObject logo) {
        super(context);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.CENTER));
        mLogoList = new LinkedList<>();
        try {
            JSONArray arr = logo.getJSONArray("link");
            for(int index = 0; index < arr.length(); index++){
                JSONObject o = arr.getJSONObject(index);
                String logoPath = o.getString("path");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mHandler.post(this);
    }

    @Override
    public void run() {

    }
}
