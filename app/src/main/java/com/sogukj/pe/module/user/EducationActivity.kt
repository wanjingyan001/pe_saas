package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bigkoo.pickerview.OptionsPickerView
import com.bigkoo.pickerview.TimePickerView
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.EducationBean
import com.sogukj.pe.bean.EducationReqBean
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_education.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import java.util.*

class EducationActivity : BaseActivity(), View.OnClickListener {
    private var educationBean: EducationBean? = null

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, EducationActivity::class.java)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }

        fun start(ctx: Activity?, education: EducationBean) {
            val intent = Intent(ctx, EducationActivity::class.java)
            intent.putExtra(Extras.DATA, education)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_education)
        Utils.setWindowStatusBarColor(this, R.color.white)
        educationBean = intent.getParcelableExtra<EducationBean?>(Extras.DATA)
        educationBean?.let {
            setData(it)
        }
        if (educationBean == null) {
            toolbar_title.text = "添加教育经历"
        } else {
            toolbar_title.text = "修改教育经历"
        }
        addTv.text = "保存"
        addTv.setOnClickListener(this)
        back.setOnClickListener(this)
        tv_start_date.setOnClickListener(this)
        tv_date_end.setOnClickListener(this)
        tv_education.setOnClickListener(this)
    }

    fun setData(educationBean: EducationBean) {
        tv_start_date.text = educationBean.toSchoolDate
        tv_date_end.text = educationBean.graduationDate
        tv_school.setText(educationBean.school)
        tv_education.text = educationBean.education
        tv_profession.setText(educationBean.major)
        tv_description.setText(educationBean.majorInfo)
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
                    //showToast("请选择入学时间")
                    showCustomToast(R.drawable.icon_toast_common, "请选择入学时间")
                    return
                }
                if (!tv_date_end.text.isNotEmpty()) {
                    //showToast("请选择毕业时间")
                    showCustomToast(R.drawable.icon_toast_common, "请选择毕业时间")
                    return
                }
                if (!tv_school.text.isNotEmpty()) {
                    //showToast("请输入学校")
                    showCustomToast(R.drawable.icon_toast_common, "请输入学校")
                    return
                }
                if (!tv_education.text.isNotEmpty()) {
                    //showToast("请选择学历")
                    showCustomToast(R.drawable.icon_toast_common, "请选择学历")
                    return
                }
                if (!tv_profession.text.isNotEmpty()) {
                    //showToast("请输入专业")
                    showCustomToast(R.drawable.icon_toast_common, "请输入专业")
                    return
                }
                val education = EducationBean()
                val reqBean = EducationReqBean()
                education.toSchoolDate = tv_start_date.text.toString()
                education.graduationDate = tv_date_end.text.toString()
                education.school = tv_school.text.toString()
                education.education = tv_education.text.toString()
                education.major = tv_profession.text.toString()
                education.majorInfo = tv_description.text.toString()
                if (educationBean == null) {
                    reqBean.ae = education
                    reqBean.type = 1
                    doRequest(reqBean)
                } else {
                    education.id = educationBean!!.id
                    reqBean.ae = education
                    reqBean.type = 1
                    Log.d("WJY",Gson().toJson(educationBean))
                    Log.d("WJY",Gson().toJson(reqBean))
                    doRequest2(reqBean)
                }
            }
            R.id.tv_start_date -> {
                //入学时间
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    tv_start_date.text = Utils.getTime(date)
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, false, false, false, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()
            }
            R.id.tv_date_end -> {
                //毕业时间
                val timePicker = TimePickerView.Builder(this, { date, view ->
                    tv_date_end.text = Utils.getTime(date)
                })
                        //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(booleanArrayOf(true, true, false, false, false, false))
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(selectedDate)
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                timePicker.show()
            }
            R.id.tv_education -> {
                //学历
//                MaterialDialog.Builder(this)
//                        .title("选择学历")
//                        .theme(Theme.LIGHT)
//                        .items(resources.getStringArray(R.array.Education).toList())
//                        .itemsCallbackSingleChoice(0) { dialog, itemView, which, text ->
//                            tv_education.text = text
//                            true
//                        }
//                        .positiveText("确定")
//                        .negativeText("取消")
//                        .show()
                var dataList = resources.getStringArray(R.array.Education).toList()
                var pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                    tv_education.text = dataList.get(options1)
                    index = options1
                }).build()
                pvOptions.setPicker(dataList, null, null)
                pvOptions.setSelectOptions(index)
                pvOptions.show()
            }
        }
    }

    var index = 0

    fun doRequest(reqBean: EducationReqBean) {
        SoguApi.getService(application,UserService::class.java)
                .addExperience(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            val intent = Intent()
                            intent.putExtra(Extras.LIST2, this)
                            setResult(Extras.RESULTCODE2, intent)
                            finish()
                        }
                    } else {
                        //showToast(payload.message)
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    hideProgress()
                }, {
                    hideProgress()
                }, {
                    showProgress("正在保存,请稍后")
                })
    }

    private fun doRequest2(reqBean: EducationReqBean) {
        SoguApi.getService(application,UserService::class.java)
                .editExperience(reqBean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            val intent = Intent()
                            intent.putExtra(Extras.LIST2, this)
                            setResult(Extras.RESULTCODE2, intent)
                            finish()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    hideProgress()
                }, {
                    hideProgress()
                }, {
                    showProgress("正在保存修改,请稍后")
                })

    }
}
