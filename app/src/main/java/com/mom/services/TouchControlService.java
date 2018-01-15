package com.mom.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.mom.MainActivity;
import com.mom.R;
import com.mom.events.TimerCompleteEvent;
import com.mom.events.UnlockEvent;
import com.mom.receivers.AlarmReceiver;
import com.mom.receivers.MomDeviceAdminReceiver;
import com.mom.util.Constants;
import com.mom.views.HandGestureImageView;
import com.mom.views.PunishmentContainerView;
import com.mom.views.TimerTextView;
import com.mom.views.TransparentBlockingView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

/**
 * Code written by Qandeel Abbassi on 12/10/2017 at 5:32 AM at 8:01 AM.
 */

public class TouchControlService extends Service {

    private static final String TAG = "TouchControlService";
    WindowManager windowManager;
    HandGestureImageView lockIcon;
    TransparentBlockingView fullView;
    boolean isLocked;
    private String lockMode;
    private int hour;
    private int minute;
    private boolean permissionRequired = false;
    private TimerTextView timerTextView;
    private PunishmentContainerView punishmentContainerView;
    private PendingIntent pendingIntent;
    private String password;
    private SharedPreferences preferences;
    private Calendar then;
    private ComponentName mDeviceAdmin;
    private DevicePolicyManager mDPM;

    public TouchControlService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        isLocked = false;

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(this, MomDeviceAdminReceiver.class);

        preferences = getSharedPreferences(Constants.PREFERENCE_FILE, MODE_PRIVATE);
        if (!hasPreRequisitePerms()) {
            permissionRequired = true;
            stopForeground(true);
            stopSelf();
            return;
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        fullView = new TransparentBlockingView(TouchControlService.this);
        lockIcon = new HandGestureImageView(TouchControlService.this);
        timerTextView = new TimerTextView(TouchControlService.this);
        punishmentContainerView = new PunishmentContainerView(TouchControlService.this);
        fullView.initParamsAndListeners(windowManager);
        lockIcon.initParamsAndListeners(windowManager);
        EventBus.getDefault().register(this);
    }

    private boolean hasPreRequisitePerms() {
        if (!isAdminActive()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return false;
        } else if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return false;
        } else if (!isAccessibilitySettingsOn(this)) {
            Toast.makeText(getApplicationContext(), "Enable Mom- Key Lock", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (permissionRequired) {
            return START_NOT_STICKY;
        }
        lockMode = intent.getStringExtra(getString(R.string.extra_lock_mode));
        if (lockMode.equals(getString(R.string.mom_mode)) || lockMode.equals(getString(R.string.punishment_mode))) {
            Constants.TIMER_UNITS timerunit;
            password = intent.getStringExtra(getString(R.string.extra_password));
            if (intent.getStringExtra(getString(R.string.extra_timer_unit)).equals("hours")) {
                hour = intent.getIntExtra(getString(R.string.extra_timer_hour), -1);
                minute = -1;
                timerunit = Constants.TIMER_UNITS.HOUR;
            } else if (intent.getStringExtra(getString(R.string.extra_timer_unit)).equals("minutes")) {
                minute = intent.getIntExtra(getString(R.string.extra_timer_minute), -1);
                hour = -1;
                timerunit = Constants.TIMER_UNITS.MINUTES;
            } else {
                return START_NOT_STICKY;
            }
            long timeInMilis = intent.getLongExtra(getString(R.string.extra_time_consumed), -1);
            if (lockMode.equals(getString(R.string.punishment_mode)))
                scheduleUnlockEvent(this, timerunit, true, timeInMilis);
            else
                scheduleUnlockEvent(this, timerunit, false, timeInMilis);
        }
        runAsForeground("MOM", "Screen Locked!");
        freez();
        return START_STICKY;
    }

    private boolean isAdminActive() {
        return mDPM != null && mDPM.isAdminActive(mDeviceAdmin);
    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    @Override
    public void onDestroy() {
        Log.d("test", "service destroy");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onUnlockEvent(UnlockEvent unlockEvent) {
        Log.d(TAG, "touch unlock event");
        unlock();
    }

    @Subscribe
    public void onTimerCompleteEvent(TimerCompleteEvent timerCompleteEvent) {
        Log.d(TAG, "touch unlock event with timer");
        unlock();
    }

    private void unlock() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        if (isLocked) {
            fullView.removeFromWindow();
            lockIcon.removeFromWindow();
            if (lockMode.equals(getString(R.string.mom_mode))) {
                timerTextView.removeFromWindow();
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            } else if (lockMode.equals(getString(R.string.punishment_mode))) {
                punishmentContainerView.removeFromWindow();
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
        }
        isLocked = false;
        preferences.edit().putBoolean(Constants.KEY_LOCKED, false).apply();
        MyAccessibilityService myAccessibilityService = MyAccessibilityService.getInstance();
        if (myAccessibilityService != null)
            myAccessibilityService.setShouldBlockSoftKeys(false);
        stopForeground(true);
        stopSelf();
    }

    private void freez() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        //Toast.makeText(TouchControlService.this, "Touch Locked!", Toast.LENGTH_SHORT).show();
        MyAccessibilityService myAccessibilityService = MyAccessibilityService.getInstance();
        if (myAccessibilityService != null)
            myAccessibilityService.setShouldBlockSoftKeys(true);
        fullView.addToWindow();
        if (lockMode.equals(getString(R.string.mom_mode)))
            timerTextView.addToWindow();
        if (lockMode.equals(getString(R.string.punishment_mode)))
            punishmentContainerView.addToWindow();
        lockIcon.addToWindow();
        isLocked = true;
        preferences.edit().putBoolean(Constants.KEY_LOCKED, true).apply();
        //Toast.makeText(TouchControlService.this, "Touch Locked!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void runAsForeground(String contentTitle, String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        int icon = R.drawable.ic_lock;
        Notification mNotification = new NotificationCompat.Builder(this, "lockNotificationChannel")
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(icon)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        int notificationID = 1000;
        startForeground(notificationID, mNotification);
    }

    public void scheduleUnlockEvent(Context mContext, Constants.TIMER_UNITS timerUnit, boolean isPunishment, long timeInMilis) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (timeInMilis <= 0) {
            then = Calendar.getInstance();
            if (timerUnit.equals(Constants.TIMER_UNITS.HOUR)) {
                then.add(Calendar.HOUR_OF_DAY, hour);
            } else if (timerUnit.equals(Constants.TIMER_UNITS.MINUTES)) {
                then.add(Calendar.MINUTE, minute);
            }
            preferences.edit().putLong(Constants.KEY_TIME_CONSUMED, then.getTimeInMillis()).apply();
        } else {
            then = Calendar.getInstance();
            then.setTimeInMillis(timeInMilis);
        }
        if (isPunishment)
            punishmentContainerView.initParamsAndListeners(windowManager, then, password);
        else
            timerTextView.initParamsAndListeners(windowManager, then);
        assert alarmManager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, then.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, then.getTimeInMillis(), pendingIntent);
        }
    }
}
