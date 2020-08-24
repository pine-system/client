package android.app.smdt.customui.text;

import android.app.smdt.config.SystemConfig;
import android.app.smdt.customui.CustomLayout;
import android.content.Context;
import android.util.DisplayMetrics;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Title  {
    private static final String TAG = Title.class.getSimpleName();
    private static final boolean DebugEnabled = false;

    public static final int TITLE_BASE_ID = 10000;
    public static final int MAIN_TITLE = 0;
    public static final int DATE_TITLE = 1;
    public static final int LOGO_TITLE = 2;
    public static final int SUBTITLE_TITLE = 3;

    private static Title mTitle = null;
    private int mTitleCounter = 0;

    //保留新建的title类，用于后期的增删改查用
    private LinkedList<CustomText> mTitleList;

    private  Title(){
        mTitleCounter = 0;
        mTitleList = new LinkedList<>();
    }

    public static Title makeTitleInstance(){
        if(mTitle == null){
            mTitle = new Title();
        }
        return mTitle;
    }
    //1.增
    public CustomText insertCustomText(Context context,CustomLayout parent,String title,int type){
        int id = TITLE_BASE_ID + type * 1000 + mTitleCounter;
        CustomText ct = new CustomText();
        ct.insertTitle(context,parent,title,type,id);
        mTitleList.addLast(ct);
        mTitleCounter ++;
        return ct;
    }
    //2.删除
    public void deleteCustomText(CustomText ct){
        if(ct == null || mTitleList.isEmpty()){
            return;
        }
        ct.onDestory();
        mTitleList.remove(ct);
    }
    //3.修改
    public void updateCustomText(CustomText ct,Map<String,Object>map){
        if(ct == null || map.isEmpty()){
            return;
        }
        Iterator<String>keySetIter = map.keySet().iterator();
        while(keySetIter.hasNext()){
            String key = keySetIter.next();
            if(key.equals("width-ratio")){
                ct.updateWidth((Float)map.get(key));
            }else if(key.equals("height-ratio")){
                ct.updateHeight((Float)map.get(key));
            }else if(key.equals("gravity")){
                ct.updateGravity((Integer)map.get(key));
            }else if(key.equals("font-color")){
                ct.updateFontColor((Integer)map.get(key));
            }else if(key.equals("back-color")){
                ct.updateBackground((Integer)map.get(key));
            }else if(key.equals("title")){
                ct.updateTitle((String)map.get(key));
            }else if(key.equals("font-size")){
                ct.updateFontSize((Float)map.get(key));
            }else if(key.equals("margin-top")){
                ct.updateMarginTop((Integer)map.get(key));
            }
        }
        SystemConfig.D(TAG,DebugEnabled,"Update complete.");
    }
    //4.查询
    public CustomText queryCustomTextByIndex(int index){
        if(index < 0 || index > mTitleList.size()){
            return null;
        }
        return mTitleList.get(index);
    }
    //5.销毁
    public void onDestory(){
        for(int index = 0; index < mTitleList.size(); index ++){
            CustomText ct = mTitleList.pop();
            ct.onDestory();
        }
        mTitleList = null;
        mTitle = null;
    }

    public class CustomText{
        private static final float MAIN_LAND_WIDTH_RATION = 0.7f;
        private static final float MAIN_LAND_HEIGHT_RATION = 0.01f;
        private static final float MAIN_PORT_WIDTH_RATION = 0.7f;
        private static final float MAIN_PORT_HEIGHT_RATION = 0.02f;

        private CustomTextView subTitle;
        private static final int STEP_DEFAULT = 5;
        private int xPos;
        //获取系统屏幕的信息
        private DisplayMetrics metrics;
        private int orient;
        private DisplayMetrics getMainTitleDisplayMetrics(Context context) {
            metrics = SystemConfig.getDisplayMetricsById(context,0);
            orient = SystemConfig.getDisplayOrient(context,0);
            if(orient > 0){
                metrics.widthPixels = (int)(metrics.widthPixels * MAIN_PORT_WIDTH_RATION);
                metrics.heightPixels = (int)(metrics.heightPixels * MAIN_PORT_HEIGHT_RATION);
            }else{
                metrics.widthPixels = (int)(metrics.widthPixels * MAIN_LAND_WIDTH_RATION);
                metrics.heightPixels = (int)(metrics.heightPixels * MAIN_LAND_HEIGHT_RATION);
            }
            return metrics;
        }
        private Timer mTimer;
        private TimerTask  timeTask = new TimerTask() {
            @Override
            public void run() {
                String date =  new SimpleDateFormat("HH:mm:ss").format(new Date());
                subTitle.setTvTitile(date);
            }
        };

        //1.增加
        public CustomTextView insertTitle(Context context,CustomLayout parent,String title,int type,int id){
            getMainTitleDisplayMetrics(context);
            subTitle = new CustomTextView(context,parent,id);
            subTitle.setTvTitile(title);
            subTitle.tvInsert();
            switch(type){
                case DATE_TITLE:
                    mTimer = new Timer();
                    mTimer.schedule(timeTask,0,500);
                    break;
                case SUBTITLE_TITLE:
                    subTitle.setTvEllipsize(true);
                    break;
            }
            return subTitle;
        }
        //2.删除
        public void onDestory(){
            if(subTitle == null){
                return;
            }
            if(null != mTimer){
                mTimer.cancel();
            }
            subTitle.tvRemove();
        }
        //3.修改
        public void updateTitle(String txt){
            if(subTitle == null){
                return;
            }
            subTitle.setTvTitile(txt);
        }
        public void updateWidth(float widthRatio){
            if(subTitle == null){
                return;
            }
            subTitle.setTvWidth((int)(metrics.widthPixels * widthRatio));
        }
        public void updateHeight(float heightRatio){
            if(subTitle == null){
                return;
            }
            subTitle.setTvHeight((int)(metrics.heightPixels * heightRatio));
        }
        public void updateGravity(int gravity){
            if(subTitle == null){
                return;
            }
            subTitle.setTvGravity(gravity);
        }
        public void updateBackground(int color){
            if(subTitle == null){
                return;
            }
            subTitle.setTvBackgroundColor(color);
        }
        public void updateFontColor(int color){
            if(subTitle == null){
                return;
            }
            subTitle.setTvFontColor(color);
        }
        public void updateFontSize(float fontSize){
            if(subTitle == null){
                return;
            }
            subTitle.setFontSize(fontSize);
        }
        public void updateMarginTop(int marginTop){
            if(subTitle == null){
                return;
            }
            subTitle.setMarginTop(marginTop);
        }
    }
}
