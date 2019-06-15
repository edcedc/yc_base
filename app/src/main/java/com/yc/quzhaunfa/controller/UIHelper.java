package com.yc.quzhaunfa.controller;

import com.blankj.utilcode.util.ActivityUtils;
import com.yc.quzhaunfa.MainActivity;


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