package com.sogukj.pe.module.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

class MainNewsActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_news)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, MainNewsFragment.newInstance())
                .commit()
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, MainNewsActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
