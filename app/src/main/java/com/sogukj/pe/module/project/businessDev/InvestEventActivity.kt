package com.sogukj.pe.module.project.businessDev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_invest_event.*

/**
 * Created by qinfei on 17/8/11.
 */
class InvestEventActivity : ToolbarActivity(), TabLayout.OnTabSelectedListener {
    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragments[tab!!.position])
                .commit()
    }

    val fragments = arrayOfNulls<Fragment>(2)
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invest_event)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "投资事件"
        tabs.addOnTabSelectedListener(this)
        fragments[0] = FinanceEventListFragment.newInstance(project)
        fragments[1] = FinanceDistributeListFragment.newInstance(project)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragments[0])
                .commit()
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, InvestEventActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
