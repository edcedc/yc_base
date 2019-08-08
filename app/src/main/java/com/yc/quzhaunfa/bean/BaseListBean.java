package com.yc.quzhaunfa.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yc on 2017/8/17.
 */

public class BaseListBean<T> implements Serializable {

    private int totalCount;
    private int totalPage;
    private int page;
    private int size;
    private List<T> content;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<T> getList() {
        return content;
    }

    public void setList(List<T> content) {
        this.content = content;
    }
}
