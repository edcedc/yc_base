package com.yc.quzhaunfa.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yc.quzhaunfa.R;
import com.yc.quzhaunfa.base.BaseListViewAdapter;
import com.yc.quzhaunfa.bean.DataBean;

import java.util.List;

/**
 * Created by edison on 2018/5/5.
 */

public class MeAdapter extends BaseListViewAdapter<DataBean> {

    private boolean isImg = false;

    public MeAdapter(Context act, List listBean) {
        super(act, listBean);
    }
    public MeAdapter(Context act, List listBean, boolean isImg) {
        super(act, listBean);
        this.isImg = isImg;
    }

    private OnClick click;
    public void setClick(OnClick click){
        this.click = click;
    }

    @Override
    protected View getCreateVieww(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(act, R.layout.i_list, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final DataBean bean = listBean.get(position);
        if (bean.getImg() != 0){
            viewHolder.ivImg.setVisibility(View.VISIBLE);
            viewHolder.ivImg.setImageResource(bean.getImg());
        }
        viewHolder.tvName.setText(bean.getName());
        viewHolder.ivBack.setVisibility(isImg == true ? View.VISIBLE : View.GONE);

        viewHolder.tvContent.setText(bean.getContent());

        if (listBean.size() - 1 == position){
            viewHolder.view.setVisibility(View.GONE);
        }else {
            viewHolder.view.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    public interface OnClick{
        void onClick(int position, String text);
    }


    class ViewHolder {

        ImageView ivImg;
        TextView tvName;
        TextView tvContent;
        View view;
        View ivBack;

        public ViewHolder(View itemView) {
            ivImg = itemView.findViewById(R.id.iv_img);
            tvName = itemView.findViewById(R.id.tv_name);
            view = itemView.findViewById(R.id.view);
            ivBack = itemView.findViewById(R.id.iv_back);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }

}
