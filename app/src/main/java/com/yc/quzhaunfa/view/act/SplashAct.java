package com.yc.quzhaunfa.view.act;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.gyf.immersionbar.ImmersionBar;
import com.yc.quzhaunfa.R;
import com.yc.quzhaunfa.base.BaseActivity;
import com.yc.quzhaunfa.view.SplashFrg;

import me.yokeyword.fragmentation.SupportActivity;

/**
 * Created by Administrator on 2018/5/9.
 *  启动页
 */

public class SplashAct extends BaseActivity {


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
        ImmersionBar.with(this)
                .transparentBar()
                .init();
        if (findFragment(SplashFrg.class) == null) {
            loadRootFragment(R.id.fl_container, SplashFrg.newInstance());
        }
    }
}
