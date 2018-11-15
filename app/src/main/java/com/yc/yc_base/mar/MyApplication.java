package com.yc.yc_base.mar;

import android.content.Context;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.request.target.ViewTarget;
import com.nanchen.crashmanager.CrashApplication;
import com.yc.yc_base.R;
import com.yc.yc_base.service.InitializeService;

public class MyApplication extends CrashApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        ViewTarget.setTagId(R.id.tag_glide);//项目glide 图片ID找不到  会报null

        InitializeService.start(this);
    }

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

}
