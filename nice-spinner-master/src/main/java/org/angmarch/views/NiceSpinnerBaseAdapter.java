package org.angmarch.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/*
 * Copyright (C) 2015 Angelo Marchesin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressWarnings("unused")
public abstract class NiceSpinnerBaseAdapter<T> extends BaseAdapter {

    private final SpinnerTextFormatter spinnerTextFormatter;

    private int textColor;
    private int backgroundSelector;

    int selectedIndex;

    NiceSpinnerBaseAdapter(Context context, int textColor, int backgroundSelector,
                           SpinnerTextFormatter spinnerTextFormatter) {
        this.spinnerTextFormatter = spinnerTextFormatter;
        this.backgroundSelector = backgroundSelector;
        this.textColor = textColor;
    }

    private int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        TextView textView;
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_list_item, parent, false);
            holder.textView = (TextView) convertView.findViewById(R.id.text_view_spinner);
            holder.tvMark = (TextView) convertView.findViewById(R.id.tvMark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.textView.setBackground(ContextCompat.getDrawable(context, backgroundSelector));
            }
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.shape);
            drawable.setBounds(0, 0, dpToPx(context, 15), dpToPx(context, 15));
            holder.textView.setCompoundDrawables(drawable, null, null, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(getItem(position).toString().isEmpty()){
            holder.textView.setVisibility(View.GONE);
            holder.tvMark.setVisibility(View.VISIBLE);
        } else {
            holder.textView.setVisibility(View.VISIBLE);
            holder.tvMark.setVisibility(View.GONE);
            holder.textView.setText(spinnerTextFormatter.format(getItem(position).toString()));
        }

        //textView.setTextColor(textColor);
        return convertView;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public abstract T getItemInDataset(int position);

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public abstract T getItem(int position);

    @Override public abstract int getCount();

    static class ViewHolder {
        TextView textView, tvMark;

    }
}
