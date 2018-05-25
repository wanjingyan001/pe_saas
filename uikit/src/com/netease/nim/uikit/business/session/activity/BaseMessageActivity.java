package com.netease.nim.uikit.business.session.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.fragment.MessageFragment;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;

import java.util.List;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public abstract class BaseMessageActivity extends UI {

    protected String sessionId;

    private SessionCustomization customization;

    private MessageFragment messageFragment;
    private static final int BASIC_PERMISSION_REQUEST_CODE = 100;

    protected abstract MessageFragment fragment();

    protected abstract int getContentViewId();

    protected abstract void initToolBar();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentViewId());
        requestBasicPermission();
        initToolBar();
        parseIntent();

        messageFragment = (MessageFragment) switchContent(fragment());
    }

    @Override
    public void onBackPressed() {
        if (messageFragment == null || !messageFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (messageFragment != null) {
            messageFragment.onActivityResult(requestCode, resultCode, data);
        }

        if (customization != null) {
            customization.onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private void parseIntent() {
        sessionId = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        customization = (SessionCustomization) getIntent().getSerializableExtra(Extras.EXTRA_CUSTOMIZATION);

        if (customization != null) {
            addRightCustomViewOnActionBar(this, customization.buttons);
        }
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_SETTINGS
    };

    protected void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        try {
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            Toast.makeText(this, "未全部授权，部分功能可能无法正常运行！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    // 添加action bar的右侧按钮及响应事件
    private void addRightCustomViewOnActionBar(UI activity, List<SessionCustomization.OptionsButton> buttons) {
        if (buttons == null || buttons.size() == 0) {
            return;
        }

        Toolbar toolbar = getToolBar();
        if (toolbar == null) {
            return;
        }

        LinearLayout view = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.nim_action_bar_custom_view, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        for (final SessionCustomization.OptionsButton button : buttons) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageResource(button.iconId);
            imageView.setBackgroundResource(R.drawable.nim_nim_action_bar_button_selector);
            imageView.setPadding(ScreenUtil.dip2px(10), 0, ScreenUtil.dip2px(10), 0);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.onClick(BaseMessageActivity.this, v, sessionId);
                }
            });
            view.addView(imageView, params);
        }

        toolbar.addView(view, new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT | Gravity.CENTER));
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return !messageFragment.onKeyDown(keyCode, event) && super.onKeyDown(keyCode, event);
//    }
}
