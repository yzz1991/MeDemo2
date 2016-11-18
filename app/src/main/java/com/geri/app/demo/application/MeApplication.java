package com.geri.app.demo.application;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

/**
 * Created by Geri on 2016/10/18.
 */

public class MeApplication extends MultiDexApplication{

    private static Context mContext;


    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        //初使化
        MyHyphenate.getInstance().initData(mContext);
    }


}
