package com.sogukj.pe.module.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IntRange
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

class ProjectFocusActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_focus)
        setBack(true)
        val type = intent.getIntExtra(Extras.TYPE, -1)
        title = intent.getStringExtra(Extras.TITLE)
//        when (type) {
//            ProjectListFragment.TYPE_GZ -> title = "关注"
//            ProjectListFragment.TYPE_DY -> title = "调研"
//            ProjectListFragment.TYPE_CB -> title = "储备"
//            ProjectListFragment.TYPE_LX -> title = "立项"
//            ProjectListFragment.TYPE_YT -> title = "已投"
//            ProjectListFragment.TYPE_TC -> title = "退出"
//        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, ProjectListFragment.newInstance(type, true))
                .commit()
    }

    companion object {
        fun start(ctx: Activity?, @IntRange(from = 1, to = 7) type: Int, title: String) {
            val intent = Intent(ctx, ProjectFocusActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }
}
