package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.ifNotNull
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.Industry
import com.sogukj.pe.bean.WorkEducationBean
import com.sogukj.pe.bean.WorkReqBean
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_work_expericence_add.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find
import java.util.*

class WorkExpericenceAddActivity : BaseActivity(), View.OnClickListener {
    private var workEducationBean: WorkEducationBean? = null
    var industry: Industry.Children = Industry().Children()
    var startTime: Date? = null
    var endTime: Date? = null

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, WorkExpericenceAddActivity::class.java)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }

        fun start(ctx: Activity?, workeducation: WorkEducationBean, tradeName: String?) {
            val intent = Intent(ctx, WorkExpericenceAddActivity::class.java)
            intent.putExtra(Extras.DATA, workeducation)
            intent.type = tradeName
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_expericence_add)
        Utils.setWindowStatusBarColor(this, R.color.white)
        val tradeName = intent.type
        workEducationBean = intent.getParcelableExtra<WorkEducationBean?>(Extras.DATA)
        workEducationBean?.let {
            setData(it, tradeName)
        }
        if (workEducationBean == null) {
            toolbar_title.text = "添加工作经历"
        } else {
            toolbar_title.text = "修改工作经历"
        }
        addTv.text = "保存"
        addTv.setOnClickListener(this)
        back.setOnClickListener(this)
        tv_start_date.setOnClickListener(this)
        tv_date_end.setOnClickListener(this)
        tv_industry.setOnClickListener(this)
        tv_workers.setOnClickListener(this)
        tv_nature.setOnClickListener(this)
    }

    fun setData(workEducationBean: WorkEducationBean, tradeName: String?) {
        tv_start_date.text = workEducationBean.employDate
        tv_date_end.text = workEducationBean.leaveDate
        tv_company.setText(workEducationBean.company)
        tv_skill.setText(workEducationBean.responsibility)
        tv_desc.setText(workEducationBean.jobInfo)
        tv_industry.text = tradeName
        tv_depart.setText(workEducationBean.department)
        tv_workers.text = workEducationBean.companyScale
        tv_nature.text = workEducationBean.companyProperty
        industry.id = workEducationBean.id
    }

    override fun onClick(v: View?) {
        val selectedDate = Calendar.getInstance()//系统当前时间
        val startDate = Calendar.getInstance()
        startDate.set(1949, 10, 1)
        val endDate = Calendar.getInstance()
        endDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH))
        when (v?.id) {
            R.id.back -> {
                onBackPressed()
            }
            R.id.addTv -> {
                //保存
                if (!tv_start_date.text.isNotEmpty()) {
                    showCustomToast(R.drawable.icon_toast_common, "请选择入职时间")
                    //showToast("请选择入职时间")
                    return
                }
//                if (!tv_date_end.text.isNotEmpty()) {
//                    showToast("请选择离职时间")
//                    return
//                }
                if (!tv_company.text.isNotEmpty()) {
                    showCustomToast(R.drawable.icon_toast_common, "请填写公司名称")
                    //showToast("请填写公司名称")
                    return
                }
                if (!tv_skill.text.isNotEmpty()) {
                    showCustomToast(R.drawable.icon_toast_common, "请填写职能")
                    //showToast("请填写职能")
                    return
                }
//                if (!tv_desc.text.isNotEmpty()) {
//                    showToast("请填写工作描述")
//                    return
//                }
                if (!tv_industry.text.isNotEmpty()) {
                    showCustomToast(R.drawable.icon_toast_common, "请选择行业")
                    //showToast("请选择行业")
                    return
                }
                if (!tv_depart.text.isNotEmpty()) {
                    showCustomToast(R.drawable.icon_toast_common, "请填写部门")
                    //showToast("请填写部门")
                    return
                }
//                if (!tv_workers.text.isNotEmpty()) {
//                    showToast("请选择公司规模")
//                    return
//                }
//                if (!tv_nature.text.isNotEmpty()) {
//                    showToast("请选择公司性质")
//                    return
//                }
                val reqBean = WorkReqBean()
                val workeducation = WorkEducationBean()
                workeducation.employDate = tv_start_date.text.toString()
                workeducation.leaveDate = tv_date_end.text.toString()
                workeducation.company = tv_company.text.toString()
                workeducation.responsibility = tv_skill.text.toString()
                workeducation.jobInfo = tv_desc.text.toString()
                if (industry.id != null) {
                    workeducation.trade = industry.id!!
                    workeducation.trade_name = tv_industry.text.toString()
                }
                workeducation.department = tv_depart.text.toString()
                workeducation.companyProperty = tv_nature.text.toString()
                workeducation.companyScale = tv_workers.text.toString()
                if (workEducationBean == null) {
                    reqBean.ae = workeducation
                    reqBean.type = 2
                    doRequest(reqBean)
                } else {
                    workeducation.id = workEducationBean!!.id
                    industry.pid?.let {
                        workeducation.pid = it
                    }
                    reqBean.ae = workeducation
                    reqBean.type = 2
                    doRequest2(reqBean)
                }
            }
            R.id.tv_start_date -> {
                //入职时间
                val timePicker = TimePickerBuilder(this, { date, view ->
                    startTime = date
                    if (startTime != null && endTime != null) {
                        if (startTime!!.time > endTime!!.time) {
                            showErrorToast("入职时间不能大于离职时间")
                        } else {
                            tv_start_date.text = Utils.getTime(date)
                        }
                    } else {
                        tv_start_date.text = Utils.getTime(date)
                    }
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, false, false, false, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentTextSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .setDecorView(window.decorView.find(android.R.id.content))
                        .build()
                timePicker.show()
            }
            R.id.tv_date_end -> {
                //离职时间
                val timePicker = TimePickerBuilder(this, { date, view ->
                    endTime = date
                    ifNotNull(startTime, endTime) { v1, v2 ->
                        if (v1.time > v2.time) {
                            showErrorToast("入职时间不能大于离职时间")
                            return@ifNotNull
                        } else {
                            tv_date_end.text = Utils.getTime(date)
                        }
                    }
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, false, false, false, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentTextSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .setDecorView(window.decorView.find(android.R.id.content))
                        .build()
                timePicker.show()
            }
            R.id.tv_industry -> {
                //行业
                IndustryActivity.start(this)
            }
            R.id.tv_workers -> {
                //公司规模
                val dataList = resources.getStringArray(R.array.workers).toList()
                val pvOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, option2, options3, v ->
                    tv_workers.text = dataList.get(options1)
                    gsgmIndex = options1
                }).setDecorView(window.decorView.find(android.R.id.content)).build<String>()
                pvOptions.setPicker(dataList, null, null)
                pvOptions.setSelectOptions(gsgmIndex)
                pvOptions.show()
            }
            R.id.tv_nature -> {
                //公司性质
                val dataList = resources.getStringArray(R.array.BusinessNature).toList()
                val pvOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, option2, options3, v ->
                    tv_nature.text = dataList.get(options1)
                    gsxzIndex = options1
                }) .setDecorView(window.decorView.find(android.R.id.content)).build<String>()
                pvOptions.setPicker(dataList)
                pvOptions.setSelectOptions(gsxzIndex)
                pvOptions.show()
            }
        }
    }

    var gsgmIndex = 0
    var gsxzIndex = 0


    private fun doRequest(reqBean: WorkReqBean) {
        SoguApi.getService(application,UserService::class.java)
                .addWorkExperience(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            val intent = Intent()
                            intent.type = this.trade_name
                            intent.putExtra(Extras.LIST, this)
                            setResult(Extras.RESULTCODE, intent)
                            finish()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, { hideProgress() }, {
                    showProgress("正在保存,请稍后")
                })
    }


    private fun doRequest2(reqBean: WorkReqBean) {
        SoguApi.getService(application,UserService::class.java)
                .editExperience(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            val intent = Intent()
                            intent.type = this.trade_name
                            intent.putExtra(Extras.LIST, this)
                            setResult(Extras.RESULTCODE, intent)
                            finish()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, { hideProgress() }, {
                    showProgress("正在保存修改,请稍后")
                })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            industry = data.getSerializableExtra(Extras.DATA) as Industry.Children
            industry.let {
                tv_industry.text = it.name
            }
        }
    }
}
