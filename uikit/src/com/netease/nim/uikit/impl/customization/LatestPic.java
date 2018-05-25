package com.netease.nim.uikit.impl.customization;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.uikit.R;

/**
 * Created by admin on 2018/5/11.
 */

public class LatestPic extends PopupWindow {
    private ImageView pic;
    private Context context;

    public LatestPic(Context context, final View.OnClickListener listener) {
        this.context = context;
        View inflate = LayoutInflater.from(context).inflate(R.layout.layout_latestpic_popwindow, null);
        pic = (ImageView) inflate.findViewById(R.id.latestPic);
        this.setContentView(inflate);
        this.setWidth(dpToPx(72));
        this.setHeight(dpToPx(110));
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        inflate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onClick(v);
            }
        });
    }

    public int dpToPx( int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public void showImg(String path) {
        Glide.with(context)
                .load(path)
                .apply(new RequestOptions().centerCrop())
                .thumbnail(0.1f)
                .into(pic);
    }
}
