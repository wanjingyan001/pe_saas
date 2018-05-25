package com.sogukj.pe.module.im;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sogukj.pe.R;

/**
 * Created by admin on 2018/2/1.
 */

public class IMDialog extends Dialog implements View.OnClickListener {

    private TextView title;
    private TextView item1;
    private TextView item2;
    private TextView cancel;
    private IMItemClickListener listener;

    public IMDialog(@NonNull Context context) {
        super(context, R.style.ios_bottom_dialog);
        setContentView(R.layout.layout_ios_dialog);
        initView();
    }

    public void setTitle(String titleStr) {
        title.setText(titleStr);
    }

    public void setOnItemClickListener(IMItemClickListener listener) {
        this.listener = listener;
    }

    private void initView() {
        title = findViewById(R.id.title);
        item1 = findViewById(R.id.item1);
        item2 = findViewById(R.id.item2);
        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMDialog.this.dismiss();
            }
        });
        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        //点击空白区域可以取消dialog
        this.setCanceledOnTouchOutside(true);
        //点击back键可以取消dialog
        this.setCancelable(true);
        Window window = this.getWindow();
        //让Dialog显示在屏幕的底部
        window.setGravity(Gravity.BOTTOM);
        //设置窗口出现和窗口隐藏的动画
        window.setWindowAnimations(R.style.ios_bottom_dialog_anim);
        //设置BottomDialog的宽高属性
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            switch (v.getId()) {
                case R.id.item1:
                    listener.itemClick(1);
                    break;
                case R.id.item2:
                    listener.itemClick(2);
                    break;
                default:
                    break;
            }
        }
    }


   public interface IMItemClickListener {
        void itemClick(int position);
    }
}
