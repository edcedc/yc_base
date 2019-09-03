package com.yc.quzhaunfa.view;

import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.yc.quzhaunfa.R;
import com.yc.quzhaunfa.base.BaseFragment;
import com.yc.quzhaunfa.base.BasePresenter;
import com.yc.quzhaunfa.databinding.FOneBinding;
import com.yc.quzhaunfa.utils.ShareTool;

import java.util.Map;

public class OneFrg extends BaseFragment<BasePresenter, FOneBinding> {

    public static OneFrg newInstance() {
        Bundle args = new Bundle();
        OneFrg fragment = new OneFrg();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    protected void initParms(Bundle bundle) {

    }

    @Override
    protected int bindLayout() {
        return R.layout.f_one;
    }

    @Override
    protected void initView(View view) {
        setTitle("哈");
        mB.refreshLayout.startRefresh();
        mB.button.setOnClickListener(view1 -> ShareTool.getInstance(act).Authorization(new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                LogUtils.e(map);
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                LogUtils.e(throwable.getMessage());
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {

            }
        }));
    }
}
