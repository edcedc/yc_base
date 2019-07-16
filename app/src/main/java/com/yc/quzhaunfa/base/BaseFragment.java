package com.yc.quzhaunfa.base;/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout;
import com.yc.quzhaunfa.R;
import com.yc.quzhaunfa.event.PayInEvent;
import com.yc.quzhaunfa.utils.TUtil;
import com.yc.quzhaunfa.utils.pay.PayResult;
import com.yc.quzhaunfa.weight.GridDividerItemDecoration;
import com.yc.quzhaunfa.weight.LoadingLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment;

/**
 * 若把初始化内容放到initData实现,就是采用Lazy方式加载的Fragment
 * 若不需要Lazy加载则initData方法内留空,初始化内容放到initViews即可
 * -
 * -注1: 如果是与ViewPager一起使用，调用的是setUserVisibleHint。
 * ------可以调用mViewPager.setOffscreenPageLimit(size),若设置了该属性 则viewpager会缓存指定数量的Fragment
 * -注2: 如果是通过FragmentTransaction的show和hide的方法来控制显示，调用的是onHiddenChanged.
 * -注3: 针对初始就show的Fragment 为了触发onHiddenChanged事件 达到lazy效果 需要先hide再show
 */

/**
 * ================================================
 * 作    者：yc）
 * 版    本：1.0
 * 创建日期：16/9/11
 * 描    述：
 * 修订历史：
 * ================================================
 */
public abstract class BaseFragment<P extends BasePresenter, VB extends ViewDataBinding> extends SwipeBackFragment {

    private boolean isVisible = true;                  //是否可见状态
    private boolean isPrepared;                 //标志位，View已经初始化完成。
    private boolean isFirstLoad = true;         //是否第一次加载
    protected LayoutInflater inflater;
    protected Activity act;
    private View view;

    protected VB mB;
    public P mPresenter;
    private TwinklingRefreshLayout refreshLayout;
    protected int pagerNumber = 1;//网络请求默认第一页

    private LoadingLayout vLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.d("onCreateView");
        this.inflater = inflater;
        isFirstLoad = true;

        View rootView = getLayoutInflater().inflate(this.bindLayout(), null, false);
        mB = DataBindingUtil.bind(rootView);
        isPrepared = true;
        // 初始化参数
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        this.view = rootView;
        mPresenter = TUtil.getT(this, 0);
        if (mPresenter != null) {
            mPresenter.act = this.getActivity();
            this.initPresenter();
        }
        initParms(bundle);
        initView(rootView);
        vLoading = view.findViewById(R.id.loadinglayout);
        return attachToSwipeBack(rootView);
    }

    //简单页面无需mvp就不用管此方法即可,完美兼容各种实际场景的变通
    public abstract void initPresenter();

    protected void setSofia(boolean isFullScreen) {
        if (!isFullScreen){
            ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(true).init();
        }else {

        }
    }


    protected abstract void initParms(Bundle bundle);

    protected abstract int bindLayout();

    protected abstract void initView(View view);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("onCreate");
        act = getActivity();
    }

    protected void showToast(final String str) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                ToastUtils.setGravity(Gravity.CENTER, 0, 0);
