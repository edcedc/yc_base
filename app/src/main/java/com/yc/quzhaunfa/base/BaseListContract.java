package com.yc.quzhaunfa.base;

/**
 * 作者：yc on 2018/9/6.
 * 邮箱：501807647@qq.com
 * 版本：v1.0
 */

public interface BaseListContract {

    interface View extends IBaseListView{

    }

    abstract class Presenter extends BasePresenter<View>{

        public abstract void onRequest(String url, int pagerNumber);

        public abstract void onRequest(String url);

    }

}
