package com.yc.quzhaunfa.view;

import android.os.Bundle;
import android.view.View;

import com.yc.quzhaunfa.R;
import com.yc.quzhaunfa.base.BaseFragment;
import com.yc.quzhaunfa.base.BasePresenter;
import com.yc.quzhaunfa.databinding.FOneBinding;

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

    }
}
