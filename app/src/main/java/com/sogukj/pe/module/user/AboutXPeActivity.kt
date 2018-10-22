package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.CheckUpdateUtil.Companion.checkUpdate
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.about_content.*
import kotlinx.android.synthetic.main.activity_aboutxpe.*

/**
 * Created by CH-ZH on 2018/8/18.
 * 关于X-PE
 */
class AboutXPeActivity : BaseActivity(){
    private var mzsm = Extras.DECLARE_URL
    private var bps = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aboutxpe)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initData()
        bindListener()
    }

    private fun initData() {
        toolbar_title.text = resources.getString(R.string.about_xpe)
        tv_version.text = "搜股XPE " + Utils.getVersionName(this)
        getAboutPageData()
    }

    private fun getAboutPageData() {
        SoguApi.getService(application,OtherService::class.java)
                .getAboutPage()
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val pageBean = payload.payload
                            pageBean?.let {
                                mzsm = it.mzsm
                                bps = it.bps
                            }
                        }
                    }

                    onError {
                        it.printStackTrace()
                    }
                }
    }

    private fun bindListener() {
        toolbar_back.clickWithTrigger {
            onBackPressed()
        }

        tv_checkUpdate.clickWithTrigger {
            checkUpdate(this)
        }

        tv_declare.clickWithTrigger {
            //免责声明
            WebNormalActivity.invoke(this,"免责声明", mzsm)
        }
        tv_safe.clickWithTrigger {
            //X-PE安全白皮书
            WebNormalActivity.invoke(this,"XPE安全白皮书", bps)
        }
    }

    companion object {
        fun invoke(context: Context){
            var intent = Intent(context,AboutXPeActivity::class.java)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}