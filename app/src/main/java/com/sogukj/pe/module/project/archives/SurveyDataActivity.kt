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
import kotlinx.android.synthetic.main.activity_survey_data.*

@Route(path = ARouterPath.SurveyDataActivity)
class SurveyDataActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_data)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        title = project.name
        setBack(true)

        // test
//        project.company_id = 1
        //

        project.company_id?.let {
            load(it)
        }

        btn_commit.setOnClickListener {
            upload()
        }
    }

    fun load(it: Int) {
        SoguApi.getService(application, InfoService::class.java)
                .surveyData(it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var data = payload.payload
                        data?.apply {
                            et_developStage.setText(company?.developStage)
                            et_generalStrategy.setText(company?.generalStrategy)
                            et_mainBusiness.setText(company?.mainBusiness)
                            et_porterAnalysis.setText(company?.porterAnalysis)
                            et_productTechnique.setText(company?.productTechnique)
                            et_occupancy.setText(company?.occupancy)
                            et_marketAbility.setText(company?.marketAbility)
                            et_execution.setText(company?.execution)
                            et_contrast.setText(company?.contrast)
                            et_humanResource.setText(company?.humanResource)
                            et_culture.setText(company?.culture)
                            et_customerAnalysis.setText(company?.customerAnalysis)
                            et_financeSituation.setText(company?.financeSituation)
                            et_financeFile.setText(company?.financeFile)
                            et_potentialDebt.setText(company?.potentialDebt)
                            et_marketBarriers.setText(company?.marketBarriers)
                            et_riskChance.setText(company?.riskChance)

                            et_finance.setText(system?.finance)
                            et_staff.setText(system?.staff)
                            et_riskControl.setText(system?.riskControl)
                            et_business.setText(system?.business)
                            et_conference.setText(system?.conference)
                            et_managerOffice.setText(system?.managerOffice)
                            et_supplyChain.setText(system?.supplyChain)
                            et_sell.setText(system?.sell)
                            et_production.setText(system?.production)
                            et_quality.setText(system?.quality)
                            et_seal.setText(system?.seal)

                            et_history.setText(law?.history)
                            et_operation.setText(law?.operation)
                            et_compliance.setText(law?.compliance)
                            et_equityStructure.setText(law?.equityStructure)
                            et_invisibleProperty.setText(law?.invisibleProperty)
                            et_legalDispute.setText(law?.legalDispute)
                            et_otherRisk.setText(law?.otherRisk)

                            et_developHistory.setText(vocation?.developHistory)
                            et_industryChain.setText(vocation?.industryChain)
                            et_scale.setText(vocation?.scale)
                            et_affinity.setText(vocation?.affinity)
                            et_increaseRate.setText(vocation?.increaseRate)
                            et_prosperity.setText(vocation?.prosperity)
                            et_legal_regulatory.setText(vocation?.legal_regulatory)
                            et_compete.setText(vocation?.compete)
                            et_drive_factory.setText(vocation?.drive_factory)
                            et_relate.setText(vocation?.relate)
                            et_report.setText(vocation?.report)
                            et_tradeRisk.setText(vocation?.tradeRisk)

                            et_details.setText(team?.details)
                            et_behaviour.setText(team?.behaviour)
                            et_family.setText(team?.family)
                            et_assetLiability.setText(team?.assetLiability)
                            et_characteristic.setText(team?.characteristic)
                            et_contract.setText(team?.contract)
                            et_praise.setText(team?.praise)
                            et_achievement.setText(team?.achievement)
                            et_shortcoming.setText(team?.shortcoming)
                            et_entrepreneur.setText(team?.entrepreneur)
                            et_introduce.setText(team?.introduce)
                            et_encourage.setText(team?.encourage)

                            if (company?.developStage == null) {
                                et_developStage.setSelection(0)
                            } else {
                                et_developStage.setSelection(company?.developStage!!.length)
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

    private fun prepareParams(it: Int) {
        paramMap.put("company_id", "$it")

        var company = HashMap<String, String>()
        company.put("developStage", et_developStage.text.toString())
        company.put("generalStrategy", et_generalStrategy.text.toString())
        company.put("mainBusiness", et_mainBusiness.text.toString())
        company.put("porterAnalysis", et_porterAnalysis.text.toString())
        company.put("productTechnique", et_productTechnique.text.toString())
        company.put("occupancy", et_occupancy.text.toString())
        company.put("marketAbility", et_marketAbility.text.toString())
        company.put("execution", et_execution.text.toString())
        company.put("contrast", et_contrast.text.toString())
        company.put("humanResource", et_humanResource.text.toString())
        company.put("culture", et_culture.text.toString())
        company.put("customerAnalysis", et_customerAnalysis.text.toString())
        company.put("financeSituation", et_financeSituation.text.toString())
        company.put("financeFile", et_financeFile.text.toString())
        company.put("potentialDebt", et_potentialDebt.text.toString())
        company.put("marketBarriers", et_marketBarriers.text.toString())
        company.put("riskChance", et_riskChance.text.toString())
        if (isTotalEmpty) {
            for ((k, v) in company) {
                if (!v.isNullOrEmpty()) {
                    isTotalEmpty = false
                }
            }
        }
        paramMap.put("ac", company)

        var system = HashMap<String, String>()
        system.put("finance", et_finance.text.toString())
        system.put("staff", et_staff.text.toString())
        system.put("riskControl", et_riskControl.text.toString())
        system.put("business", et_business.text.toString())
        system.put("conference", et_conference.text.toString())
        system.put("managerOffice", et_managerOffice.text.toString())
        system.put("supplyChain", et_supplyChain.text.toString())
        system.put("sell", et_sell.text.toString())
        system.put("production", et_production.text.toString())
        system.put("quality", et_quality.text.toString())
        system.put("seal", et_seal.text.toString())
        if (isTotalEmpty) {
            for ((k, v) in system) {
                if (!v.isNullOrEmpty()) {
                    isTotalEmpty = false
                }
            }
        }
        paramMap.put("as", system)

        var law = HashMap<String, String>()
        law.put("history", et_history.text.toString())
        law.put("operation", et_operation.text.toString())
        law.put("compliance", et_compliance.text.toString())
        law.put("equityStructure", et_equityStructure.text.toString())
        law.put("invisibleProperty", et_invisibleProperty.text.toString())
        law.put("legalDispute", et_legalDispute.text.toString())
        law.put("otherRisk", et_otherRisk.text.toString())
        if (isTotalEmpty) {
            for ((k, v) in law) {
                if (!v.isNullOrEmpty()) {
                    isTotalEmpty = false
                }
            }
        }
        paramMap.put("al", law)

        var vocation = HashMap<String, String>()
        vocation.put("developHistory", et_developHistory.text.toString())
        vocation.put("industryChain", et_industryChain.text.toString())
        vocation.put("scale", et_scale.text.toString())
        vocation.put("affinity", et_affinity.text.toString())
        vocation.put("increaseRate", et_increaseRate.text.toString())
        vocation.put("prosperity", et_prosperity.text.toString())
        vocation.put("legal_regulatory", et_legal_regulatory.text.toString())
        vocation.put("compete", et_compete.text.toString())
        vocation.put("drive_factory", et_drive_factory.text.toString())
        vocation.put("relate", et_relate.text.toString())
        vocation.put("report", et_report.text.toString())
        vocation.put("tradeRisk", et_tradeRisk.text.toString())
        if (isTotalEmpty) {
            for ((k, v) in vocation) {
                if (!v.isNullOrEmpty()) {
                    isTotalEmpty = false
                }
            }
        }
        paramMap.put("av", vocation)

        var team = HashMap<String, String>()
        team.put("details", et_details.text.toString())
        team.put("behaviour", et_behaviour.text.toString())
        team.put("family", et_family.text.toString())
        team.put("assetLiability", et_assetLiability.text.toString())
        team.put("characteristic", et_characteristic.text.toString())
        team.put("contract", et_contract.text.toString())
        team.put("praise", et_praise.text.toString())
        team.put("achievement", et_achievement.text.toString())
        team.put("shortcoming", et_conference.text.toString())
        team.put("entrepreneur", et_entrepreneur.text.toString())
        team.put("introduce", et_introduce.text.toString())
        team.put("encourage", et_encourage.text.toString())
        if (isTotalEmpty) {
            for ((k, v) in team) {
                if (!v.isNullOrEmpty()) {
                    isTotalEmpty = false
                }
            }
        }
        paramMap.put("at", team)
    }

    var isTotalEmpty = true

    private fun upload() {
        project.company_id?.let {
            prepareParams(it)
            if (isTotalEmpty) {
                showCustomToast(R.drawable.icon_toast_common, "未填写任何数据")
                finish()
                return
            }
            SoguApi.getService(application, InfoService::class.java)
                    .addEditSurveyData(paramMap)
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
            val intent = Intent(ctx, SurveyDataActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
