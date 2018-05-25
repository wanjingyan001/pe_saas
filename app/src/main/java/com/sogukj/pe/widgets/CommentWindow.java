package com.sogukj.pe.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.interf.CommentListener;

/**
 * Created by admin on 2017/12/8.
 */

public class CommentWindow extends PopupWindow {
    private Context context;
    private View inflate;
    private TextView cancel;
    private TextView confirm;
    private EditText commentEdt;
    private CommentListener listener;

    public CommentWindow(Context context, CommentListener listener) {
        super(context);
        this.context = context;
        init(context);
        this.listener = listener;
    }

    private void init(final Context context) {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_comment_window, null);
        cancel = inflate.findViewById(R.id.cancel);
        confirm = inflate.findViewById(R.id.confirm);
        commentEdt = inflate.findViewById(R.id.commentEdt);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeInput(context,commentEdt);
                commentEdt.setText("");
                dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.closeInput(context,commentEdt);
                listener.confirmListener(commentEdt.getText().toString());
                commentEdt.setText("");
                dismiss();
            }
        });
        this.setContentView(inflate);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.ShareDialogAnim);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setOutsideTouchable(true);
    }

    private void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);

        backgroundAlpha((Activity) context, 0.4f);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        backgroundAlpha((Activity) context, 1f);
    }
}
