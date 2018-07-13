package com.sogukj.pe.module.project

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil.setContentView
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import anet.channel.util.Utils.context
import com.alibaba.android.arouter.facade.annotation.Route
import com.amap.api.mapcore.util.it
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.R
import com.sogukj.pe.R.id.*
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.ArrayPagerAdapter
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.project.archives.RecordDetailActivity
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.activity_project_trace.*
@Route(path = ARouterPath.ProjectTraceActivity)
class ProjectTraceActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, ProjectTraceActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_trace)
        StatusBarUtil.setColor(this, resources.getColor(R.color.main_bottom_bar_color), 0)
        StatusBarUtil.setDarkMode(this)
        toolbar?.setBackgroundColor(Color.TRANSPARENT)

        back.setOnClickListener {
            onBackPressed()
        }

        toolbar_menu.setOnClickListener {
            var project = ProjectBean()
            project.name = ""
            RecordDetailActivity.startAdd(this@ProjectTraceActivity, project, mTypeList.get(view_pager.currentItem))
        }

        AppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            var appBarHeight = AppBarLayout.height
            var toolbarHeight = toolbar!!.height

            var gifH = record.height
            var finalGifH = 0
            var scale = 1.0f - (gifH - finalGifH) * Math.abs(verticalOffset) * 1.0f / (appBarHeight - toolbarHeight) / gifH
            record.alpha = scale

            if (appBarHeight - toolbarHeight - Math.abs(verticalOffset).toFloat() < 5) {
                collapse.setTitle("")
                toolbar_title.visibility = View.VISIBLE
            } else {
                collapse.setTitle("项目跟踪")
                toolbar_title.visibility = View.GONE
            }
        }

        initView()

        SoguApi.getService(application,ProjectService::class.java)
                .recordsNum()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.payload?.apply {
                            number.text = this.num.toString()
                        }
                    } else {
                        record.visibility = View.INVISIBLE
                    }
                }, { e ->
                    record.visibility = View.INVISIBLE
                })
    }

    lateinit var fragments: ArrayList<BaseFragment>
    val mTypeList = ArrayList<Int>()
    val mTypeMap = HashMap<Int, String>()

    private fun initView() {
        tabs.removeAllTabs()
        SoguApi.getService(application,ProjectService::class.java)
                .showStage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        fragments = ArrayList<BaseFragment>()
                        payload?.payload?.forEach {
                            mTypeList.add(it.type!!)
                            mTypeMap.put(it.type!!, it.name!!)
                            fragments.add(TraceListFragment.newInstance(it.type!!))
                            tabs.addTab(tabs.newTab().setText(it.name!!))
                        }

                        Utils.setUpIndicatorWidth(tabs, 5, 5, context)

                        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments.toTypedArray())
                        view_pager.adapter = adapter
                        view_pager.offscreenPageLimit = fragments.size

                        tabs.getTabAt(mTypeList.size / 2)?.select()
                        view_pager?.currentItem = mTypeList.size / 2

                        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                            override fun onTabReselected(tab: TabLayout.Tab?) {

                            }

                            override fun onTabUnselected(tab: TabLayout.Tab?) {

                            }

                            override fun onTabSelected(tab: TabLayout.Tab) {
                                view_pager?.currentItem = tab.position
                            }

                        })
                        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                            override fun onPageScrollStateChanged(state: Int) {
                            }

                            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                            }

                            override fun onPageSelected(position: Int) {
                                tabs?.getTabAt(position)?.select()
                            }

                        })

                        if (mTypeList.size % 2 == 0) {
                            tabs.getTabAt(mTypeList.size / 2 - 1)?.select()
                            view_pager?.currentItem = mTypeList.size / 2 - 1
                        } else {
                            tabs.getTabAt(mTypeList.size / 2)?.select()
                            view_pager?.currentItem = mTypeList.size / 2
                        }

                        if (tabs.tabCount <= 5) {
                            tabs.tabMode = TabLayout.MODE_FIXED
                        } else {
                            tabs.tabMode = TabLayout.MODE_SCROLLABLE
//                            tabs.viewTreeObserver.addOnGlobalLayoutListener {
//                                Utils.setTabWidth(tabs, context, tabs.width / 5 + 5)
//                            }
                        }
                    } else {
                    }
                }, { e ->
                    Trace.e(e)
                })
    }
}
