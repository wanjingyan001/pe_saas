package com.sogukj.pe.module.clockin.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sogukj.pe.Extras;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.DateUtils;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.baselibrary.widgets.DotView;
import com.sogukj.pe.bean.LocationRecordBean;
import com.sogukj.pe.bean.UserBean;
import com.sogukj.pe.module.approve.ApproveDetailActivity;
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity;
import com.sogukj.pe.peUtils.Store;

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
            holder.tvClockTime = view.findViewById(R.id.clockTime);
            holder.tvLocate = view.findViewById(R.id.locate);
            holder.tvRelate = view.findViewById(R.id.relate);
            holder.mRelative = view.findViewById(R.id.relative);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        int padLeft = Utils.dpToPx(context, 15);
        int padRight = Utils.dpToPx(context, 15);
        holder.mRelative.setPadding(padLeft, 0, padRight, 0);

        final LocationRecordBean.LocationCellBean bean = getItem(position);

        int stamp = bean.getTime();
        String dateStr = DateUtils.getTime24HDisplay(context, stamp);
        String str1 = dateStr.substring(11);

        String str2 = "定位打卡";
        SpannableString str = new SpannableString(str1 + "  " + str2);
        str.setSpan(new ForegroundColorSpan(Color.parseColor("#282828")), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        str.setSpan(new ForegroundColorSpan(Color.parseColor("#282828")), str1.length() + 2, str.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        holder.tvClockTime.setText(str);

        holder.tvLocate.setText(bean.getPlace());
        if (bean.getSid() == null) {
            holder.tvRelate.setVisibility(View.GONE);
        } else {
            holder.tvRelate.setVisibility(View.VISIBLE);
            holder.tvRelate.setText("关联审批：" + bean.getAdd_time().split(" ")[0] + "  " + bean.getTitle());
            holder.tvRelate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean.getApprove_type() == 1) {
                        //老审批
                        LeaveBusinessApproveActivity.Companion.start((Activity) context, bean.getSid(), bean.getStype());
                    } else {
                        //新审批
                        Intent intent = new Intent(context, ApproveDetailActivity.class);
                        intent.putExtra("ext.id", bean.getSid());
                        intent.putExtra("ext.flag", 1);
                        context.startActivity(intent);
                    }

                }
            });
        }
        return view;
    }

    final static class ViewHolder {
        TextView tvClockTime, tvLocate, tvRelate;
        RelativeLayout mRelative;
    }
}
