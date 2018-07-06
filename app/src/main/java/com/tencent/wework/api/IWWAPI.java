package com.tencent.wework.api;

import android.content.Intent;

import com.tencent.wework.api.model.BaseMessage;

public interface IWWAPI {
    /**
     *
     * @param schema
     * @return
     */
    boolean registerApp(String schema);

    void unregisterApp();

    boolean handleIntent(Intent var1, IWWAPIEventHandler var2);


    boolean isWWAppInstalled();

    boolean isWWAppSupportAPI();


    int getWWAppSupportAPI();

    boolean openWWApp();


    boolean sendMessage(BaseMessage var1);

    boolean sendMessage(BaseMessage var1, IWWAPIEventHandler callback);
    void detach();
}
