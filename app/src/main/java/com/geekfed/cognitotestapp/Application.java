package com.geekfed.cognitotestapp;

import com.facebook.FacebookSdk;

/**
 * Created by cody on 2/14/16.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
    }
}
