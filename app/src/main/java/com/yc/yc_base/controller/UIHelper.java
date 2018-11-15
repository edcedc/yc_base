package com.yc.yc_base.controller;

import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.google.gson.Gson;
import com.yc.yc_base.MainActivity;

import java.lang.reflect.Type;


/**
 * Created by Administrator on 2017/2/22.
 */

public final class UIHelper {

    private UIHelper() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void startMainAct() {
        ActivityUtils.startActivity(MainActivity.class);
    }

}