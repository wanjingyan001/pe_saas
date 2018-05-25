package com.sogukj.pe.module.project

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.StoreProjectBean
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_cb_project.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class StoreProjectAddActivity : ToolbarActivity() {

    var type = 0
    var project: ProjectBean? = null
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean?
        type = intent.getIntExtra(Extras.TYPE, TYPE_VIEW)
        setContentView(R.layout.activity_add_cb_project)
        when (type) {
            TYPE_VIEW -> {
                title = project?.name
                btn_commit.visibility = View.GONE
                disable(et_company_name)
                disable(et_short_name)
                disable(et_info)
                disable(et_enterpriseType)
                disable(et_regCapital)
                disable(et_mainBusiness)
                disable(et_ownershipRatio)
                disable(et_lastYearIncome)
                disable(et_lastYearProfit)
                disable(et_thisYearIncome)
                disable(et_thisYearProfit)
                disable(et_lunci)
                disable(et_appraisement)
                disable(et_financeUse)
                disable(et_capitalPlan)
            }
            TYPE_ADD -> title = "添加储备项目"
            TYPE_EDIT -> title = "修改储备项目"
        }
        setBack(true)
        btn_commit.setOnClickListener {
            doSave()
        }
        if (type != TYPE_VIEW) {
            et_estiblishTime.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, { v, y, m, d ->
                    val strDate = "${y}-${m + 1}-${d}"
                    et_estiblishTime.text = strDate
                }, year, month, day).show()
            }
        }
        if (type != TYPE_ADD) {
            SoguApi.getService(application,NewService::class.java)
                    .getStoreProject(project!!.company_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            val bean = payload.payload
                            if (null != bean) {
                                fillForm(bean)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }

                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "保存失败")
                    })
        }
    }

    fun fillForm(bean: StoreProjectBean) {
        et_company_name.setText(bean.name)
        et_company_name.setSelection(bean.name!!.length)
        et_short_name.setText(bean.shortName)
        et_info.setText(bean.info)
        et_estiblishTime.text = bean.estiblishTime
        et_enterpriseType.setText(bean.enterpriseType)
        et_regCapital.setText(bean.regCapital)
        et_mainBusiness.setText(bean.mainBusiness)
        et_ownershipRatio.setText(bean.ownershipRatio)
        et_lastYearIncome.setText(bean.lastYearIncome)
        et_lastYearProfit.setText(bean.lastYearProfit)
        et_thisYearIncome.setText(bean.thisYearIncome)
        et_thisYearProfit.setText(bean.thisYearProfit)
        et_lunci.setText(bean.lunci)
        et_appraisement.setText(bean.appraisement)
        et_financeUse.setText(bean.financeUse)
        et_capitalPlan.setText(bean.capitalPlan)
    }

    fun disable(et: EditText) {
        et.isEnabled = false
    }

    fun tvalue(et: EditText): String {
        et.filters = Utils.getFilter(context)
        return et.text.trim().toString()
    }

    private fun doSave() {
        val bean = StoreProjectBean()
        bean.name = et_company_name.text.trim().toString()
        if (TextUtils.isEmpty(bean.name)) {
            showCustomToast(R.drawable.icon_toast_common, "公司名称不能为空")
            et_company_name.requestFocus()
            return
        }
        bean.shortName = et_short_name.text.trim().toString()
        bean.info = et_info.text.trim().toString()
        bean.estiblishTime = et_estiblishTime.text.trim().toString()
        if (TextUtils.isEmpty(bean.estiblishTime)) {
            bean.estiblishTime = null
        }
        bean.enterpriseType = et_enterpriseType.text.trim().toString()
        bean.regCapital = et_regCapital.text.trim().toString()
        bean.mainBusiness = et_mainBusiness.text.trim().toString()
        bean.ownershipRatio = tvalue(et_ownershipRatio)
        bean.lastYearIncome = tvalue(et_lastYearIncome)
        bean.lastYearProfit = tvalue(et_lastYearProfit)
        bean.thisYearIncome = tvalue(et_thisYearIncome)
        bean.thisYearProfit = tvalue(et_thisYearProfit)
        bean.lunci = tvalue(et_lunci)
        bean.appraisement = tvalue(et_appraisement)
        bean.financeUse = tvalue(et_financeUse)
        bean.capitalPlan = tvalue(et_capitalPlan)

        if (type == TYPE_ADD)
            SoguApi.getService(application,NewService::class.java)
                    .addStoreProject(name = bean.name!!
                            , shortName = bean.shortName!!
                            , info = bean.info
                            , estiblishTime = bean.estiblishTime
                            , enterpriseType = bean.enterpriseType
                            , regCapital = bean.regCapital
                            , mainBusiness = bean.mainBusiness
                            , ownershipRatio = bean.ownershipRatio
                            , lastYearIncome = bean.lastYearIncome
                            , lastYearProfit = bean.lastYearProfit
                            , thisYearIncome = bean.thisYearIncome
                            , thisYearProfit = bean.thisYearProfit
                            , lunci = bean.lunci
                            , appraisement = bean.appraisement
                            , financeUse = bean.financeUse
                            , capitalPlan = bean.capitalPlan)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "保存成功")
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }

                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "保存失败")
                    })
        else if (type == TYPE_EDIT)
            SoguApi.getService(application,NewService::class.java)
                    .editStoreProject(company_id = project!!.company_id!!
                            , name = bean.name!!
                            , shortName = bean.shortName!!
                            , info = bean.info
                            , estiblishTime = bean.estiblishTime
                            , enterpriseType = bean.enterpriseType
                            , regCapital = bean.regCapital
                            , mainBusiness = bean.mainBusiness
                            , ownershipRatio = bean.ownershipRatio
                            , lastYearIncome = bean.lastYearIncome
                            , lastYearProfit = bean.lastYearProfit
                            , thisYearIncome = bean.thisYearIncome
                            , thisYearProfit = bean.thisYearProfit
                            , lunci = bean.lunci
                            , appraisement = bean.appraisement
                            , financeUse = bean.financeUse
                            , capitalPlan = bean.capitalPlan)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "保存成功")
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }

                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "保存失败")
                    })
    }

    companion object {
        val TYPE_VIEW = 0
        val TYPE_EDIT = 1
        val TYPE_ADD = 2
        fun startAdd(ctx: Activity?) {
            val intent = Intent(ctx, StoreProjectAddActivity::class.java)
            intent.putExtra(Extras.TYPE, TYPE_ADD)
            ctx?.startActivity(intent)
        }

        fun startEdit(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, StoreProjectAddActivity::class.java)
            intent.putExtra(Extras.TYPE, TYPE_EDIT)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }

        fun startView(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, StoreProjectAddActivity::class.java)
            intent.putExtra(Extras.TYPE, TYPE_VIEW)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}