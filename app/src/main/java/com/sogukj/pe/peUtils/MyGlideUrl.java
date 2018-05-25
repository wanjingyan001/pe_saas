package com.sogukj.pe.peUtils;

import com.bumptech.glide.load.model.GlideUrl;

public class MyGlideUrl extends GlideUrl {
    private String mUrl;

    public MyGlideUrl(String url) {
        super(url);
        mUrl = url;
    }

    @Override
    public String getCacheKey() {
        try {
            return mUrl.split("\\?")[0];
        } catch (Exception e) {
            return mUrl;
        }
    }

    //http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/headimg/5ad59dd25f869.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1525685656&Signature=G1e0XaL1E8kljM42I6hFPo5aOJg%3D
    //http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/headimg/5ad59dd25f869.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1525685687&Signature=p%2BipNtVPZSIIBU0MaxLghUQ43Yc%3D
}
