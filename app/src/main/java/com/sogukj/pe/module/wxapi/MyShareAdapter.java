package com.sogukj.pe.module.wxapi;

import cn.sharesdk.framework.authorize.AuthorizeAdapter;

/**
 * Created by jjc on 2016/6/8.
 */
public class MyShareAdapter extends AuthorizeAdapter {
    @Override
    public void onCreate() {
        // 隐藏标题栏右部的ShareSDK Logo
        hideShareSDKLogo();
    }
}
