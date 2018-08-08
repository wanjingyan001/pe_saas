package com.sogukj.pe.module.clockin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.initNavTextColor2
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.widgets.ArrayPagerAdapter
import kotlinx.android.synthetic.main.activity_location.*
import org.jetbrains.anko.textColor

@Route(path = ARouterPath.LocationActivity)
class LocationActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        StatusBarUtil.setColor(this, resources.getColor(R.color.white), 0)
        StatusBarUtil.setLightMode(this)
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar_back.setImageResource(R.drawable.back_chevron)
        setBack(true)
        toolbar_title.textColor = Color.parseColor("#282828")
        toolbar_title.text = "外出打卡"

        initBottomNavBar()

        var per = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_SETTINGS)
        if (ContextCompat.checkSelfPermission(this, per[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, per[1]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, per[2]) != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this, per, 0x001);//自定义的code
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {

        } else {
            showCustomToast(R.drawable.icon_toast_common, "外出打卡功能需要定位权限")
        }
    }

    val fragments = arrayOf(
            LocationClockFragment(),
            LocationRecordFragment()
    )

    private fun initBottomNavBar() {
        val clockin = BottomNavigationItem(R.drawable.location_clock, "打卡").setInactiveIconResource(R.drawable.location_clock_neg).initNavTextColor2()
        val record = BottomNavigationItem(R.drawable.location_record, "记录").setInactiveIconResource(R.drawable.location_record_neg).initNavTextColor2()
        bottomBar.addItem(clockin)
        bottomBar.addItem(record)

        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = fragments.size

        bottomBar.setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setBarBackgroundColor(R.color.white)
                .setFirstSelectedPosition(0)
                .initialise()
        bottomBar.setTabSelectedListener(object : BottomNavigationBar.SimpleOnTabSelectedListener() {
            override fun onTabSelected(position: Int) {
                view_pager.currentItem = position
            }
        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                bottomBar.selectTab(position)
            }
        })
    }
}
