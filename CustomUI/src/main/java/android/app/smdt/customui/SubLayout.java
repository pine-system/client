package android.app.smdt.customui;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.subui.camera.CustomCamera;
import android.app.smdt.customui.subui.image.CustomFourPicture;
import android.app.smdt.customui.subui.image.CustomFullPicture;
import android.app.smdt.customui.subui.image.CustomNinePicture;
import android.app.smdt.customui.subui.logo.CustomLogo;
import android.app.smdt.customui.subui.subtitle.CustomSubtitle;
import android.app.smdt.customui.subui.video.CustomVideo;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.这个布局中存在多个SurfaceView和多个imageView.同时多个
 * 2.每个surfaceView可以播放多个视频，
 * 3.每个视频可能关联多个图片。
 * 4.每一个图片需要指定对应的imageView来播放
 * 5.整体播放原则：以视频播放为准。
 */
public class SubLayout extends AbsoluteLayout {
    private static final String TAG = "multi";
    private static final boolean DebugEnabled = true;
    private static final int CUSTOM_WINDOW_ID = 5000;
    private static final int CUSTOM_WINDOW_TYPE_CAMERA = CUSTOM_WINDOW_ID;
    public static final int CUSTOM_WINDOW_TYPE_IMAGE = CUSTOM_WINDOW_TYPE_CAMERA + 1000;
    private static final int CUSTOM_WINDOW_TYPE_LOGO = CUSTOM_WINDOW_TYPE_IMAGE + 1000;
    private static final int CUSTOM_WINDOW_TYPE_SUBTITLE = CUSTOM_WINDOW_TYPE_LOGO + 1000;
    private static final int CUSTOM_WINDOW_TYPE_VIDEO = CUSTOM_WINDOW_TYPE_SUBTITLE + 1000;
    //1.布局的名称
    private int id;
    private Drawable mDrawableDef;
    //1.定义多个SurfaceView
    private List<AbsoluteLayout> mVideoLayoutList;
    private List<AbsoluteLayout> mCameraLayoutList;
    private List<AbsoluteLayout> mImageLayoutList;
    private List<AbsoluteLayout> mSubtitleLayoutList;
    private List<AbsoluteLayout> mLogoLayoutList;

