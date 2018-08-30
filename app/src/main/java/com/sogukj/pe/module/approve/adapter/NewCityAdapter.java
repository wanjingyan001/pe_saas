package com.sogukj.pe.module.approve.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.bean.CityBean;

import java.util.List;

/**
 * Created by CH-ZH on 2018/8/30.
 */
public class NewCityAdapter extends RecyclerView.Adapter<NewCityAdapter.ViewHolder>{
    protected Context mContext;
    protected List<CityBean> mDatas;
    protected LayoutInflater mInflater;
    private OnCityItemClickListener onCityItemClickListener;
    public NewCityAdapter(Context mContext, List<CityBean> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        mInflater = LayoutInflater.from(mContext);
    }

    public List<CityBean> getDatas() {
        return mDatas;
    }

    public NewCityAdapter setDatas(List<CityBean> datas) {
        mDatas = datas;
        return this;
    }

    @Override
    public NewCityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.city_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final NewCityAdapter.ViewHolder holder, final int position) {
        final CityBean cityBean = mDatas.get(position);
        holder.tvCity.setText(cityBean.getCity());
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != onCityItemClickListener) {
                    onCityItemClickListener.onCityItemClick(cityBean.getCity(),position);
                }
            }
        });
        if(cityBean.isSeclected()) {
            holder.tvCity.setTextColor(Color.parseColor("#1787fb"));
            holder.content.setBackgroundColor(Color.parseColor("#fff8f9fe"));
        }else{
            holder.tvCity.setTextColor(Color.parseColor("#282828"));
            holder.content.setBackgroundColor(Color.parseColor("#ffffffff"));
        }
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCity;
        View content;
        public ViewHolder(View itemView) {
            super(itemView);
            tvCity = (TextView) itemView.findViewById(R.id.city);
            content = itemView.findViewById(R.id.root);
        }
    }

    public void setOnCityItemClickListener(OnCityItemClickListener onCityItemClickListener) {
        this.onCityItemClickListener = onCityItemClickListener;
    }

    public interface OnCityItemClickListener{
        void onCityItemClick(String city,int position);
    }
}
