package com.sogukj.pe.ddshare

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory
import com.android.dingtalk.share.ddsharemodule.IDDAPIEventHandler
import com.android.dingtalk.share.ddsharemodule.IDDShareApi
import com.android.dingtalk.share.ddsharemodule.ShareConstant
import com.android.dingtalk.share.ddsharemodule.message.BaseReq
import com.android.dingtalk.share.ddsharemodule.message.BaseResp
import com.android.dingtalk.share.ddsharemodule.message.SendAuth
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.jsonStr

class DDShareActivity : Activity(), IDDAPIEventHandler {
    companion object {
        const val DDApp_Id = "dingoarlewi4r20zyuurob"
        const val DD_APP_SECRET = "zTY34GJEui_iCJjVPqfacnZUVcpFm-f62yDnhtPhEaHKkMlvUBhBA6mYMF_5RLJ5"
    }

    private lateinit var mIDDShareApi: IDDShareApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            //activity的export为true，try起来，防止第三方拒绝服务攻击
            mIDDShareApi = DDShareApiFactory.createDDShareApi(this, DDApp_Id, false)
            mIDDShareApi.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResp(baseResp: BaseResp) {
        val errCode = baseResp.mErrCode
        val errMsg = baseResp.mErrStr
        Log.e("TAG","errCode =====>" + errCode + "  errMsg ====> " + errMsg )
        if (baseResp.type == ShareConstant.COMMAND_SENDAUTH_V2 && baseResp is SendAuth.Resp) {
            when (errCode) {
                BaseResp.ErrCode.ERR_OK -> {
                    DDLogin(baseResp.code)
                    Toast.makeText(this, "授权成功，授权码为:" + baseResp.code, Toast.LENGTH_SHORT).show()
                }
                BaseResp.ErrCode.ERR_USER_CANCEL -> Toast.makeText(this, "授权取消", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "授权异常" + baseResp.mErrStr, Toast.LENGTH_SHORT).show()
            }
        } else {
            when (errCode) {
                BaseResp.ErrCode.ERR_OK -> Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show()
                BaseResp.ErrCode.ERR_USER_CANCEL -> Toast.makeText(this, "分享取消", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "分享失败" + baseResp.mErrStr, Toast.LENGTH_SHORT).show()
            }
        }

        finish()
    }

    override fun onReq(p0: BaseReq?) {
    }

    private fun DDLogin(code: String) {
        val service = DDApi.getService()
        service.getAccountToken()
                .flatMap {
                    service.getAuthorizeCode(it.access_token,AuthorizeReq(code))
                }.execute {
                    onNext {
                        Log.d("WJY", it.jsonStr)
                    }
                }

    }

}
