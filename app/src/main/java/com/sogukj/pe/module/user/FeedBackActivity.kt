package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_feed_back.*

class FeedBackActivity : BaseActivity() {

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, FeedBackActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_back)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "意见反馈"
        toolbar_back.clickWithTrigger {
            onBackPressed()
        }

        submitFeed.clickWithTrigger {
            submit()
        }
    }


    private fun submit() {
        val user = Store.store.getUser(context)
        user?.let {
            SoguApi.getService(application, UserService::class.java)
                    .addFeedback(feedEdt.text.toString(),
                            user.name,
                            user.phone)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            //showToast("提交成功")
                            showCustomToast(R.drawable.icon_toast_success, "提交成功")
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        }
    }
}
