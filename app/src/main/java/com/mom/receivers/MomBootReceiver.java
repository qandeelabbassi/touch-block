package com.mom.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.mom.R;
import com.mom.services.TouchControlService;
import com.mom.util.Constants;

import static android.content.Context.MODE_PRIVATE;
import static com.mom.util.Constants.KEY_MODE;
import static com.mom.util.Constants.KEY_PASSWORD;
import static com.mom.util.Constants.KEY_TIME;
import static com.mom.util.Constants.KEY_TIMER_UNIT;
import static com.mom.util.Constants.KEY_TIME_CONSUMED;

public class MomBootReceiver extends BroadcastReceiver {
    private SharedPreferences preferences;

    public MomBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        preferences = context.getSharedPreferences(Constants.PREFERENCE_FILE, MODE_PRIVATE);
        boolean isLocked = preferences.getBoolean(Constants.KEY_LOCKED, false);
        if (isLocked) {
            Log.d("test", "boot mom");
            Intent serviceIntent = new Intent(context, TouchControlService.class);
            serviceIntent.putExtra(context.getString(R.string.extra_lock_mode), preferences.getString(KEY_MODE, "none"));
            serviceIntent.putExtra(context.getString(R.string.extra_timer_unit), preferences.getString(KEY_TIMER_UNIT, "minutes"));
            serviceIntent.putExtra(context.getString(R.string.extra_timer_hour), Integer.valueOf(preferences.getString(KEY_TIME, "0")));
            serviceIntent.putExtra(context.getString(R.string.extra_timer_minute), Integer.valueOf(preferences.getString(KEY_TIME, "0")));
            serviceIntent.putExtra(context.getString(R.string.extra_time_consumed), preferences.getLong(KEY_TIME_CONSUMED, -1));
            serviceIntent.putExtra(context.getString(R.string.extra_password), preferences.getString(KEY_PASSWORD, "1234"));
            context.startService(serviceIntent);
        }
    }

}
