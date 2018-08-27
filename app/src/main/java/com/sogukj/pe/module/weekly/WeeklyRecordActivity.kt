package com.sogukj.pe.module.weekly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.peUtils.CacheUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.WeeklyService
import com.sogukj.pe.widgets.CalendarDingDing
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_weekly_record.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*

class WeeklyRecordActivity : BaseActivity() {

    var format = SimpleDateFormat("yyyy-MM-dd")

    lateinit var week: WeeklyThisBean.Week

    lateinit var cache: CacheUtils
    var tag = ""

    override fun onDestroy() {
        super.onDestroy()
        cache.close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_record)
        Utils.setWindowStatusBarColor(this, R.color.white)

        var calendar = Calendar.getInstance()
        cache = CacheUtils(context)

        tag = intent.getStringExtra(Extras.FLAG)
        if (tag == "ADD") {
            toolbar_title.text = "补充工作日程"

            tv_start_time.text = format.format(calendar.time)
            tv_end_time.text = format.format(calendar.time)

            week = intent.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week

            tv_start_time.text = week.start_time
            tv_end_time.text = week.end_time

            loadScript()

        } else if (tag == "EDIT") {
            toolbar_title.text = "修改工作日程"
            week = intent.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week

            tv_start_time.text = week.start_time
            tv_end_time.text = week.end_time
            et_des.setText(week.info)
            et_des.setSelection(week.info!!.length)
        }
        addTv.visibility = View.GONE
        //back.setOnClickListener { finish() }
        back.setOnClickListener {
            if (tag == "EDIT") {
                finish()
            } else {
                var script = et_des.text.toString().trim()
                if (script == "") {
                    finish()
                    return@setOnClickListener
                }
                saveScript()
            }
        }

        var startDD = CalendarDingDing(context)
        tr_start_time.setOnClickListener {
            if (tag == "EDIT" || tag == "ADD") {
                return@setOnClickListener
            }

            calendar.time = format.parse(tv_start_time.text.toString())
//            val timePicker = TimePickerView.Builder(this, { date, view ->
//                tv_start_time.text = format.format(date)
//            })
//                    //年月日时分秒 的显示与否，不设置则默认全部显示
//                    .setType(booleanArrayOf(true, true, true, true, true, false))
//                    .setDividerColor(Color.DKGRAY)
//                    .setContentSize(15)
//                    .setDate(calendar)
//                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
//                    .build()
//            timePicker.show()
            startDD.show(2, calendar) { date ->
                if (date != null) {
                    tv_start_time.text = format.format(date)
                }
            }
        }

        var deadDD = CalendarDingDing(context)
        tr_end_time.setOnClickListener {
            if (tag == "EDIT" || tag == "ADD") {
                return@setOnClickListener
            }
            calendar.time = format.parse(tv_end_time.text.toString())
//            val timePicker = TimePickerView.Builder(this, { date, view ->
//                tv_end_time.text = format.format(date)
//            })
//                    //年月日时分秒 的显示与否，不设置则默认全部显示
//                    .setType(booleanArrayOf(true, true, true, true, true, false))
//                    .setDividerColor(Color.DKGRAY)
//                    .setContentSize(15)
//                    .setDate(calendar)
//                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
//                    .build()
//            timePicker.show()
            deadDD.show(2, calendar) { date ->
                if (date != null) {
                    tv_end_time.text = format.format(date)
                }
            }
        }

//        toolbar?.setBackgroundColor(Color.WHITE)
//        toolbar?.apply {
//            val title = this.findViewById(R.id.toolbar_title) as TextView?
//            title?.textColor = Color.parseColor("#282828")
//            val back = this.findViewById(R.id.toolbar_back) as ImageView
//            back?.visibility = View.VISIBLE
//            back.setBackgroundResource(R.drawable.grey_back)
//        }
//        setBack(true)

        btn_commit.setOnClickListener {
            var weekly_id: Int? = null
            if (tag == "EDIT") {
                weekly_id = week.week_id
            }
//            if (et_des.text.toString().trim() == "") {
//                showCustomToast(R.drawable.icon_toast_common, "工作内容不能为空")
//                return@setOnClickListener
//            }
            SoguApi.getService(application,WeeklyService::class.java)
                    .addEditReport(tv_start_time.text.toString(), tv_end_time.text.toString(), et_des.text.toString(), weekly_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            clearScript()
                            payload.payload?.apply {
                                var df = SimpleDateFormat("HH:mm")
                                if (tag == "EDIT") {
                                    var intent = Intent()
                                    //week.time = df.format(Date())
                                    week.info = et_des.text.toString()
                                    intent.putExtra(Extras.DATA, week)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                } else {
                                    week.date = SimpleDateFormat("yyyy-MM-DD HH:mm:ss").format(Date())
//                                    week = WeeklyThisBean.Week()
                                    week.time = df.format(Date())
//                                    week.s_times = tv_start_time.text.toString()
//                                    week.e_times = tv_end_time.text.toString()
                                    week.info = et_des.text.toString()
                                    // 应该是int，确实double
                                    week.week_id = this.toString().split(".")[0].toInt()

                                    var intent = Intent()
                                    intent.putExtra(Extras.DATA, week)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                }
                            }
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        ToastError(e)
                    })
        }
    }

    override fun onBackPressed() {
        if (tag == "EDIT") {
            super.onBackPressed()
        } else {
            var script = et_des.text.toString().trim()
            if (script == "") {
                super.onBackPressed()
                return
            }
            saveScript()
        }
    }

    fun loadScript() {
        var key = "${Store.store.getUser(context)!!.uid}+$tag+${week.start_time}+${week.end_time}"
        var script = cache.getScript(key)
        if (script.isNullOrEmpty()) {
            return
        }
        et_des.setText(script)
        et_des.setSelection(script.length)
    }

    fun saveScript() {
        var key = "${Store.store.getUser(context)!!.uid}+$tag+${week.start_time}+${week.end_time}"
        var script = et_des.text.toString().trim()
        var mDialog = MaterialDialog.Builder(this@WeeklyRecordActivity)
                .theme(Theme.LIGHT)
                .canceledOnTouchOutside(true)
                .customView(R.layout.dialog_yongyin, false).build()
        mDialog.show()
        val content = mDialog.find<TextView>(R.id.content)
        val cancel = mDialog.find<Button>(R.id.cancel)
        val yes = mDialog.find<Button>(R.id.yes)
        content.text = "是否需要保存草稿"
        cancel.text = "否"
        yes.text = "是"
        cancel.setOnClickListener {
            super.onBackPressed()
        }
        yes.setOnClickListener {
            showCustomToast(R.drawable.icon_toast_success, "草稿保存成功")
            cache.saveScript(key, script)
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        hideInput(et_des)
    }

    fun clearScript() {
        var key = "${Store.store.getUser(context)!!.uid}+$tag+${week.start_time}+${week.end_time}"
        cache.saveScript(key, "")
    }
}
