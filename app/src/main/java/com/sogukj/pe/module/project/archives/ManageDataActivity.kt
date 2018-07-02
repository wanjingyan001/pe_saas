package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manage_data.*

@Route(path = ARouterPath.ManageDataActivity)
class ManageDataActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_data)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        title = project.name
        setBack(true)
        project.company_id?.let {
            load(it)
        }
        btn_commit.setOnClickListener {
            upload()
        }
    }

    fun load(it: Int) {
        SoguApi.getService(application, InfoService::class.java)
                .manageData(it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var data = payload.payload
                        data?.apply {
                            et_majorEvents.setText(majorEvents)
                            et_recentFinance.setText(recentFinance)
                            et_tendency.setText(tendency)
                            et_policyIssues.setText(policyIssues)
                            et_clause.setText(clause)
                            et_opinion.setText(opinion)
                            et_bonus.setText(bonus)

                            if (majorEvents == null) {
                                et_majorEvents.setSelection(0)
                            } else {
                                et_majorEvents.setSelection(majorEvents!!.length)
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    val paramMap = HashMap<String, Any>()

    fun prepareParams(it: Int) {
        paramMap.put("company_id", "$it")

        var manageDataBean = HashMap<String, String>()
        manageDataBean.put("majorEvents", et_majorEvents.text.toString())
        manageDataBean.put("recentFinance", et_recentFinance.text.toString())
        manageDataBean.put("tendency", et_tendency.text.toString())
        manageDataBean.put("policyIssues", et_policyIssues.text.toString())
        manageDataBean.put("clause", et_clause.text.toString())
        manageDataBean.put("opinion", et_opinion.text.toString())
        manageDataBean.put("bonus", et_bonus.text.toString())
        if (isTotalEmpty) {
            for ((k, v) in manageDataBean) {
                if (!v.isNullOrEmpty()) {
                    isTotalEmpty = false
                }
            }
        }
        paramMap.put("ae", manageDataBean)
    }

    var isTotalEmpty = true

    fun upload() {
        project.company_id?.let {
            prepareParams(it)
            if (isTotalEmpty) {
                showCustomToast(R.drawable.icon_toast_common, "未填写任何数据")
                finish()
                return
            }
            SoguApi.getService(application, InfoService::class.java)
                    .addEditManageData(paramMap)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            finish()
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "保存失败")
                    })
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ManageDataActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
