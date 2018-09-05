package com.sogukj.pe.module.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

@Route(path = ARouterPath.MainNewsActivity)
class MainNewsActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_news)
        val intExtra = intent.getIntExtra(Extras.FLAG, -1)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, MainNewsFragment.newInstance(intExtra))
                .commit()
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, MainNewsActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
