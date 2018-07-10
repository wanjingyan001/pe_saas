package com.sogukj.pe.widgets;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.utils.PickerViewAnimateUtil;
import com.sogukj.pe.R;

/**
 * Created by sogubaby on 2018/7/8.
 */

public class PayView extends View {

    private Context mContext;
    private ViewGroup decorView;
    private ViewGroup rootView;

    private ImageView mImgDelete;
    private TextView mTvProj, mTvZhengX;
    private LinearLayout mBgPic, mTelephone, mTaoCan;

    private Animation getInAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(Gravity.BOTTOM, true);
        return AnimationUtils.loadAnimation(mContext, res);
    }

    private Animation getOutAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(Gravity.BOTTOM, false);
        return AnimationUtils.loadAnimation(mContext, res);
    }

    public PayView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public PayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public PayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        if (decorView == null) {
            decorView = ((Activity) mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        }
        rootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.pay_view, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        mImgDelete = rootView.findViewById(R.id.delete);
        mTvProj = rootView.findViewById(R.id.mTvProj);
        mTvZhengX = rootView.findViewById(R.id.mTvZhengX);
        mTelephone = rootView.findViewById(R.id.mTelephone);
        mBgPic = rootView.findViewById(R.id.bg_pic);
        mTaoCan = rootView.findViewById(R.id.taocan);

        mImgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    /**
     * type=1 征信, type=2 项目管理
     */
    public void show(int type, final String telephone, final PermissionListener listener) {
        if (rootView.getParent() == null) {
            decorView.addView(rootView);
            rootView.startAnimation(getInAnimation());
        }
        if (type == 1) {
            mTvProj.setVisibility(GONE);
            mTvZhengX.setVisibility(VISIBLE);
            SpannableString spStr = new SpannableString("您的基础版账号征信查询次数已达上限，如需增加请购买征信扩容包");
            spStr.setSpan(new ForegroundColorSpan(Color.parseColor("#3C98E8")), 7, 17, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spStr.setSpan(new ForegroundColorSpan(Color.parseColor("#3C98E8")), 25, 30, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            mTvZhengX.setText(spStr);
            mBgPic.setBackgroundResource(R.drawable.pay_zx);
        } else {
            mTvZhengX.setVisibility(GONE);
            mTvProj.setVisibility(VISIBLE);
            SpannableString spStr = new SpannableString("您的基础版账号项目管理数量已达上限，如需增加请购买项目管理扩容包");
            spStr.setSpan(new ForegroundColorSpan(Color.parseColor("#3C98E8")), 7, 17, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spStr.setSpan(new ForegroundColorSpan(Color.parseColor("#3C98E8")), 25, 32, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            mTvProj.setText(spStr);
            mBgPic.setBackgroundResource(R.drawable.pay_proj);
        }
        ((TextView) mTelephone.getChildAt(0)).setText(telephone);
        mTelephone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // 没有权限。
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.CALL_PHONE)) {
                        // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                    } else {
                        // 申请授权。
                        listener.requestPermission(Manifest.permission.CALL_PHONE, telephone);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    Uri data = Uri.parse("tel:" + telephone);
                    intent.setData(data);
                    mContext.startActivity(intent);
                }
            }
        });

        // mTaoCan
    }

    public interface PermissionListener {
        void requestPermission(String permission, String telephone);
    }

    public void dismiss() {
        if (rootView.getParent() != null) {
            Animation out = getOutAnimation();
            out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    decorView.removeView(rootView);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rootView.startAnimation(out);
        }
    }
}
