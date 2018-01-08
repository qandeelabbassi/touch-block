package com.mom.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.mom.util.FontCache;
import com.mom.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Code written by Qandeel Abbassi on 12/23/2017 at 9:50 PM.
 */

public class TimerTextView extends AppCompatTextView implements View.OnTouchListener {

    private final String TAG = "TimerTextView";
    private WindowManager windowManager;
    private WindowManager.LayoutParams timerViewParams;
    private long last_text_edit = 0;
    private long lastPressTime;
    private long delay = 1000; // 1 seconds after user stops interaction
    private Handler handler = new Handler();
    private Calendar then;
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay)) {
                animateAlpha(1.0f, 0f);
            }
        }
    };

    public TimerTextView(Context context) {
        super(context);
        applyCustomFont(context);
        this.setTextSize(24);
        this.setGravity(Gravity.CENTER);
        this.setBackgroundColor(context.getResources().getColor(R.color.timer_txt_bg));
        this.setTextColor(context.getResources().getColor(R.color.timer_txt_color));
    }

    public TimerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public TimerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = FontCache.getTypeface("fonts/opensans-reg.ttf", context);
        setTypeface(customFont);
    }

    public void initParamsAndListeners(WindowManager windowManager, Calendar then){
        this.then = then;
        this.windowManager = windowManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timerViewParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            timerViewParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        timerViewParams.gravity = Gravity.CENTER;
        timerViewParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        timerViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        setOnTouchListener(this);
    }

    public void addToWindow() {
        windowManager.addView(this, timerViewParams);
        handler.postDelayed(input_finish_checker, delay);
        setTimerText();
    }

    public void removeFromWindow() {
        try {
            handler.removeCallbacks(input_finish_checker);
            windowManager.removeView(this);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void animateAlpha(float start, float end) {
        ValueAnimator animation = ValueAnimator.ofFloat(start, end);
        animation.setDuration(500);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                try {
                    float animatedValue = (float) updatedAnimation.getAnimatedValue();
                    timerViewParams.alpha = animatedValue;
                    windowManager.updateViewLayout(TimerTextView.this, timerViewParams);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        animation.start();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last_text_edit = System.currentTimeMillis();
                timerViewParams.alpha = 1.0f;
                setTimerText();
                break;
            case MotionEvent.ACTION_UP:
                handler.postDelayed(input_finish_checker, delay);
                break;
            case MotionEvent.ACTION_MOVE:
                windowManager.updateViewLayout(this, timerViewParams);
                break;
        }
        return false;
    }

    private void setTimerText(){
        String text = "";
        Date date1 = Calendar.getInstance().getTime();
        Date date2 = then.getTime();
        long diff = date2.getTime()-date1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        String stSec, stMin, stHr;
        if(diffSeconds < 10)
            stSec = String.format("%02d", diffSeconds);
        else
            stSec = String.valueOf(diffSeconds);

        if(diffMinutes < 10)
            stMin = String.format("%02d", diffMinutes);
        else
            stMin = String.valueOf(diffMinutes);

        if(diffHours < 10)
            stHr = String.format("%02d", diffHours);
        else
            stHr = String.valueOf(diffHours);

        text = stHr+":"+stMin+":"+stSec;

        this.setText(text+"\nRemaining");
    }
}
