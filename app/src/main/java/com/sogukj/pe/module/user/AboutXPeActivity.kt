package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.CheckUpdateUtil.Companion.checkUpdate
import kotlinx.android.synthetic.main.about_content.*
import kotlinx.android.synthetic.main.activity_aboutxpe.*

/**
 * Created by CH-ZH on 2018/8/18.
 * 关于X-PE
 */
class AboutXPeActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aboutxpe)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initData()
        bindListener()
    }

    private fun initData() {
        toolbar_title.text = resources.getString(R.string.about_xpe)
        tv_version.text = "搜股X-PE " + Utils.getVersionName(this)
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