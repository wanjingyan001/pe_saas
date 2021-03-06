package com.sogukj.pe.module.approve.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.bean.CityArea.City;

import java.util.ArrayList;

/**
 * Created by sogubaby on 2018/3/20.
 */

public class HistoryAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<City> city;

    public HistoryAdapter(Context context) {
        this.context = context;
        this.city = new ArrayList<City>();
    }

    public ArrayList<City> getData() {
        return city;
    }
    public void setData(ArrayList<City> city){
        this.city = city;
    }

    @Override
    public int getCount() {
        return city.size();
    }

    @Override
    public City getItem(int position) {
        return city.get(position);
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
            view = LayoutInflater.from(context).inflate(R.layout.history_item, null);
            holder.tvCity = view.findViewById(R.id.city);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvCity.setText(city.get(position).getName());
        return view;
    }

    final static class ViewHolder {
        TextView tvCity;
    }
}
