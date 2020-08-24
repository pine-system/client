package android.app.smdt.customui.utils;

import android.view.Gravity;

public class utils {
    public static final int GRAVITY_VERTICAL = 1;
    public static final int GRAVITY_HORIZENTOR = 2;
    //1.获取对应的gravity
    public static int[] GravityNineTbl ={
            Gravity.TOP|Gravity.LEFT, Gravity.TOP | Gravity.CENTER, Gravity.TOP | Gravity.RIGHT,
            Gravity.CENTER|Gravity.LEFT, Gravity.CENTER | Gravity.CENTER, Gravity.CENTER | Gravity.RIGHT,
            Gravity.BOTTOM|Gravity.LEFT, Gravity.BOTTOM | Gravity.CENTER, Gravity.BOTTOM | Gravity.RIGHT,
    };
    public static int[] GravityFourTbl ={
            Gravity.TOP|Gravity.LEFT,  Gravity.TOP | Gravity.RIGHT,
            Gravity.BOTTOM|Gravity.LEFT, Gravity.BOTTOM | Gravity.RIGHT,
    };
    public static int[] GravityVerticalTwoTbl = {
            Gravity.TOP | Gravity.CENTER,
            Gravity.BOTTOM | Gravity.CENTER
    };
    public static int[] GravityHorizTwoTbl = {
            Gravity.CENTER | Gravity.LEFT,
            Gravity.CENTER | Gravity.RIGHT
    };
    public static int[] GravityOneTbl ={
            Gravity.CENTER | Gravity.CENTER
    };
    public static int getGravity(int orient,int totalNum,int index) {
        if (totalNum == 9) {
            return GravityNineTbl[index];
        } else if (totalNum == 4) {
            return GravityFourTbl[index];
        } else if (orient == GRAVITY_VERTICAL && totalNum == 2) {
            return GravityVerticalTwoTbl[index];
        } else if (orient == GRAVITY_HORIZENTOR && totalNum == 2) {
            return GravityHorizTwoTbl[index];
        }else if(totalNum ==  1){
            return GravityOneTbl[index];
        }else{
            return GravityOneTbl[0];
        }
    }
}
