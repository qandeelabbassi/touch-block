package com.mom.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mom.R;

/**
 * Code written by Qandeel Abbassi on 1/16/2018 at 2:51 AM.
 */

public class MomDeviceAdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLE_REQUESTED)) {
            abortBroadcast();
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d("test", "enabled");
        //showToast(context, context.getString(R.string.admin_receiver_status_enabled));
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_receiver_status_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d("test", "disabled");
        //showToast(context, context.getString(R.string.admin_receiver_status_disabled));
    }

}