    public SubLayout(Context context,JSONObject layoutJSON,int screenId, Drawable drawableDef) {
        super(context);
        //1.定义多层绝对布局
        id = screenId;
        mDrawableDef = drawableDef;
        setLayoutParams(new AbsoluteLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
        //2.初始化视频层、图片层列表
        mVideoLayoutList = new ArrayList<>();
        mImageLayoutList = new ArrayList<>();
        mCameraLayoutList = new ArrayList<>();
        mSubtitleLayoutList = new ArrayList<>();
        mLogoLayoutList = new ArrayList<>();
        //3.从json文件解析视频、图片信息
        try {
            layoutVideoFileParser(context, layoutJSON);
            //4.加入到多层布局中
            for (int index = 0; index < mVideoLayoutList.size(); index++) {
                addView(mVideoLayoutList.get(index));
            }
            for (int index = 0; index < mImageLayoutList.size(); index++) {
                addView(mImageLayoutList.get(index));
            }
            for (int index = 0; index < mCameraLayoutList.size(); index++) {
                addView(mCameraLayoutList.get(index));
            }
            for (int index = 0; index < mSubtitleLayoutList.size(); index++) {
                addView(mSubtitleLayoutList.get(index));
            }
            for (int index = 0; index < mLogoLayoutList.size(); index++) {
                addView(mLogoLayoutList.get(index));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        //1.删除视频层的列表
        if (mVideoLayoutList != null) {
            mVideoLayoutList.clear();
            mVideoLayoutList = null;
        }
        //2.删除图片层的列表
        if (mImageLayoutList != null) {
            mImageLayoutList.clear();
            mImageLayoutList = null;
        }
        //3.删除相机层的列表
        if (mCameraLayoutList != null) {
            mCameraLayoutList.clear();
            mCameraLayoutList = null;
        }
        //4.删除字幕层的列表
        if(mSubtitleLayoutList != null){
            mSubtitleLayoutList.clear();
            mSubtitleLayoutList = null;
        }
        //5.删除logo层的列表
        if(mLogoLayoutList != null){
            mLogoLayoutList.clear();
            mLogoLayoutList = null;
        }
    }
    class Frame extends AbsoluteLayout {
       private int id;
       private int left;
       private int top;
       private int width;
       private int height;
       private View mView;
       private boolean mBuiltIn;
        public Frame(Context context, JSONObject o,int typeID) {
            super(context);
            try {
                setId(o.getInt("id") + typeID);
                setLayoutParams(new LayoutParams(o.getInt("width"),o.getInt("height"),o.getInt("left"),o.getInt("top")));
                mBuiltIn = false;
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        public void insert(View mView) {
            if(!mBuiltIn) {
                mBuiltIn = true;
                this.mView = mView;
                addView(mView);
            }
        }
        public void remove() {
            if(mBuiltIn) {
                removeView(mView);
                mBuiltIn = false;
            }
        }
    }

    private AbsoluteLayout makeImageLayout(Context context,JSONObject o) {
        Frame mFrame = new Frame(context,o,CUSTOM_WINDOW_TYPE_IMAGE);
        FrameLayout mImageView = new CustomFullPicture(context,SystemConfig.getDisplayOrient(context,id),mDrawableDef,o);
        mFrame.insert(mImageView);
        return mFrame;
    }

    private AbsoluteLayout makeVideoLayout(Context context,JSONObject o) {
        Frame mFrame = new Frame(context,o,CUSTOM_WINDOW_TYPE_VIDEO);
        FrameLayout mVideoView = new CustomVideo(context,mDrawableDef,o,mImageLayoutList);
        mFrame.insert(mVideoView);
        return mFrame;
    }
    private AbsoluteLayout makeSubtitleLayout(Context context,JSONObject o) {
        Frame mFrame = new Frame(context,o,CUSTOM_WINDOW_TYPE_SUBTITLE);
        FrameLayout mSubtitle = new CustomSubtitle(context,SystemConfig.getDisplayOrient(context,id),o);
        mFrame.insert(mSubtitle);
        return mFrame;
    }
    private AbsoluteLayout makeLogoLayout(Context context,JSONObject o) {
        Frame mFrame = new Frame(context,o,CUSTOM_WINDOW_TYPE_LOGO);
        FrameLayout mLogoView = new CustomLogo(context,SystemConfig.getDisplayOrient(context,id),mDrawableDef,o);
        mFrame.insert(mLogoView);
        return mFrame;
    }
    private AbsoluteLayout makeCameraLayout(Context context,JSONObject o) {
        Frame mFrame = new Frame(context,o,CUSTOM_WINDOW_TYPE_CAMERA);
        FrameLayout mCameraView = new CustomCamera(context,SystemConfig.getDisplayOrient(context,id),mDrawableDef,o);
        mFrame.insert(mCameraView);
        return mFrame;
    }
    private void layoutVideoFileParser(Context context, JSONObject layoutJSON) throws JSONException {
        //1.解析json文件
        AbsoluteLayout tmp;
        JSONArray mLayoutArr = layoutJSON.getJSONArray("layout");
        for (int index = 0; index < mLayoutArr.length(); index++) {
            JSONObject o = mLayoutArr.getJSONObject(index);
            String type = o.getString("type");
            if (type.equals("image")) {
                tmp = makeImageLayout(context, o);
                mImageLayoutList.add(tmp);
            }else if (type.equals("video")) {
                tmp = makeVideoLayout(context,o);
                mVideoLayoutList.add(tmp);
            } else if (type.equals("subtitle")) {
                tmp = makeSubtitleLayout(context,o);
                mSubtitleLayoutList.add(tmp);
            } else if (type.equals("logo")) {
                tmp = makeLogoLayout(context,o);
                mLogoLayoutList.add(tmp);
            } else if (type.equals("camera")) {
                tmp = makeCameraLayout(context,o);
                mCameraLayoutList.add(tmp);
            } else {
                continue;
            }
        }
    }
}
