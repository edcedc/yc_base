package com.yc.yc_base.controller;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.luck.picture.lib.entity.LocalMedia;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okrx2.adapter.ObservableBody;
import com.lzy.okrx2.adapter.ObservableResponse;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：yc on 2018/6/20.
 * 邮箱：501807647@qq.com
 * 版本：v1.0
 */

public class CloudApi {


    private static final String url =
//            "10.0.0.200:8081/luxury_shopping/" ;
//            "47.106.217.107/";
            "shegouapp.com/";

    public static final String SERVLET_IMG_URL = "http://" +
            url;

    public static final String SERVLET_URL = SERVLET_IMG_URL + "api/";


    public static final String TEST_URL = ""; //测试

    private static final String TAG = "CloudApi";

    private CloudApi() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

}