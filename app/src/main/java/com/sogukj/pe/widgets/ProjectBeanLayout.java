package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sogukj.pe.R;


/**
 * Created by sogubaby on 2018/1/17.
 */
public class ProjectBeanLayout extends LinearLayout {

    private Context context;
    private ImageView icon;
    private TextView mTitle, mSubTitle;

    public ProjectBeanLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ProjectBeanLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ProjectBeanLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.tip_view_project, this);

        icon = findViewById(R.id.icon);
        mTitle = findViewById(R.id.title);
        mSubTitle = findViewById(R.id.subtitle);
    }

    /**
     * @param enabled----是否为灰色
     * @param tag-------1，2
     */
    public void setData(boolean enabled, int tag, String date) {
        if (enabled) {
            if (tag == 1) {
                Glide.with(context).load(R.drawable.zxzx_yes).into(icon);
                mTitle.setTextColor(Color.parseColor("#282828"));
                mSubTitle.setTextColor(Color.parseColor("#d19191"));
                mTitle.setText("最新资讯");
            } else {
                Glide.with(context).load(R.drawable.xmgz_yes).into(icon);
                mTitle.setTextColor(Color.parseColor("#282828"));
                mSubTitle.setTextColor(Color.parseColor("#97b075"));
                mTitle.setText("项目跟踪");
            }
        } else {
            if (tag == 1) {
                Glide.with(context).load(R.drawable.zxzx_no).into(icon);
                mTitle.setText("最新资讯");
            } else {
                Glide.with(context).load(R.drawable.xmgz_no).into(icon);
                mTitle.setText("项目跟踪");
            }
            mTitle.setTextColor(Color.parseColor("#a0a4aa"));
            mSubTitle.setTextColor(Color.parseColor("#dcdee6"));
        }
        mSubTitle.setText(date);
    }
}
