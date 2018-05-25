package com.sogukj.pe.module.calendar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.bean.KeyNode;
import com.sogukj.pe.bean.TodoYear;

import java.util.List;

/**
 * Created by admin on 2017/12/7.
 */

public class CompleteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int year = 1, info = 2;
    private Context context;
    private List<Object> data;

    public CompleteAdapter(Context context, List<Object> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == year) {
            return new YearHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_year, parent, false));
        } else if (viewType == info) {
            return new InfoHolder(LayoutInflater.from(context).inflate(R.layout.item_complete_info, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        try {
            Object o = data.get(position);
            if (holder instanceof YearHolder) {
                TodoYear year = (TodoYear) o;
                ((YearHolder) holder).Year.setText(year.getYear());
            } else if (holder instanceof InfoHolder) {
                KeyNode info = (KeyNode) o;
                if (info.getFinish_time()!=null){
                    ((InfoHolder) holder).completeTime.setText(info.getFinish_time());
                    ((InfoHolder) holder).completeInfo.setText(info.getTitle());
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = data.get(position);
        if (o instanceof TodoYear) {
            return year;
        } else if (o instanceof KeyNode) {
            return info;
        }else {
            return info;
        }
    }

    class YearHolder extends RecyclerView.ViewHolder {
        private TextView Year;

        public YearHolder(View itemView) {
            super(itemView);
            Year = itemView.findViewById(R.id.yearTv);
        }
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        private TextView completeInfo;
        private TextView completeTime;

        public InfoHolder(View itemView) {
            super(itemView);
            completeInfo = itemView.findViewById(R.id.completeInfo);
            completeTime = itemView.findViewById(R.id.completeTime);

        }
    }
}
