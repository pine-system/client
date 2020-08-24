package android.app.smdt.customui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CustomDialog {
    private static final String TAG = "CustomDialog";
    private static final boolean DebugEnabled = true;
    private Activity a;
    public CustomDialog(Context context,String title,String message){
        a = (Activity)context;
        AlertDialog.Builder  mBuilder = new AlertDialog.Builder(context);
        mBuilder.setTitle(title);
        mBuilder.setMessage(message);
        mBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                a.finish();
            }
        });
        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(true);
    }
}
