package com.mom.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Qandeel Abbassi on 12/10/2017 at 6:57 AM.
 */

public class TransparentBlockingView extends View implements View.OnClickListener {
    private final String TAG = "TransparentBlockingView";
    private WindowManager windowManager;
    private WindowManager.LayoutParams blockingViewParams;

    public TransparentBlockingView(Context context) {
        super(context);
    }

    public TransparentBlockingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TransparentBlockingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TransparentBlockingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initParamsAndListeners(WindowManager windowManager){
        this.windowManager = windowManager;
        setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            blockingViewParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
            );
        }
        else{
            blockingViewParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR ,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
            );
        }
        blockingViewParams.gravity = Gravity.TOP | Gravity.START;
        blockingViewParams.x = 0;
        blockingViewParams.y = 0;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "blocking view clicked");
    }

    public void addToWindow(){
        windowManager.addView(this, blockingViewParams);
    }

    public void removeFromWindow(){
        windowManager.removeView(this);
    }
}
