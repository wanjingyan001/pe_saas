package com.sogukj.pe.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sogukj.pe.R;

/**
 * Created by CH-ZH on 2018/8/18.
 */
public class PhoneNotifDialog extends Dialog {
    private TextView tv_detail;
    private TextView tv_close;
    private Context context;
    public PhoneNotifDialog(Context context) {
        super(context, R.style.AppTheme_Dialog);
        this.context = context;
        setContentView(R.layout.layout_phone_dialog);
        tv_detail = findViewById(R.id.tv_detail);

        tv_close = findViewById(R.id.tv_close);

        tv_detail.setOnClickListener(view -> {
            //查看详情
            invokeSetting();
        });

        tv_close.setOnClickListener(v -> {
            goneLoadding();
        });
    }

    private void invokeSetting() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        if(!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showLoadding(){
        Window window = this.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = -1;
        params.y = -1;
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
        onWindowAttributesChanged(params);
        setCanceledOnTouchOutside(false);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        this.show();
    }

    public void goneLoadding(){
        if(this.isShowing()) {
            this.dismiss();
        }
    }
}
