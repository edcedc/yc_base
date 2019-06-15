package com.yc.quzhaunfa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yc.quzhaunfa.base.BaseActivity;
import com.yc.quzhaunfa.view.MainFrg;

public class MainActivity extends BaseActivity {


    @Override
    protected void initPresenter() {

    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initParms(Bundle bundle) {

    }

    @Override
    protected void initView() {
        if (findFragment(MainFrg.class) == null) {
            loadRootFragment(R.id.fl_container, MainFrg.newInstance());
        }
    }
}
