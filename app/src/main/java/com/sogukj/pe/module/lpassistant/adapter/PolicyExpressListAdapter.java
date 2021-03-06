package com.sogukj.pe.module.lpassistant.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.PlListInfos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CH-ZH on 2018/9/5.
 */
public class PolicyExpressListAdapter extends BaseAdapter {
    private Context context;
    private List<PlListInfos> infos = new ArrayList<>();
    public PolicyExpressListAdapter(Context context) {
        this.context = context;
    }

    public List<PlListInfos> getInfos() {
        return infos;
    }

    public void setInfos(List<PlListInfos> infos) {
        this.infos = infos;
    }

    public void loadMoreInfos(List<PlListInfos> moreData){
        if(null != moreData) {
            infos.addAll(moreData);
        }
    }
    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(null == convertView) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.pl_item,null);
            holder.tv_title1 = convertView.findViewById(R.id.tv_title1);
            holder.tv_tag1 = convertView.findViewById(R.id.tv_tag1);
            holder.tv_time = convertView.findViewById(R.id.tv_time);
            holder.iv_image = convertView.findViewById(R.id.iv_image);
            holder.tv_title2 = convertView.findViewById(R.id.tv_title2);
            holder.tv_tag2 = convertView.findViewById(R.id.tv_tag2);
            holder.tv_time2 = convertView.findViewById(R.id.tv_time2);
            holder.rl_normal = convertView.findViewById(R.id.rl_normal);
            holder.rl_image = convertView.findViewById(R.id.rl_image);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        PlListInfos plListInfos = infos.get(position);
        fitData(holder,plListInfos);

        return convertView;
    }

    private void fitData(ViewHolder holder, PlListInfos plListInfos) {
        if(null != plListInfos.getImg() && !"".equals(plListInfos.getImg())) {
            holder.rl_normal.setVisibility(View.GONE);
            holder.rl_image.setVisibility(View.VISIBLE);
        }else{
            holder.rl_normal.setVisibility(View.VISIBLE);
            holder.rl_image.setVisibility(View.GONE);
        }

        if(null != plListInfos.getTitle()) {
            holder.tv_title1.setText(plListInfos.getTitle());
            holder.tv_title2.setText(plListInfos.getTitle());
        }

        if(null != plListInfos.getImg()) {
            Glide.with(context).load(plListInfos.getImg()).apply(RequestOptions.bitmapTransform(new RoundedCorners(10)))
                    .thumbnail(0.5f).into(holder.iv_image);
        }

        if(null != plListInfos.getTime()) {
            holder.tv_time.setText(plListInfos.getTime());
            holder.tv_time2.setText(plListInfos.getTime());
        }

        if(null != plListInfos.getSource()) {
            holder.tv_tag1.setText(plListInfos.getSource());
            holder.tv_tag2.setText(plListInfos.getSource());
        }
    }
    
    class ViewHolder{
        TextView tv_title1;
        TextView tv_tag1;
        TextView tv_time;
        ImageView iv_image;
        TextView tv_title2;
        TextView tv_tag2;
        TextView tv_time2;
        RelativeLayout rl_normal;
        RelativeLayout rl_image;
    }

}
