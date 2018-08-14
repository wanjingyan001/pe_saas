package com.sogukj.pe.module.clockin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.bean.LocationRecordBean;

import java.util.ArrayList;

/**
 * Created by sogubaby on 2018/8/14.
 */

public class LocationAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<LocationRecordBean.LocationCellBean> mList;

    public LocationAdapter(Context mContext, ArrayList<LocationRecordBean.LocationCellBean> list) {
        context = mContext;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public LocationRecordBean.LocationCellBean getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_locate_record, null);
            holder.tvTime = view.findViewById(R.id.dutyOnTime);
            holder.tvLocate = view.findViewById(R.id.dutyOnLocate);
            holder.tvLocate = view.findViewById(R.id.dutyOnLocate);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        LocationRecordBean.LocationCellBean bean = getItem(position);

        String str1 = bean.getTime().substring(0, 5);
        String str2 = "定位打卡";
        SpannableString str = new SpannableString(str1 + "  " + str2);
        str.setSpan(new ForegroundColorSpan(Color.parseColor("#282828")), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        str.setSpan(new ForegroundColorSpan(Color.parseColor("#808080")), str1.length() + 2, str.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        holder.tvTime.setText(str);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.location_clock_neg);
        drawable.setBounds(0, 0, Utils.dpToPx(context, 10), Utils.dpToPx(context, 10));
        holder.tvLocate.setCompoundDrawables(drawable, null, null, null);
        holder.tvLocate.setText(bean.getPlace());
        return view;
    }

    final static class ViewHolder {
        TextView tvTime;
        TextView tvLocate;
    }
}
