package com.sogukj.pe.module.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.widgets.CalendarDingDing
import kotlinx.android.synthetic.main.activity_weekly_select.*
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

class WeeklySelectActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, hasDepart: Boolean) {
            val intent = Intent(ctx, WeeklySelectActivity::class.java)
            intent.putExtra(Extras.DATA, hasDepart)
            ctx?.startActivityForResult(intent, 0x001)
        }
    }

    var format = SimpleDateFormat("yyyy-MM-dd")
    var hasDepart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_select)
        Utils.setWindowStatusBarColor(this, R.color.white)

        hasDepart = intent.getBooleanExtra(Extras.DATA, false)

        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById<TextView?>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back)
            back?.visibility = View.VISIBLE
            back.setImageResource(R.drawable.back_chevron)
        }
        setBack(true)
        title = "请选择"

        var calendar = Calendar.getInstance()
        tv_start_time.text = format.format(calendar.time)
        tv_end_time.text = format.format(calendar.time)

        var startDD = CalendarDingDing(context)
        tr_start_time.setOnClickListener {
            calendar.time = format.parse(tv_start_time.text.toString())
            startDD.show(1, calendar, object : CalendarDingDing.onTimeClick {
                override fun onClick(date: Date?) {
                    if (date != null) {
                        tv_start_time.text = format.format(date)
                    }
                }
            })
        }

        var deadDD = CalendarDingDing(context)
        tr_end_time.setOnClickListener {
            calendar.time = format.parse(tv_end_time.text.toString())
            deadDD.show(1, calendar, object : CalendarDingDing.onTimeClick {
                override fun onClick(date: Date?) {
                    if (date != null) {
                        tv_end_time.text = format.format(date)
                    }
                }
            })
        }

        if (hasDepart) {
            tr_depart.visibility = View.VISIBLE
        } else {
            tr_depart.visibility = View.GONE
        }

        btn_commit.setOnClickListener {
            var intent = Intent()
            if (tv_start_time.text.isNullOrEmpty() || tv_end_time.text.isNullOrEmpty()) {
                showCustomToast(R.drawable.icon_toast_common, "请选择时间")
                return@setOnClickListener
            }
            if (format.parse(tv_start_time.text.toString()).time >= format.parse(tv_end_time.text.toString()).time) {
                showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于结束时间")
                return@setOnClickListener
            }
            intent.putExtra(Extras.TIME1, tv_start_time.text!!)
            intent.putExtra(Extras.TIME2, tv_end_time.text!!)
            if (hasDepart) {
                if (tv_depart.text.isNullOrEmpty()) {
                    showCustomToast(R.drawable.icon_toast_common, "请选择部门")
                    return@setOnClickListener
                }
                intent.putExtra(Extras.DATA, tv_depart.text)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
