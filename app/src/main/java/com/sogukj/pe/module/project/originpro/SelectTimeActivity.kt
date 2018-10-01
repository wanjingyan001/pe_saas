package com.sogukj.pe.module.project.originpro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.widgets.TimeSelectorView
import kotlinx.android.synthetic.main.activity_select_time.*
import java.util.*

/**
 * Created by CH-ZH on 2018/9/30.
 */
class SelectTimeActivity : ToolbarActivity() {
    private var selectDate : Date ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_time)
        selectDate = intent.getSerializableExtra(Extras.DATA) as Date?
        initView()
    }

    private fun initView() {
        setBack(true)
        setTitle("选择时间")
        if (null == selectDate){
            selectDate = Date()
        }
        tsview.show(selectDate, TimeSelectorView.OnTimeClick { date ->
            if (null != date){
                doFinish(date)
            }
        })
    }
    fun doFinish(date: Date) {
        val intent = Intent()
        intent.putExtra(Extras.DATA, date)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        fun start(ctx: Activity?, date: Date, code: Int) {
            val intent = Intent(ctx, SelectTimeActivity::class.java)
            intent.putExtra(Extras.DATA, date)
            ctx?.startActivityForResult(intent, code)
        }
    }
}