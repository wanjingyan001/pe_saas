package com.sogukj.pe.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.sogukj.pe.Extras;
import com.sogukj.pe.module.receipt.AccountRechargeActivity;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by CH-ZH on 2018/10/23.
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
    private int reqCode;
    private IWXAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Extras.WEIXIN_APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.e("TAG","result = :" +new Gson().toJson(baseReq));
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        reqCode=baseResp.errCode;
        Intent intent=new Intent(WXPayEntryActivity.this, AccountRechargeActivity.class);
        intent.putExtra(Extras.WX_PAY_TYPE,reqCode+"");
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
