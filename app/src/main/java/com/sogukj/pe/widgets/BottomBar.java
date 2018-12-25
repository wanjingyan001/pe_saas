package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;

import java.util.ArrayList;

/**
 * Created by sogubaby on 2018/6/9.
 */

public class BottomBar extends LinearLayout {

    public BottomBar(Context context) {
        super(context);
        init(context);
    }

    public BottomBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BottomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Context mContext;
    private LayoutInflater inflater;
    private LinearLayout root;

    private void init(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.bottom_bar_layout, this);
        root = findViewById(R.id.root);
        setOrientation(HORIZONTAL);
    }

    public BottomBar setActiveColor(@ColorInt int color) {
        mActiveColor = color;
        return this;
    }

    int mInActiveColor = Color.parseColor("#D9D9D9");
    int mActiveColor = Color.parseColor("#4390F0");

    public BottomBar setInActiveColor(@ColorInt int color) {
        mInActiveColor = color;
        return this;
    }

    public void setDataSource(ArrayList<String> mTitle, @DrawableRes ArrayList<Integer> mResIds) {
        if (mTitle.size() != mResIds.size()) {
            return;
        }
        root.removeAllViews();
        for (int i = 0; i < mTitle.size(); ++i) {
            LinearLayout item = (LinearLayout) inflater.inflate(R.layout.bottom_bar_item, null);
            ImageView iv = item.findViewById(R.id.icon);
            iv.setImageResource(mResIds.get(i));
            TextView tv = item.findViewById(R.id.title);
            tv.setText(mTitle.get(i));

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, Utils.dpToPx(mContext, 55), 1.0f);
            item.setLayoutParams(params);

            root.addView(item);
        }
    }

    public void update(ArrayList<Boolean> actives) {
        this.actives = actives;
        if (root.getChildCount() != actives.size()) {
            return;
        }
        for (int i = 0; i < root.getChildCount(); ++i) {
            boolean isActive = actives.get(i);
            LinearLayout item = (LinearLayout) root.getChildAt(i);
            ImageView iv = (ImageView) item.getChildAt(0);
            TextView tv = (TextView) item.getChildAt(1);
            if (isActive) {
                iv.clearColorFilter();
                tv.setTextColor(mActiveColor);
            } else {
                iv.setColorFilter(mInActiveColor, PorterDuff.Mode.SRC_ATOP);//对setImageResource有用
                tv.setTextColor(mInActiveColor);
            }
        }
    }

    private ArrayList<Boolean> actives;

    public ArrayList<Boolean> getActives() {
        return actives;
    }

    public LinearLayout getRoot() {
        return root;
    }
}
