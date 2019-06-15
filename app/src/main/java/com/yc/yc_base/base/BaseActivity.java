package com.yc.yc_base.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.yc.yc_base.R;
import com.yc.yc_base.base.BasePresenter;
import com.yc.yc_base.utils.Constants;
import com.yc.yc_base.utils.TUtil;
import com.yc.yc_base.utils.pay.PayResult;
import com.yc.yc_base.weight.AuthResult;

import org.json.JSONObject;

import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * 作者：yc on 2018/7/25.
 * 邮箱：501807647@qq.com
 * 版本：v1.0
 */

public abstract class BaseActivity<P extends BasePresenter, VB extends ViewDataBinding> extends SupportActivity {

    protected Activity act;
    protected VB mB;
    public P mPresenter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mB = DataBindingUtil.setContentView(this,this.bindLayout());;
        act = this;
        mPresenter = TUtil.getT(this, 0);
        if (mPresenter != null) {
            mPresenter.act = this;
            this.initPresenter();
        }
        // 初始化参数
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        initParms(bundle);

        initView();
        api = WXAPIFactory.createWXAPI(act, Constants.WX_APPID);
    }

    protected abstract void initPresenter();

    protected void setSofia(boolean isFullScreen) {

    }

    protected abstract int bindLayout();

    protected abstract void initParms(Bundle bundle);

    protected abstract void initView();

    protected void setCenterTitle(String title){
        title(title, 0, null, -1);
    }
    protected void setTitle(String title){
        title(title, 1, null, -1);
    }
    protected void setTitle(String title, String right){
        title(title, 2, right, -1);
    }
    protected void setTitle(String title, int rightImg){
        title(title, 1, null, rightImg);
    }

    private void title(String title, int type, String rightText, int img) {
        final AppCompatActivity mAppCompatActivity = (AppCompatActivity) act;
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView topTitle = findViewById(R.id.top_title);
        TextView topRight = findViewById(R.id.top_right);
        FrameLayout topRightFy = findViewById(R.id.top_right_fy);
        //需要调用该函数才能设置toolbar的信息
        mAppCompatActivity.setSupportActionBar(toolbar);
        switch (type){
            case 0:
                mAppCompatActivity.getSupportActionBar().setTitle("");
                topTitle.setVisibility(View.VISIBLE);
                topTitle.setText(title);
                toolbar.setNavigationIcon(null);
                break;
            case 1:
                topTitle.setVisibility(View.GONE);
                mAppCompatActivity.getSupportActionBar().setTitle(title);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                break;
            case 2:
                topTitle.setVisibility(View.GONE);
                mAppCompatActivity.getSupportActionBar().setTitle(title);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                topRight.setText(rightText);
                topRightFy.setVisibility(View.VISIBLE);
                topRightFy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setOnRightClickListener();
                    }
                });
                break;
        }
    }

    protected void setOnRightClickListener() {

    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime;

    protected boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    private ProgressDialog dialog;

    public void showLoading() {
        mHandler.sendEmptyMessage(handler_load);
    }

    public void hideLoading() {
        mHandler.sendEmptyMessage(handler_hide);
    }

    public void onError(Throwable e) {
        mHandler.sendEmptyMessage(handler_hide);
        LogUtils.e(e.getMessage());
        showToast(e.getMessage());
    }

    private final int handler_load = 0;
    private final int handler_hide = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case handler_load:
                    if (dialog != null && dialog.isShowing()) return;
                    dialog = new ProgressDialog(act);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setMessage("请求网络中...");
                    dialog.show();
                    break;
                case handler_hide:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        LogUtils.e("支付宝支付成功");
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        LogUtils.e("支付宝支付失败");
                    }
                    break;
                }
                case SDK_AUTH_FLAG: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        LogUtils.e("支付宝授权成功");
                    } else {
                        // 其他状态值则为授权失败
                        LogUtils.e("支付宝授权失败");
                    }
                    break;
                }
            }
        }
    };

    protected void showToast(final String str){
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                ToastUtils.setGravity(Gravity.CENTER, 0, 0);
//                ToastUtils.showLong(str);
                ToastUtils.showShort(str);
            }
        });
    }

    private CompositeDisposable compositeDisposable;

    public void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    private void dispose() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        dispose();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    //支付宝支付
    public void pay(final String info){
        new Thread(new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(act);
                Map<String, String> result = alipay.payV2(info,true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    //微信支付
    protected IWXAPI api;
    protected void wxPay(final JSONObject data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (api.getWXAppSupportAPI() >= com.tencent.mm.opensdk.constants.Build.PAY_SUPPORTED_SDK_INT) {
                    PayReq req = new PayReq();
                    req.appId = data.optString("appid");
                    req.partnerId = data.optString("partnerid");
                    req.prepayId = data.optString("prepayid");
                    req.nonceStr = data.optString("noncestr");
                    req.timeStamp = data.optString("timestamp");
                    req.packageValue = data.optString("package");
                    req.sign = data.optString("sign");
                    req.extData = "app data"; // optional
                    api.sendReq(req);
                }else {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(getResources().getString(R.string.not_wx_pay));
                        }
                    });
                }
            }
        }).start();
    }

    private static final int SDK_PAY_FLAG = 99;
    private static final int SDK_AUTH_FLAG = 98;

}
