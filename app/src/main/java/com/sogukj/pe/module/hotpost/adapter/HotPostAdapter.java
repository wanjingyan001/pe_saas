package com.sogukj.pe.module.hotpost.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.HotPostInfo;

import java.util.List;

/**
 * Created by CH-ZH on 2018/9/6.
 */
public class HotPostAdapter extends BaseLoadingAdapter<HotPostInfo> {
    private Context context;
    private List<HotPostInfo> hotPostInfos;
    private OnHeaderClickListener onHeaderClickListener;
    private OnItemClickListener onItemClickListener;
    public HotPostAdapter(Context context,RecyclerView recyclerView, List<HotPostInfo> hotPostInfos) {
        super(recyclerView, hotPostInfos);
        this.context = context;
        this.hotPostInfos = hotPostInfos;
    }

    public void addDatas(List<HotPostInfo> hotPostInfos){
        this.hotPostInfos = hotPostInfos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_design, parent, false);
        return new NormalViewHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position) {
        HotPostInfo hotPostInfo = hotPostInfos.get(position);
        NormalViewHolder normalHolder = (NormalViewHolder) holder;
        if(null != hotPostInfo) {
            normalHolder.tv_title.setText(hotPostInfo.getTitle());
            if(null != hotPostInfo.getImage()) {
                Glide.with(context).load(hotPostInfo.getImage())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(6))).into(normalHolder.iv_image);
            }
        }
        if(null != onItemClickListener) {
            normalHolder.view.setOnClickListener(v -> {
                onItemClickListener.onItemClick(position);
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        HotPostInfo hotPostInfo = hotPostInfos.get(0);
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        if(null != hotPostInfo) {
            headerHolder.tv_title.setText(hotPostInfo.getTitle());
            if(null != hotPostInfo.getImage()) {
                Glide.with(context).load(hotPostInfo.getImage())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(6))).into(headerHolder.iv_image);
            }
        }
        if(null != onHeaderClickListener) {
            headerHolder.view.setOnClickListener(v -> {
                onHeaderClickListener.onHeaderClick(position);
            });
        }
    }

    public class NormalViewHolder extends RecyclerView.ViewHolder{
        public View view;
        public ImageView iv_image;
        public TextView tv_title;
        public NormalViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            iv_image = itemView.findViewById(R.id.iv_image);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        public View view;
        public ImageView iv_image;
        public TextView tv_title;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            iv_image = itemView.findViewById(R.id.iv_image);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }

    public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
        this.onHeaderClickListener = onHeaderClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnHeaderClickListener{
        void onHeaderClick(int position);
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}
