package com.mom.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService {
    private static MyAccessibilityService myAccessibilityService;
    private boolean shouldBlockSoftKeys = false;

    public static MyAccessibilityService getInstance() {
        return myAccessibilityService;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        myAccessibilityService = this;
    }

    @Override
    public void onDestroy() {
        myAccessibilityService = null;
    }

    public void setShouldBlockSoftKeys(boolean shouldBlock) {
        shouldBlockSoftKeys = shouldBlock;
        int i = 32;
        if (Build.VERSION.SDK_INT >= 18) {
            AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
            accessibilityServiceInfo.eventTypes = Build.VERSION.SDK_INT >= 26 ? RecyclerView.ItemAnimator.FLAG_MOVED : 32;
            if (!shouldBlock) {
                i = 1;
            }
            accessibilityServiceInfo.flags = i;
            accessibilityServiceInfo.feedbackType = 16;
            setServiceInfo(accessibilityServiceInfo);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return shouldBlockSoftKeys;
    }

    @Override
    public void onInterrupt() {

    }
}