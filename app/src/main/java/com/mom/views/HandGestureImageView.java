package com.mom.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.mom.R;
import com.mom.events.UnlockEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Qandeel Abbassi on 12/10/2017 at 6:23 AM.
 */

public class HandGestureImageView extends AppCompatImageView implements View.OnTouchListener {
    private WindowManager.LayoutParams gestureIconParams;
    private long lastPressTime;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager windowManager;
    private long delay = 1500; // 1 seconds after user stops interaction
    private long last_text_edit = 0;
    private Handler handler = new Handler();
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay)) {
                animateAlpha(1.0f, 0.5f);
            }
        }
    };

    public HandGestureImageView(Context context) {
        super(context);
    }

    public HandGestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HandGestureImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initParamsAndListeners(WindowManager windowManager) {
        this.windowManager = windowManager;
        this.setImageResource(R.drawable.ic_hand_gesture);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gestureIconParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            gestureIconParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        gestureIconParams.gravity = Gravity.TOP | Gravity.START;
        gestureIconParams.y = 10;
        gestureIconParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        gestureIconParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        setOnTouchListener(this);
    }

    public void addToWindow() {
        windowManager.addView(this, gestureIconParams);
        handler.postDelayed(input_finish_checker, delay);
    }

    public void removeFromWindow() {
        try {
            handler.removeCallbacks(input_finish_checker);
            windowManager.removeView(this);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last_text_edit = System.currentTimeMillis();
                gestureIconParams.alpha = 1.0f;
                // windowManager.updateViewLayout(HandGestureImageView.this, gestureIconParams);
                // Get current time in nano seconds.
                long pressTime = System.currentTimeMillis();
                // If double click...
                if (pressTime - lastPressTime <= 500) {
                    //stopIt();
                    EventBus.getDefault().post(new UnlockEvent());
                }
                lastPressTime = pressTime;
                initialX = gestureIconParams.x;
                initialY = gestureIconParams.y;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                handler.postDelayed(input_finish_checker, delay);
                break;
            case MotionEvent.ACTION_MOVE:
                gestureIconParams.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                gestureIconParams.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);
                windowManager.updateViewLayout(this, gestureIconParams);
                break;
        }
        return false;
    }

    private void animateAlpha(float start, float end) {
        ValueAnimator animation = ValueAnimator.ofFloat(start, end);
        animation.setDuration(500);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                try {
                    float animatedValue = (float) updatedAnimation.getAnimatedValue();
                    gestureIconParams.alpha = animatedValue;
                    windowManager.updateViewLayout(HandGestureImageView.this, gestureIconParams);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        animation.start();
    }

}