//                ToastUtils.showLong(str);
                ToastUtils.showShort(str);
            }
        });
    }


    public void onError(Throwable e, String errorName) {
        errorText(e, errorName);
    }
    public void onError(Throwable e) {
        errorText(e, null);
    }
    private void errorText(Throwable e, String errorName){
        if (null != e) {
            LogUtils.e(e.getMessage(), errorName);
            showToast(e.getMessage());
            if (refreshLayout != null){
                refreshLayout.finishRefreshing();
                refreshLayout.finishLoadmore();
                showError();
            }
        }else{
            LogUtils.e("请求之外Throwable,断点");
        }
    }


    private final int handler_load = 0;
    private final int handler_hide = 1;
    private final int handler_empty = 2;
    private final int handler_error = 3;
    private final int handler_no_network = 4;
    private final int handler_loadData = 5;
    private final int handler_success = 6;
    private final int SDK_PAY_FLAG = 7;

    private ProgressDialog dialog;

    public void showLoading() {
        mHandler.sendEmptyMessage(handler_load);
    }

    public void hideLoading() {
        mHandler.sendEmptyMessage(handler_hide);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        showToast("支付成功");
                        LogUtils.e("支付成功");
                        EventBus.getDefault().post(new PayInEvent());
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        showToast("支付取消");
                    }
                    break;
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
                    if (vLoading != null) {
                        vLoading.showContent();
                    }
                    break;
                case handler_empty:
                    if (vLoading != null) {
                        vLoading.showEmpty();
                    }
                    break;
                case handler_error:
                    if (vLoading != null) {
                        vLoading.showError();
                    }
                    break;
                case handler_loadData:
                    if (vLoading != null) {
                        vLoading.showLoading();
                    }
                    break;
            }
        }
    };

    public void showLoadDataing() {
        mHandler.sendEmptyMessage(handler_loadData);
    }

    public void showLoadEmpty() {
        mHandler.sendEmptyMessage(handler_empty);
    }

    private void showError(){
        ((BaseActivity)act).showError();
    }

    protected void setRefreshLayout(TwinklingRefreshLayout refreshLayout, RefreshListenerAdapter listener) {
//        ProgressLayout headerView = new ProgressLayout(act);
//        refreshLayout.setHeaderView(headerView);
//        refreshLayout.setOverScrollRefreshShow(false);
        ProgressLayout header = new ProgressLayout(act);
        refreshLayout.setHeaderView(header);
        refreshLayout.setFloatRefresh(true);
        refreshLayout.setOverScrollRefreshShow(false);
        refreshLayout.setHeaderHeight(140);
        refreshLayout.setMaxHeadHeight(240);
        refreshLayout.setOverScrollHeight(200);
        refreshLayout.setOnRefreshListener(listener);
        this.refreshLayout = refreshLayout;
    }

    /**
     *  是否有更多
     * @param listSize
     * @param totalRow
     * @param refreshLayout
     */
    protected void setRefreshLayoutMode(int listSize, int totalRow, TwinklingRefreshLayout refreshLayout) {
        if (listSize == totalRow) {
            refreshLayout.setEnableLoadmore(false);
        } else {
            refreshLayout.setEnableLoadmore(true);
        }
    }

    protected void setRefreshLayout(final int pagerNumber, final TwinklingRefreshLayout refreshLayout) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pagerNumber == 1) {
                    refreshLayout.finishRefreshing();
                } else {
                    refreshLayout.finishLoadmore();
                }
            }
        }, 300);

    }


    protected void setOnRightClickListener() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LogUtils.d("onAttach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.d("onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtils.d("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.d("onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.d("onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.d("onDestroyView");
    }

    @Override
    public void onDestroy() {
//        hideLoading();
        ((BaseActivity)act).dispose();
        super.onDestroy();
        LogUtils.d("onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LogUtils.d("onDetach");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setRecyclerViewType(RecyclerView recyclerView){
        recyclerView.setLayoutManager(new LinearLayoutManager(act));
        setRecyclerView(recyclerView, R.color.white);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setRecyclerViewType(RecyclerView recyclerView, int baColor){
        recyclerView.setLayoutManager(new LinearLayoutManager(act));
        setRecyclerView(recyclerView, baColor);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setRecyclerViewGridType(RecyclerView recyclerView, int spanCount, int width, int height, int color){
        recyclerView.setLayoutManager(new GridLayoutManager(act, spanCount));
        recyclerView.addItemDecoration(new GridDividerItemDecoration(width, height, ContextCompat.getColor(act,color)));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setRecyclerViewGridType(RecyclerView recyclerView, int spanCount, int width, int height){
        recyclerView.setLayoutManager(new GridLayoutManager(act, spanCount));
        recyclerView.addItemDecoration(new GridDividerItemDecoration(width, height, ContextCompat.getColor(act,R.color.white)));
    }

    private void setRecyclerView(RecyclerView recyclerView, int baColor){
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(ContextCompat.getColor(act,baColor));
    }

    protected void setTitle(String title) {
        title(title, null, -1, true);
    }
    protected void setTitle(String title, int img) {
        title(title, null, img, true);
    }
    protected void setTitle(String title, String right) {
        title(title,  right, -1, true);
    }
    protected void setTitle(String title, boolean isBack) {
        title(title,  null, -1, isBack);
    }
    protected void setTitle(String title, String right, boolean isBack) {
        title(title, right, -1, isBack);
    }

    protected void setTitleTransparent(String title, boolean back){
        setTitle(title);
        view.findViewById(R.id.top_view).setVisibility(View.GONE);
        view.findViewById(R.id.title_bar).setBackgroundColor(act.getColor(R.color.transparent));
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (back){
            toolbar.setNavigationIcon(null);
        }
        toolbar.setBackgroundColor(act.getColor(R.color.transparent));
    }

    private void title(String title, String rightText, int img, boolean isBack) {
        setSofia(false);
        final AppCompatActivity mAppCompatActivity = (AppCompatActivity) act;
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        TextView topTitle = view.findViewById(R.id.top_title);
        TextView topRight = view.findViewById(R.id.top_right);
        FrameLayout topRightFy = view.findViewById(R.id.top_right_fy);
        //需要调用该函数才能设置toolbar的信息
        mAppCompatActivity.setSupportActionBar(toolbar);
        mAppCompatActivity.getSupportActionBar().setTitle("");
        if (isBack){
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    act.onBackPressed();
                }
            });
        }else {
            toolbar.setNavigationIcon(null);
        }
        topTitle.setText(title);
        if (!StringUtils.isEmpty(rightText)){
            topRightFy.setVisibility(View.VISIBLE);
            topRight.setText(rightText);
        }else if (img != -1){
            topRightFy.setVisibility(View.VISIBLE);
            topRight.setBackgroundResource(img);
        }else {

        }
        topRightFy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOnRightClickListener();
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

    public void dispose() {
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

}
