package com.sogukj.pe.module.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.ldf.calendar.model.CalendarDate
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.interf.MonthSelectListener
import com.sogukj.pe.module.calendar.adapter.ContentAdapter
import kotlinx.android.synthetic.main.activity_calendar_mian.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

@Route(path = ARouterPath.CalendarMainActivity)
class CalendarMainActivity : ToolbarActivity(), MonthSelectListener, ViewPager.OnPageChangeListener {

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, CalendarMainActivity::class.java))
        }
    }

    override val menuId: Int
        get() = R.menu.calendar_menu
    private var currentPosition: Int by Delegates.notNull()
    val fragments = ArrayList<Fragment>()
    private val titles = arrayListOf("周工作安排", "日历", "任务", "项目事项", "团队日程")
    private lateinit var arrangeFragment: ArrangeListFragment
    private lateinit var scheduleFragment: ScheduleFragment
    private lateinit var teamScheduleFragment: TeamScheduleFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_mian)
        setBack(true)
        currentPosition = 0
        arrangeFragment = ArrangeListFragment.newInstance("", "")
        scheduleFragment = ScheduleFragment.newInstance("", "")
        teamScheduleFragment = TeamScheduleFragment.newInstance("", "")
        scheduleFragment.monthSelect = this
        teamScheduleFragment.monthSelect = this
        fragments.add(arrangeFragment)
        fragments.add(scheduleFragment)
        fragments.add(TaskFragment.newInstance("", ""))
        fragments.add(ProjectMattersFragment.newInstance("", ""))
        fragments.add(teamScheduleFragment)
        initPager()
        title = SimpleDateFormat("yyyy年MM月").format(Date(System.currentTimeMillis()))
        addSchedule.visibility = View.GONE
        addSchedule.setOnClickListener {
            ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
        }
    }

    private fun initPager() {
        val adapter = ContentAdapter(supportFragmentManager, fragments, titles)
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
        contentPager.offscreenPageLimit = 3
    }

    override fun onMonthSelect(date: CalendarDate) {
        title = "${date.year}年${date.month}月"
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }


    override fun onPageSelected(position: Int) {
        currentPosition = position
        when (position) {
            0 -> {
                mMenu.getItem(0).isVisible = true
                title = scheduleFragment.date
                addSchedule.visibility = View.GONE
            }
            1 -> {
                mMenu.getItem(0).isVisible = true
                title = scheduleFragment.date
                addSchedule.visibility = View.GONE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
                }
            }
            2 -> {
                mMenu.getItem(0).isVisible = true
                title = "日历"
                addSchedule.visibility = View.GONE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Task)
                }
            }
            3 -> {
                mMenu.getItem(0).isVisible = false
                title = "日历"
                addSchedule.visibility = View.GONE
            }
            4 -> {
                mMenu.getItem(0).isVisible = false
                title = teamScheduleFragment.date
                addSchedule.visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.calendar_menu -> {
                when (currentPosition) {
                    0 -> ArrangeEditActivity.start(this, arrangeFragment.getWeeklyData(), arrangeFragment.offset.toString())
                    1 -> ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
                    2 -> ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Task)
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }
}
