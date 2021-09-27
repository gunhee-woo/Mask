package com.dlog.mask;

import android.app.Application;

public class GlobalApplication extends Application {
    public static SharedPreferencesActivity prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new SharedPreferencesActivity(getApplicationContext());
    }
}