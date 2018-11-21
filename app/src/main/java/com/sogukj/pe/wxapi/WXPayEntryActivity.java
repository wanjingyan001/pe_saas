package com.sogukj.pe.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.sogukj.pe.Extras;
import com.sogukj.pe.baselibrary.utils.ToastUtils;
import com.sogukj.pe.baselibrary.utils.XmlDb;
import com.sogukj.pe.module.creditCollection.CreditSelectActivity;
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity;
import com.sogukj.pe.module.dataSource.DocumentsListActivity;
import com.sogukj.pe.module.other.PayExpansionActivity;
import com.sogukj.pe.module.project.ProjectDetailActivity;
import com.sogukj.pe.module.receipt.AccountRechargeActivity;
import com.sogukj.pe.module.user.PayManagerActivity;
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
        Log.e("TAG","onResp ---");
        reqCode=baseResp.errCode;
        int invokeType = XmlDb.Companion.open(this).get("invokeType", -1);
        switch (invokeType){
            case 0:
                //个人企业账户充值
                Intent intent=new Intent(WXPayEntryActivity.this, AccountRechargeActivity.class);
                intent.putExtra(Extras.WX_PAY_TYPE,reqCode+"");
                intent.putExtra(Extras.INSTANCE.getDATA(),"");
                intent.putExtra(Extras.INSTANCE.getTITLE(),"");
                startActivity(intent);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                //扩容套餐,账号管理,项目详情舆情购买,智能文书购买,首页征信购买，其他征信购买
                switch (reqCode) {
                    case 0 :
//                        ToastUtils.showSuccessToast("支付成功",this);
                        break;
                    case -1:
                        ToastUtils.showErrorToast("支付失败",this);
                        break;
                    case -2:
                        ToastUtils.showWarnToast("支付已取消",this);
                        break;
                }
                break;
        }
        if(reqCode == 0) {
            if(invokeType == 1) {
                //扩容套餐
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PayExpansionActivity.Companion.getEXPANSION_ACTION()));
            }else if(invokeType == 2) {
                //账号管理
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PayManagerActivity.Companion.getPAYMANAGER_ACTION()));
            }else if(invokeType == 3) {
                //项目详情舆情购买
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ProjectDetailActivity.Companion.getPROJECT_ACTION()));
            }else if(invokeType == 4) {
                //智能文书购买
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DocumentsListActivity.Companion.getINTELLIGENT_ACTION()));
            }else if(invokeType == 5) {
                //首页征信购买
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CreditSelectActivity.Companion.getCREDIT_ACTION()));
            }else if(invokeType == 6) {
                //其他征信购买
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ShareholderCreditActivity.Companion.getSHARE_CREDIT_ACTION()));
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
