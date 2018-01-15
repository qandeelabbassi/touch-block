package com.mom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mom.receivers.MomDeviceAdminReceiver;
import com.mom.services.TouchControlService;
import com.mom.util.Constants;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_ENABLE_ADMIN = 2000;
    private int OVERLAY_PERM_REQUEST = 1000;
    private int NOTIFICATION_ID = 10;
    private Notification mNotification;
    private Toolbar toolbar;
    private Switch swtEnable;
    private TextView help;
    private EditText timer;
    private EditText edtPassword;
    private SharedPreferences preferences;
    private RadioGroup mode;
    private TextView btnUnlockTiming;
    private int hour = -1;
    private int minute = -1;
    private Spinner timerUnit;
    private int spinnerCheck = 0;
    private ImageView imgShowHidePass;
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(this, MomDeviceAdminReceiver.class);

        preferences = getSharedPreferences(Constants.PREFERENCE_FILE, MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mobile Obsession Manipulator");

        edtPassword = findViewById(R.id.edt_pass);
        mode = findViewById(R.id.radiogrp_mode);
        swtEnable = findViewById(R.id.enable_switch);
        help = findViewById(R.id.btn_help);
        timerUnit = findViewById(R.id.time_unit_spinner);
        timer = findViewById(R.id.edt_timer);
        imgShowHidePass = findViewById(R.id.imgShowHide);

        boolean notifVisible = preferences.getBoolean(Constants.KEY_NOTIF_VISIBILITY, false);
        String md = preferences.getString(Constants.KEY_MODE, "none");
        String password = preferences.getString(Constants.KEY_PASSWORD, "");
        String timUnit = preferences.getString(Constants.KEY_TIMER_UNIT, "hours");
        String time = preferences.getString(Constants.KEY_TIME, "");
        if (md.equals(getString(R.string.kids_mode))) {
            mode.check(R.id.radio_kids);
            findViewById(R.id.clock_settings).setVisibility(View.GONE);
        } else if (md.equals(getString(R.string.mom_mode))) {
            mode.check(R.id.radio_mom);
        } else if (md.equals(getString(R.string.punishment_mode))) {
            mode.check(R.id.radio_punishment);
            findViewById(R.id.edt_pass_cont).setVisibility(View.VISIBLE);
            edtPassword.setText(password);
        }
        if (md.equals(getString(R.string.punishment_mode)) || md.equals(getString(R.string.mom_mode))) {
            findViewById(R.id.clock_settings).setVisibility(View.VISIBLE);
            if (timUnit.equals("hours"))
                timerUnit.setSelection(0);
            else
                timerUnit.setSelection(1);
            timer.setText(time);
        }
        if (notifVisible)
            swtEnable.setChecked(true);
        setListeners();
        if (!mDPM.isAdminActive(mDeviceAdmin)) {
            new MaterialDialog.Builder(this)
                    .title("Administrator Access")
                    .content("This app requires administrator access to prevent uninstallation when touch lock is enabled.")
                    .positiveText("OK")
                    .negativeText("CANCEL")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            checkDeviceAdminState();
                        }
                    })
                    .show();
        }
    }

    private void checkDeviceAdminState() {
        boolean isActive = false;
        if (mDPM != null) {
            isActive = mDPM.isAdminActive(mDeviceAdmin);
        }
        if (!isActive) {
            // Launch the activity to have the user enable our admin.
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.mom_device_admin_description));
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        }
    }

    private void setListeners() {
        help.setOnClickListener(this);
        imgShowHidePass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        edtPassword.setSelection(edtPassword.getText().length());
                        break;
                    case MotionEvent.ACTION_UP:
                        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        edtPassword.setSelection(edtPassword.getText().length());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                return true;
            }
        });
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (swtEnable.isChecked()) {
                    swtEnable.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        timer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (swtEnable.isChecked()) {
                    swtEnable.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (timer.getText().toString().equals(""))
                    return;
                if (Integer.valueOf(timer.getText().toString()) <= 0) {
                    timer.setText("");
                    Toast.makeText(MainActivity.this, "Time can't be 0.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (String.valueOf(timerUnit.getSelectedItem()).equals("hours")) {
                    int time = getTimeLimit(Constants.TIMER_UNITS.HOUR);
                    if (Integer.valueOf(timer.getText().toString()) > time) {
                        timer.setText("");
                        Toast.makeText(MainActivity.this, "Hours should be <= " + time, Toast.LENGTH_LONG).show();
                    }
                } else if (String.valueOf(timerUnit.getSelectedItem()).equals("minutes")) {
                    int time = getTimeLimit(Constants.TIMER_UNITS.MINUTES);
                    if (Integer.valueOf(timer.getText().toString()) > getTimeLimit(Constants.TIMER_UNITS.MINUTES)) {
                        timer.setText("");
                        Toast.makeText(MainActivity.this, "Minutes should be <= " + time, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        timerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (swtEnable.isChecked() && ++spinnerCheck > 1) {
                    swtEnable.setChecked(false);
                }
                timer.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                swtEnable.setChecked(false);
                int selectedId = mode.getCheckedRadioButtonId();
                String stMode = ((RadioButton) findViewById(selectedId)).getText().toString();
                preferences.edit().putString(Constants.KEY_MODE, stMode).apply();
                if (!stMode.equals(getString(R.string.kids_mode))) {
                    findViewById(R.id.clock_settings).setVisibility(View.VISIBLE);
                    if (stMode.equals(getString(R.string.punishment_mode)))
                        findViewById(R.id.edt_pass_cont).setVisibility(View.VISIBLE);
                    else
                        findViewById(R.id.edt_pass_cont).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.clock_settings).setVisibility(View.GONE);
                    findViewById(R.id.edt_pass_cont).setVisibility(View.GONE);
                }
            }
        });
        swtEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    preferences.edit().putBoolean(Constants.KEY_NOTIF_VISIBILITY, false).apply();
                    cancelAllNotifications();
                    return;
                }
                int selectedId = mode.getCheckedRadioButtonId();
                String stMode = ((RadioButton) findViewById(selectedId)).getText().toString();
                if (isChecked && !stMode.equals(getString(R.string.kids_mode)) && (timer.getText().toString().length() == 0
                        || Integer.valueOf(timer.getText().toString()) <= 0)) {
                    swtEnable.setChecked(false);
                    Toast.makeText(MainActivity.this, "Please set the time first!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (isChecked && stMode.equals(getString(R.string.punishment_mode)) && edtPassword.getText().toString().length() == 0) {
                    swtEnable.setChecked(false);
                    Toast.makeText(MainActivity.this, "Please set the password first!", Toast.LENGTH_LONG).show();
                    return;
                }
                setTimer();
                preferences.edit().putBoolean(Constants.KEY_NOTIF_VISIBILITY, isChecked).apply();
                preferences.edit().putString(Constants.KEY_PASSWORD, edtPassword.getText().toString()).apply();
                startMyService();
            }
        });
    }

    private void cancelAllNotifications() {
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManger != null;
        notificationManger.cancelAll();
    }

    private void startMyService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERM_REQUEST);
            } else {
                showOngoingNotification();
            }
        } else {
            showOngoingNotification();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERM_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this))
                    showOngoingNotification();
                else
                    swtEnable.setChecked(false);
            }
        } else if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode != RESULT_OK)
                Toast.makeText(MainActivity.this, "App might not function properly!", Toast.LENGTH_LONG).show();

        }
    }

    private void showOngoingNotification() {
        int selectedId = mode.getCheckedRadioButtonId();
        String content = getString(R.string.ongoing_notification_text);
        String stMode = ((RadioButton) findViewById(selectedId)).getText().toString();
        int icon = R.drawable.ic_ongoing_notification;
        Intent notificationIntent = new Intent(this, TouchControlService.class);
        notificationIntent.putExtra(getString(R.string.extra_lock_mode), stMode);
        notificationIntent.putExtra(getString(R.string.extra_timer_unit), String.valueOf(timerUnit.getSelectedItem()));
        notificationIntent.putExtra(getString(R.string.extra_timer_hour), hour);
        notificationIntent.putExtra(getString(R.string.extra_timer_minute), minute);
        notificationIntent.putExtra(getString(R.string.extra_password), edtPassword.getText().toString());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (stMode.equals(getString(R.string.mom_mode)) || stMode.equals(getString(R.string.punishment_mode))) {
            if (String.valueOf(timerUnit.getSelectedItem()).equals("hours"))
                content = getResources().getQuantityString(R.plurals.ongoing_notification_text_hour, hour, hour);
            if (String.valueOf(timerUnit.getSelectedItem()).equals("minutes"))
                content = getResources().getQuantityString(R.plurals.ongoing_notification_text_minute, minute, minute);

        }
        mNotification = new NotificationCompat.Builder(this, "lockNotificationChannel")
                .setContentTitle(stMode)
                .setContentText(content)
                .setSmallIcon(icon)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(NOTIFICATION_ID, mNotification);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_help:
                new MaterialDialog.Builder(this)
                        .title(null)
                        .customView(R.layout.dialog_mode_help, false)
                        .positiveText("OK")
                        .show();
                break;
//            case R.id.btn_set_clock:
//                Calendar now = Calendar.getInstance();
//                TimePickerDialog tpd = TimePickerDialog.newInstance(
//                        MainActivity.this,
//                        now.get(Calendar.HOUR_OF_DAY),
//                        now.get(Calendar.MINUTE),
//                        false
//                );
//                tpd.show(getFragmentManager(), "TimePickerDialog");
//                break;
        }
    }

    public void setTimer() {
        hour = -1;
        minute = -1;
        if (timer.getText().toString().length() == 0) {
            return;
        }
        if (String.valueOf(timerUnit.getSelectedItem()).equals("hours")) {
            setTimerHours(Integer.valueOf(timer.getText().toString()));
        } else {
            setTimerMinutes(Integer.valueOf(timer.getText().toString()));
        }
    }

    public void setTimerHours(int hours) {
        this.hour = hours;
        preferences.edit().putString(Constants.KEY_TIMER_UNIT, "hours").apply();
        preferences.edit().putString(Constants.KEY_TIME, String.valueOf(hours)).apply();
    }

    public void setTimerMinutes(int minutes) {
        this.minute = minutes;
        preferences.edit().putString(Constants.KEY_TIMER_UNIT, "minutes").apply();
        preferences.edit().putString(Constants.KEY_TIME, String.valueOf(minutes)).apply();
    }

    public int getTimeLimit(Constants.TIMER_UNITS timerUnit) {
        int selectedId = mode.getCheckedRadioButtonId();
        String stMode = ((RadioButton) findViewById(selectedId)).getText().toString();
        if (timerUnit == Constants.TIMER_UNITS.HOUR) {
            if (stMode.equals(getString(R.string.mom_mode)))
                return 72;
            else
                return 24;
        } else {
            if (stMode.equals(getString(R.string.mom_mode)))
                return 4320;
            else
                return 1440;
        }
    }
}
