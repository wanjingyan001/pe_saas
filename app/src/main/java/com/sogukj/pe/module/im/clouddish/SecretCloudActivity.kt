package com.sogukj.pe.module.im.clouddish

import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_secret_cloud.*
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/25.
 * 首页加密云盘
 */
class SecretCloudActivity : ToolbarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_cloud)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        bindListener()
        setBack(true)
        setTitle("加密云盘")
    }

    private fun bindListener() {
        rl_search.clickWithTrigger {
            //文件搜索
        }

        tv_dynamic.clickWithTrigger {
            //文件动态
            startActivity<FileDynamicActivity>()
        }

        view_mine_file.clickWithTrigger {
            //我的文件
            startActivity<MineFileActivity>()
        }

        ll_fund_file.clickWithTrigger {
            //基金文件
        }

        ll_pro_file.clickWithTrigger {
            //项目文件
        }

        ll_chat_file.clickWithTrigger {
            //群组文件
        }
    }
}