package com.mom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mom.events.TimerCompleteEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Qandeel Abbassi on 12/21/2017 at 7:18 AM.
 */

public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new TimerCompleteEvent());
    }
}
