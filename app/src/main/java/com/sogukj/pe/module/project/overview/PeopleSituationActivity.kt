package com.sogukj.pe.module.project.overview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.extraDelegate
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.ArrayPagerAdapter
import com.sogukj.pe.module.calendar.adapter.ContentAdapter
import com.sogukj.pe.module.project.ProjectListFragment
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_people_situation.*
import org.jetbrains.anko.find

class PeopleSituationActivity : ToolbarActivity(), ViewPager.OnPageChangeListener {
    lateinit var fragments: ArrayList<BaseFragment>
    private val principalId by extraDelegate(Extras.ID, 0)
    private val name by extraDelegate(Extras.NAME,"")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_situation)
        title = "${name}的项目管理"
        setBack(true)
        getTags()
    }


    private fun getTags() {
        SoguApi.getService(application, ProjectService::class.java)
                .showStage()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            fragments = ArrayList()
                            val titles = mutableListOf<String>()
                            payload.payload?.filter { it.name != "总览" }?.forEach {
                                titles.add(it.name!!)
                                fragments.add(ProjectListFragment.newInstance(it.type!!, false, principalId))
                            }
                            initViewpager(fragments, titles)
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    private fun initViewpager(fragments: ArrayList<BaseFragment>, titles: List<String>) {
        val adapter = ArrayPagerAdapter(supportFragmentManager, fragments.toTypedArray())
        contentPager.adapter = adapter
        tabLayout.setViewPager(contentPager)
        tabLayout.setTabViewFactory { parent, _ ->
            parent.removeAllViews()
            for (i in 0 until titles.size) {
                val view = LayoutInflater.from(this).inflate(R.layout.item_calendar_indicator, parent, false)
                view.find<TextView>(R.id.indicatorTv).text = titles[i]
                parent.addView(view)
            }
        }
        contentPager.addOnPageChangeListener(this)
        contentPager.currentItem = 0
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {

    }


}
