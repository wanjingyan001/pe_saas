package com.sogukj.pe.module.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.initNavTextColor2
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.widgets.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_weekly.*
import org.jetbrains.anko.textColor

@Route(path = ARouterPath.WeeklyActivity)
class WeeklyActivity : ToolbarActivity() {

    val fragments = arrayOf(
            WeeklyThisFragment.newInstance("MAIN", null, null, null, 1000000),
            WeeklyWaitToWatchFragment(),
            WeeklyISendFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly)
        StatusBarUtil.setColor(this, resources.getColor(R.color.white), 0)
        StatusBarUtil.setLightMode(this)
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar_back.setImageResource(R.drawable.back_chevron)
        setBack(true)

        toolbar_title.textColor = Color.parseColor("#282828")
        toolbar_title.text = "本周周报"

        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = fragments.size

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                bottomBar.selectTab(position)
            }
        })

        initBottomNavBar()

        if (bottomBar.currentSelectedPosition == 0) {
            toolbar_title.text = "本周周报"
            toolbar_menu.visibility = View.VISIBLE
            state = "FULL"
            toolbar_menu.setImageResource(R.drawable.full1)

            toolbar_menu.setOnClickListener {
                if (state == "FULL") {
                    state = "ONLY"
                    toolbar_menu.setImageResource(R.drawable.only)
                    (fragments.get(0) as WeeklyThisFragment).hide()
                } else {
                    state = "FULL"
                    toolbar_menu.setImageResource(R.drawable.full1)
                    (fragments.get(0) as WeeklyThisFragment).show()
                }
            }
        }
    }

    private fun initBottomNavBar() {
        bottomBar.addItem(BottomNavigationItem(R.drawable.weekly11, "本周周报").setInactiveIconResource(R.drawable.weekly1).initNavTextColor2())
                .addItem(BottomNavigationItem(R.drawable.weekly22, "我接收的").setInactiveIconResource(R.drawable.weekly2).initNavTextColor2())
                .addItem(BottomNavigationItem(R.drawable.weekly33, "我发出的").setInactiveIconResource(R.drawable.weekly3).initNavTextColor2())
                .setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setBarBackgroundColor(R.color.white)
                .setFirstSelectedPosition(0)
                .initialise()
        bottomBar.setTabSelectedListener(object : BottomNavigationBar.SimpleOnTabSelectedListener() {
            override fun onTabSelected(position: Int) {
                view_pager.setCurrentItem(position, true)

                if (position == 0) {
                    toolbar_title.text = "本周周报"
                    toolbar_menu.visibility = View.VISIBLE
                    state = "FULL"
                    toolbar_menu.setImageResource(R.drawable.full1)
                    (fragments.get(0) as WeeklyThisFragment).show()
                    toolbar_menu.setOnClickListener {
                        if (state == "FULL") {
                            state = "ONLY"
                            toolbar_menu.setImageResource(R.drawable.only)
                            (fragments.get(0) as WeeklyThisFragment).hide()
                        } else {
                            state = "FULL"
                            toolbar_menu.setImageResource(R.drawable.full1)
                            (fragments.get(0) as WeeklyThisFragment).show()
                        }
                    }
                } else if (position == 1) {
                    toolbar_title.text = "我接收的"
                    toolbar_menu.visibility = View.INVISIBLE
                } else {
                    toolbar_title.text = "我发出的"
                    toolbar_menu.visibility = View.VISIBLE
                    toolbar_menu.setImageResource(R.drawable.iv_search_filter_gray)
                    toolbar_menu.setOnClickListener {
                        WeeklySelectActivity.start(context, false, "我发出的")
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            data?.apply {
                var start = getStringExtra(Extras.TIME1)
                var end = getStringExtra(Extras.TIME2)
                (fragments.get(2) as WeeklyISendFragment).doRequest(start, end)
            }
        }
    }

    var state = "FULL"

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, WeeklyActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
