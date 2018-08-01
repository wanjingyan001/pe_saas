package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.UserBean;
import com.sogukj.pe.peUtils.MyGlideUrl;

import java.util.ArrayList;

/**
 * Created by sogubaby on 2018/7/31.
 */

public class MyCSAdapter extends BaseAdapter implements Filterable {
    private Context mContext;
    private ArrayList<UserBean> oriData;
    private ArrayList<UserBean> sortData;

    public MyCSAdapter(Context context, ArrayList<UserBean> list) {
        mContext = context;
        oriData = new ArrayList<UserBean>(list);
        sortData = new ArrayList<UserBean>();
    }

    @Override
    public int getCount() {
        return sortData.size();
    }

    @Override
    public Object getItem(int position) {
        return sortData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.send_item, null);
            viewHolder.icon = (CircleImageView) convertView.findViewById(R.id.icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final UserBean bean = sortData.get(position);
        if (bean.getUrl() == null || bean.getUrl().isEmpty()) {
            viewHolder.icon.setChar(bean.getName().charAt(0));
        } else {
            Glide.with(mContext)
                    .load(new MyGlideUrl(bean.getUrl()))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            viewHolder.icon.setChar(bean.getName().charAt(0));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            viewHolder.icon.setImageDrawable(resource);
                            return false;
                        }
                    }).into(viewHolder.icon);
        }
        viewHolder.name.setText(bean.getName());
        return convertView;
    }

    class ViewHolder {
        CircleImageView icon;
        TextView name;
    }

    private MyFilter mFilter;

    /**
     * ""    "1"    "2"
     *
     * @return
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }

    class MyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = new ArrayList<UserBean>(oriData);
                results.count = oriData.size();
            } else {
                // "1"    "2"
                String read = constraint.toString();
                ArrayList<UserBean> values = new ArrayList<UserBean>();
                for (int i = 0; i < oriData.size(); i++) {
                    UserBean bean = oriData.get(i);
                    try {
                        if (read.equals(bean.is_read().toString())) {
                            values.add(bean);
                        }
                    } catch (NullPointerException e) {
                        //bean.is_read().toString()  null转化
                    }
                }
                results.values = values;
                results.count = values.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            sortData.clear();
            sortData.addAll((ArrayList<UserBean>) results.values);
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
