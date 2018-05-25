package com.sogukj.pe.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sogukj.pe.R;


/**
 * Created by sogubaby on 2018/1/3.
 */

public class TipsViewMain2 extends LinearLayout {

    private CircleImageView icon;
    private TextView mTitle;

    public TipsViewMain2(Context context) {
        super(context);
        init(context, null);
    }

    public TipsViewMain2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TipsViewMain2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.tip_view_main2, this);

        icon = findViewById(R.id.icon);
        mTitle = findViewById(R.id.title);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.title_main);
        int icon_id = ta.getResourceId(R.styleable.title_main_icon_main, 0);
        icon.setImageResource(icon_id);
        String title = ta.getString(R.styleable.title_main_title);
        mTitle.setText(title);
        ta.recycle();
    }
}
