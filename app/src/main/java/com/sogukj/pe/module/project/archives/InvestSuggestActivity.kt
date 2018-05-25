package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invest_suggest.*

class InvestSuggestActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invest_suggest)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        title = project.name
        setBack(true)

        //
//        project.company_id = 1

        project.company_id?.let {
            load(it)
        }

        btn_commit.setOnClickListener {
            upload()
        }
    }

    fun load(it: Int) {
        SoguApi.getService(application,InfoService::class.java)
                .investSuggest(it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var data = payload.payload
                        data?.apply {
                            et_valuation.setText(plan?.valuation)
                            et_amount.setText(plan?.amount)
                            et_equityRatio.setText(plan?.equityRatio)
                            et_mainCast.setText(plan?.mainCast)
                            et_position.setText(plan?.position)
                            et_estimateTime.setText(plan?.estimateTime)

                            et_motivation.setText(other?.motivation)
                            et_riskTreat.setText(other?.riskTreat)
                            et_managePlan.setText(other?.managePlan)
                            et_quitPlan.setText(other?.quitPlan)
                            et_sensibilyAnalysis.setText(other?.sensibilyAnalysis)
                            et_otherIssue.setText(other?.otherIssue)
                            et_contract.setText(other?.contract)

                            if (plan?.valuation == null) {
                                et_valuation.setSelection(0)
                            } else {
                                et_valuation.setSelection(plan?.valuation!!.length)
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

        var investBean = HashMap<String, String>()
        investBean.put("valuation", et_valuation.text.toString())
        investBean.put("amount", et_amount.text.toString())
        investBean.put("equityRatio", et_equityRatio.text.toString())
        investBean.put("mainCast", et_mainCast.text.toString())
        investBean.put("position", et_position.text.toString())
        investBean.put("estimateTime", et_estimateTime.text.toString())
        investBean.put("motivation", et_motivation.text.toString())
        investBean.put("riskTreat", et_riskTreat.text.toString())
        investBean.put("managePlan", et_managePlan.text.toString())
        investBean.put("quitPlan", et_quitPlan.text.toString())
        investBean.put("sensibilyAnalysis", et_sensibilyAnalysis.text.toString())
        investBean.put("otherIssue", et_otherIssue.text.toString())
        investBean.put("contract", et_contract.text.toString())
        if (isTotalEmpty) {
            for ((k, v) in investBean) {
                if (!v.isNullOrEmpty()) {
                    isTotalEmpty = false
                }
            }
        }
        paramMap.put("ae", investBean)
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
            SoguApi.getService(application,InfoService::class.java)
                    .addEditInvestSuggest(paramMap)
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
            val intent = Intent(ctx, InvestSuggestActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
