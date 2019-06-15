package com.yc.quzhaunfa.view;

import android.os.Bundle;
import android.view.View;

import com.yc.quzhaunfa.R;
import com.yc.quzhaunfa.base.BaseFragment;

/**
 * Created by wb  yyc
 * User: 501807647@qq.com
 * Date: 2019/6/16
 * Time: 1:11
 */
public class ThreeFrg extends BaseFragment {

    public static ThreeFrg newInstance() {
        Bundle args = new Bundle();
        ThreeFrg fragment = new ThreeFrg();
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
        return R.layout.f_three;
    }

    @Override
    protected void initView(View view) {

    }
}
