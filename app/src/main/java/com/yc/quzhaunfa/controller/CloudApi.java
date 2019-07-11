package com.yc.quzhaunfa.controller;

import com.blankj.utilcode.util.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableBody;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.yc.quzhaunfa.bean.BaseListBean;
import com.yc.quzhaunfa.bean.BaseResponseBean;
import com.yc.quzhaunfa.bean.DataBean;
import com.yc.quzhaunfa.callback.JsonConvert;
import com.yc.quzhaunfa.callback.NewsCallback;
import com.yc.quzhaunfa.utils.Constants;
import com.yc.quzhaunfa.utils.cache.ShareSessionIdCache;

import org.json.JSONObject;

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

    /**
     *  获取微信登陆返回值
     */
    public static Observable<JSONObject> wxLogin(String openid, String access_token){
        return OkGo.<JSONObject>get("https://api.weixin.qq.com/sns/userinfo")
                .params("access_token", access_token)
                .params("openid", openid)
                .converter(new JsonConvert<JSONObject>() {})
                .adapt(new ObservableBody<JSONObject>())
                .subscribeOn(Schedulers.io());
    }


    /**
     * 通用list数据
     */
    public static Observable<Response<BaseResponseBean<BaseListBean<DataBean>>>> list(int pageNumber, String url) {
        return OkGo.<BaseResponseBean<BaseListBean<DataBean>>>post(SERVLET_URL + url)
                .params("pageNumber", pageNumber)
                .params("pageSize", Constants.pageSize)
                .params("sessionId", ShareSessionIdCache.getInstance(Utils.getApp()).getSessionId())
                .converter(new NewsCallback<BaseResponseBean<BaseListBean<DataBean>>>() {
                    @Override
                    public void onSuccess(Response<BaseResponseBean<BaseListBean<DataBean>>> response) {

                    }
                })
                .adapt(new ObservableResponse<BaseResponseBean<BaseListBean<DataBean>>>())
                .subscribeOn(Schedulers.io());
    }
    /**
     * 通用list 2
     */
    public static Observable<Response<BaseResponseBean<List<DataBean>>>> list2(String url) {
        return OkGo.<BaseResponseBean<List<DataBean>>>post(SERVLET_URL + url)
                .params("sessionId", ShareSessionIdCache.getInstance(Utils.getApp()).getSessionId())
                .converter(new NewsCallback<BaseResponseBean<List<DataBean>>>() {
                    @Override
                    public void onSuccess(Response<BaseResponseBean<List<DataBean>>> response) {
                    }
                })
                .adapt(new ObservableResponse<BaseResponseBean<List<DataBean>>>())
                .subscribeOn(Schedulers.io());
    }
}