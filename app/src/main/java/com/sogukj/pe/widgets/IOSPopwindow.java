package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sogukj.pe.R;


/**
 * Created by admin on 2017/11/30.
 */

public class IOSPopwindow extends PopupWindow implements View.OnClickListener {

    private View inflate;
    private TextView select1;
    private TextView select2;
    private OnItemClickListener mListener;

    public IOSPopwindow(Context context) {
        super(context);
        init(context);
        setPopWindow();
    }

    public IOSPopwindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setPopWindow();
    }

    private void init(Context context) {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_ios_popwindow, null);
        select1 = inflate.findViewById(R.id.typeSelect1);
        select2 = inflate.findViewById(R.id.typeSelect2);
        select1.setOnClickListener(this);
        select2.setOnClickListener(this);
    }

    private void setPopWindow() {
        this.setContentView(inflate);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.ShareDialogAnim);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setOutsideTouchable(true);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            dismiss();
            switch (v.getId()) {
                case R.id.typeSelect1:
                    mListener.setOnItemClick(v, 1);
                    break;
                case R.id.typeSelect2:
                    mListener.setOnItemClick(v, 2);
                    break;
                default:
                    break;
            }
        }
    }

    public interface OnItemClickListener {
        void setOnItemClick(View v, int select);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
}
