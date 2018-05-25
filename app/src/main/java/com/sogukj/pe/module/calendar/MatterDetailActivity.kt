package com.sogukj.pe.module.calendar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.ProjectMatterCompany
import com.sogukj.pe.module.calendar.adapter.MatterPagerAdapter
import kotlinx.android.synthetic.main.activity_matter_dateil.*

class MatterDetailActivity : ToolbarActivity() {


    companion object {
        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, MatterDetailActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matter_dateil)
        val company = intent.getSerializableExtra(Extras.DATA) as ProjectMatterCompany
        title = company.companyName
        setBack(true)


        val fragments = ArrayList<Fragment>()
        fragments.add(KeyNoteFragment.newInstance(company.companyId, ""))
        fragments.add(TodoFragment.newInstance(company.companyId, ""))
        fragments.add(CompleteProjectFragment.newInstance(company.companyId, ""))
        mattersList.adapter = MatterPagerAdapter(supportFragmentManager, fragments)
        matterTab.setupWithViewPager(mattersList)
    }


}
