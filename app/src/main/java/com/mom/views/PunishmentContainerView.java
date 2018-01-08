package com.mom.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mom.R;
import com.mom.events.UnlockEvent;
import com.mom.util.FontCache;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;

/**
 * Code written by Qandeel Abbassi on 1/7/2018 at 6:03 AM.
 */

public class PunishmentContainerView extends LinearLayout implements View.OnTouchListener, TextView.OnEditorActionListener {

    private Context ctx;
    private Calendar then;
    private WindowManager windowManager;
    private WindowManager.LayoutParams passwordLayoutParams;
    private String password;
    private EditText edtUnlockPass;
    private TextView txtPunTimer;
    private long last_text_edit = 0;
    private long delay = 5000; // 1 seconds after user stops interaction
    private Handler handler = new Handler();
    private Handler timerHandler = new Handler();
    private ValueAnimator animation;
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            long time = last_text_edit + delay;
            long cur = System.currentTimeMillis();
            if (cur >= time) {
                animateAlpha();
            }
        }
    };
    private Runnable timer_updater = new Runnable() {
        @Override
        public void run() {
            setTimerText();
        }
    };

    public PunishmentContainerView(Context context) {
        super(context);
        this.ctx = context;
        this.setOrientation(LinearLayout.VERTICAL);
        this.setGravity(Gravity.CENTER);
        this.setBackgroundColor(context.getResources().getColor(R.color.timer_txt_bg));
        LayoutInflater inflater = LayoutInflater.from(context);
        View inflatedLayout = inflater.inflate(R.layout.layout_password_container, null, false);
        edtUnlockPass = inflatedLayout.findViewById(R.id.edtUnlockPass);
        txtPunTimer = inflatedLayout.findViewById(R.id.txtPunTimer);
        applyCustomFont(context, txtPunTimer);
        edtUnlockPass.setOnEditorActionListener(this);
        edtUnlockPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                last_text_edit = System.currentTimeMillis();
                handler.postDelayed(input_finish_checker, delay);
                cancelAnimation();
                passwordLayoutParams.alpha = 1.0f;
                windowManager.updateViewLayout(PunishmentContainerView.this, passwordLayoutParams);
            }
        });
        this.addView(inflatedLayout);
    }

    private void applyCustomFont(Context context, TextView textView) {
        Typeface customFont = FontCache.getTypeface("fonts/opensans-reg.ttf", context);
        textView.setTypeface(customFont);
    }

    public PunishmentContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PunishmentContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initParamsAndListeners(WindowManager windowManager, Calendar then, String pass) {
        this.then = then;
        this.password = pass;
        this.windowManager = windowManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            passwordLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT);
        } else {
            passwordLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT);
        }
        passwordLayoutParams.gravity = Gravity.CENTER;
        passwordLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        passwordLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last_text_edit = System.currentTimeMillis();
                cancelAnimation();
                passwordLayoutParams.alpha = 1.0f;
                break;
            case MotionEvent.ACTION_UP:
                handler.postDelayed(input_finish_checker, delay);
                break;
            case MotionEvent.ACTION_MOVE:
                windowManager.updateViewLayout(this, passwordLayoutParams);
                break;
        }
        return false;
    }

    public void addToWindow() {
        windowManager.addView(this, passwordLayoutParams);
        handler.postDelayed(input_finish_checker, delay);
        setTimerText();
    }

    public void removeFromWindow() {
        try {
            timerHandler.removeCallbacks(timer_updater);
            handler.removeCallbacks(input_finish_checker);
            windowManager.removeView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTimerText() {
        String text = "";
        Date date1 = Calendar.getInstance().getTime();
        Date date2 = then.getTime();
        long diff = date2.getTime() - date1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        String stSec, stMin, stHr;
        if (diffSeconds < 10)
            stSec = String.format("%02d", diffSeconds);
        else
            stSec = String.valueOf(diffSeconds);

        if (diffMinutes < 10)
            stMin = String.format("%02d", diffMinutes);
        else
            stMin = String.valueOf(diffMinutes);

        if (diffHours < 10)
            stHr = String.format("%02d", diffHours);
        else
            stHr = String.valueOf(diffHours);

        text = stHr + ":" + stMin + ":" + stSec;

        txtPunTimer.setText(text);
        timerHandler.postDelayed(timer_updater, 1000);
    }

    private void animateAlpha() {
        hideSoftKeyboard();
        animation = ValueAnimator.ofFloat(1.0f, 0f);
        animation.setDuration(500);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                try {
                    passwordLayoutParams.alpha = (float) updatedAnimation.getAnimatedValue();
                    windowManager.updateViewLayout(PunishmentContainerView.this, passwordLayoutParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        animation.start();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d("test", "DONE");
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideSoftKeyboard();
            if (password.equals(edtUnlockPass.getText().toString())) {
                EventBus.getDefault().post(new UnlockEvent());
            }
            return true;
        }
        return false;
    }

    private void cancelAnimation() {
        try {
            if (animation != null)
                animation.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtUnlockPass.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
