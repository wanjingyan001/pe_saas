package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.bean.ProjectBean

/**
 * Created by qinfei on 17/8/11.
 */
class ShiDaLiuTongGuDongActivity : ShiDaGuDongActivity() {

    override val type: Int
        get() = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "十大流通股东"
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ShiDaLiuTongGuDongActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
