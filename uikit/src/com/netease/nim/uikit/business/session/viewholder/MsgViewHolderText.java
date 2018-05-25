package com.netease.nim.uikit.business.session.viewholder;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;

/**
 * Created by zhoujianghua on 2015/8/4.
 */
public class MsgViewHolderText extends MsgViewHolderBase {

    protected TextView bodyTextView;

    public MsgViewHolderText(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_text;
    }

    @Override
    protected void inflateContentView() {
        bodyTextView = findViewById(R.id.nim_message_item_text_body);
    }

    @Override
    protected void bindContentView() {
        layoutDirection();
        bodyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick();
            }
        });
        MoonUtil.identifyFaceExpression(NimUIKit.getContext(), bodyTextView, getDisplayText(), ImageSpan.ALIGN_BOTTOM);
        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bodyTextView.setOnLongClickListener(longClickListener);
        setMaxWidth();
    }

    private void layoutDirection() {
        if (isReceivedMessage()) {
            bodyTextView.setBackgroundResource(NimUIKitImpl.getOptions().messageLeftBackground);
            bodyTextView.setTextColor(Color.parseColor("#282828"));
            bodyTextView.setPadding(ScreenUtil.dip2px(15), ScreenUtil.dip2px(8), ScreenUtil.dip2px(10), ScreenUtil.dip2px(8));
        } else {
            bodyTextView.setBackgroundResource(NimUIKitImpl.getOptions().messageRightBackground);
            bodyTextView.setTextColor(Color.parseColor("#282828"));
            bodyTextView.setPadding(ScreenUtil.dip2px(10), ScreenUtil.dip2px(8), ScreenUtil.dip2px(15), ScreenUtil.dip2px(8));
        }

    }

    private void setMaxWidth() {
        TextPaint paint = bodyTextView.getPaint();
        float textStr = paint.measureText(getDisplayText());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bodyTextView.getLayoutParams();
        if (textStr > getMaxEdge()) {
            params.width = getMaxEdge();
        }else {
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
        bodyTextView.setLayoutParams(params);
    }

    public static int getMaxEdge() {
        return (int) (0.72 * ScreenUtil.screenMin);
    }

    @Override
    protected int leftBackground() {
        return 0;
    }

    @Override
    protected int rightBackground() {
        return 0;
    }

    protected String getDisplayText() {
        return message.getContent().trim();
    }
}
