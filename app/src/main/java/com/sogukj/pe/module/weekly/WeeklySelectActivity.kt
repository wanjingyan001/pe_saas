package com.sogukj.pe.module.weekly

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ReceiveSpinnerBean
import com.sogukj.pe.service.UserService
import com.sogukj.pe.widgets.CalendarDingDing
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_weekly_select.*
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

class WeeklySelectActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, hasDepart: Boolean, title: String) {
            val intent = Intent(ctx, WeeklySelectActivity::class.java)
            intent.putExtra(Extras.DATA, hasDepart)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivityForResult(intent, 0x001)
        }
    }

    var format = SimpleDateFormat("yyyy-MM-dd")
    var hasDepart = false

    /**
     * 每周的第一天和最后一天
     * @param dataStr
     * @param dateFormat
     * @param resultDateFormat
     * @return
     * @throws ParseException
     */
    fun getFirstAndLastOfWeek(): ArrayList<String> {
        val cal = Calendar.getInstance()
        cal.time = Date()
        var d = 0
        if (cal.get(Calendar.DAY_OF_WEEK) === Calendar.SUNDAY) {
            d = -6
        } else {
            d = 2 - cal.get(Calendar.DAY_OF_WEEK)
        }
        cal.add(Calendar.DAY_OF_WEEK, d)
        // 所在周开始日期
        val data1 = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        // 所在周结束日期
        val data2 = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        return arrayListOf(data1, data2)
    }

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
        title = intent.getStringExtra(Extras.TITLE)

        var calendar = Calendar.getInstance()
//        tv_start_time.text = format.format(calendar.time)
//        tv_end_time.text = format.format(calendar.time)
        tv_start_time.text = getFirstAndLastOfWeek()[0]
        tv_end_time.text = getFirstAndLastOfWeek()[1]

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

            getDepartmentData()

            tr_depart.setOnClickListener {
                var pvOptions = OptionsPickerBuilder(this@WeeklySelectActivity, OnOptionsSelectListener { options1, option2, options3, v ->
                    fillData(spinner_data.get(options1))
                }).setDecorView(window.decorView.find(android.R.id.content)).build<String>()
                pvOptions.setPicker(spinner_data.map { it.name })
                pvOptions.show()
            }
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
                intent.putExtra(Extras.DATA, selected_depart_id)
                intent.putExtra(Extras.DATA2, tv_depart.text)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    var spinner_data = ArrayList<ReceiveSpinnerBean>()
    var selected_depart_id: Int = 0

    private fun fillData(bean: ReceiveSpinnerBean) {
        selected_depart_id = bean.id!!
        tv_depart.text = bean.name
    }

    private fun getDepartmentData() {
        spinner_data.clear()
        SoguApi.getService(application, UserService::class.java)
                .getDepartment()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            spinner_data = this
                            var total = ReceiveSpinnerBean()
                            total.id = 0
                            total.name = "全部"
                            spinner_data.add(0, total)
                            fillData(total)
                        }
                    } else {
                        //showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        var total = ReceiveSpinnerBean()
                        total.id = 0
                        total.name = "全部"
                        spinner_data.add(0, total)
                        fillData(total)
                    }
                }, { e ->
                    Trace.e(e)
                    var total = ReceiveSpinnerBean()
                    total.id = 0
                    total.name = "全部"
                    spinner_data.add(0, total)
                    fillData(total)
                    //ToastError(e)
                })
    }
}
