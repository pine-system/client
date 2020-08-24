package android.app.smdt.customui.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class CustomToast {
    public static void Toast(final Context context, final String msg){
                Toast toast = Toast.makeText(context,msg,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
    }
}
