package android.app.smdt.customui;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

public class DifferentDisplay extends Presentation{

    private int layoutId;
    public DifferentDisplay(Context outerContext, Display display,int layoutId) {
        super(outerContext, display);
        this.layoutId = layoutId;
    }

    public DifferentDisplay(Context outerContext, Display display, int theme,int layoutId) {
        super(outerContext, display, theme);
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);
    }
}
