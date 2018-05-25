package com.sogukj.pe.module.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_scan_result.*

class ScanResultActivity : BaseActivity() {

    companion object {
        fun start(ctx: Activity?, result: String) {
            val intent = Intent(ctx, ScanResultActivity::class.java)
            intent.putExtra(Extras.DATA, result)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        toolbar_back.setOnClickListener {
            finish()
            overridePendingTransition(0,R.anim.activity_out)
        }

        cancel.setOnClickListener {
            finish()
            overridePendingTransition(0,R.anim.activity_out)
        }

        login.setOnClickListener {
            var scanResult = intent.getStringExtra(Extras.DATA)

            var index = scanResult.indexOf("/api/")

            SoguApi.getService(application, OtherService::class.java)
                    .qrNotify(scanResult.substring(index), 2)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            finish()
                            overridePendingTransition(0,R.anim.activity_out)
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        showCustomToast(R.drawable.icon_toast_fail, "登陆失败")
                    })
        }
    }
}
