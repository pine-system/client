package android.app.smdt.customui.subui.image;

import android.app.smdt.customui.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.LinkedList;

public class MyImage extends  androidx.appcompat.widget.AppCompatImageView implements Runnable{
    private static final String TAG = MyImage.class.getName();
    private static final boolean Debug = true;
    private static final int IMAGE_SWITCH_TIMEOUT = 5 * 1000;
    private static final boolean IMAGE_SWITCH_ENABLE = true;

    private Context context;
    private LinkedList<Drawable> mImageList;
    private Handler mHandler;
    private int mImageSwitchTimeout;
    private boolean mImageSwitchEnabled;
    private boolean mNoResExist = false;
    private Drawable mDefaultDrawable;
    public MyImage(@NonNull Context context,Drawable defaultDrawable,int delay) {
        super(context);
        this.context = context;
        mDefaultDrawable = defaultDrawable;
        mImageList = new LinkedList<>();
        mHandler = new Handler(Looper.getMainLooper());
        mImageSwitchTimeout = (delay == 0)?IMAGE_SWITCH_TIMEOUT : delay;
        mImageSwitchEnabled = IMAGE_SWITCH_ENABLE;
        mHandler.post(this);
    }
    public void insert(LinkedList<Drawable> drawables){
        mImageList.clear();
       for(int index = 0; index < drawables.size(); index ++){
           mImageList.addLast(drawables.get(index));
       }
    }
    public void insertAppend(LinkedList<Drawable> drawables){
        for(int index = 0; index < drawables.size(); index ++){
            mImageList.addLast(drawables.get(index));
        }
    }
    @Override
    public void run() {
        if(mImageSwitchEnabled){
            if(!mImageList.isEmpty()){
                mNoResExist = false;
                Drawable d = mImageList.pop();
                setImageDrawable(d);
                mImageList.addLast(d);
            }else if(!mNoResExist){
                setImageDrawable(mDefaultDrawable);
                mNoResExist = true;
            }
        }
        mHandler.postDelayed(this,mImageSwitchTimeout);
    }
}
