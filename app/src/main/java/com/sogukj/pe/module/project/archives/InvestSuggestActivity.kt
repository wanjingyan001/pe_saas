package com.sogukj.pe.module.project.archives

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invest_suggest.*

class InvestSuggestActivity : ToolbarActivity(), TextWatcher, View.OnClickListener {
    lateinit var project: ProjectBean
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invest_suggest)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        title = project.name
        setBack(true)
        btn_commit.setBackgroundResource(R.drawable.bg_btn_gray)
        //
//        project.company_id = 1

        project.company_id?.let {
            load(it)
        }
        bindListener()

    }

    private fun bindListener() {
        btn_commit.setOnClickListener {
            upload()
        }
        et_valuation.addTextChangedListener(this)
        et_amount.addTextChangedListener(this)
        et_equityRatio.addTextChangedListener(this)
        et_mainCast.addTextChangedListener(this)
        et_position.addTextChangedListener(this)
        et_estimateTime.addTextChangedListener(this)

        et_motivation.addTextChangedListener(this)
        et_riskTreat.addTextChangedListener(this)
        et_managePlan.addTextChangedListener(this)
        et_quitPlan.addTextChangedListener(this)
        et_sensibilyAnalysis.addTextChangedListener(this)
        et_otherIssue.addTextChangedListener(this)
        et_contract.addTextChangedListener(this)

        tv_valuation.setOnClickListener(this)
        tv_amount.setOnClickListener(this)
        tv_equityRatio.setOnClickListener(this)
        tv_mainCast.setOnClickListener(this)
        tv_position.setOnClickListener(this)
        tv_estimateTime.setOnClickListener(this)
        tv_motivation.setOnClickListener(this)
        tv_riskTreat.setOnClickListener(this)
        tv_managePlan.setOnClickListener(this)
        tv_quitPlan.setOnClickListener(this)
        tv_sensibilyAnalysis.setOnClickListener(this)
        tv_otherIssue.setOnClickListener(this)
        tv_contract.setOnClickListener(this)

    }

    @SuppressLint("NewApi")
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.tv_valuation -> {
                Utils.showSoftInputFromWindow(this,et_valuation)
            }
            R.id.tv_amount -> {
                Utils.showSoftInputFromWindow(this,et_amount)
            }
            R.id.tv_equityRatio -> {
                Utils.showSoftInputFromWindow(this,et_equityRatio)
            }
            R.id.tv_mainCast -> {
                Utils.showSoftInputFromWindow(this,et_mainCast)
            }
            R.id.tv_position -> {
                Utils.showSoftInputFromWindow(this,et_position)
            }
            R.id.tv_estimateTime -> {
                Utils.showSoftInputFromWindow(this,et_estimateTime)
            }
            R.id.tv_motivation -> {
                Utils.showSoftInputFromWindow(this,et_motivation)
            }
            R.id.tv_riskTreat -> {
                Utils.showSoftInputFromWindow(this,et_riskTreat)
            }
            R.id.tv_managePlan -> {
                Utils.showSoftInputFromWindow(this,et_managePlan)
            }
            R.id.tv_quitPlan -> {
                Utils.showSoftInputFromWindow(this,et_quitPlan)
            }
            R.id.tv_sensibilyAnalysis -> {
                Utils.showSoftInputFromWindow(this,et_sensibilyAnalysis)
            }
            R.id.tv_otherIssue -> {
                Utils.showSoftInputFromWindow(this,et_otherIssue)
            }
            R.id.tv_contract -> {
                Utils.showSoftInputFromWindow(this,et_contract)
            }
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
                showCustomToast(R.drawable.icon_toast_common, "请先填写数据再提交")
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

    override fun onPause() {
        super.onPause()
        if (null != et_valuation){
            Utils.closeInput(this,et_valuation)
        }
    }
    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, InvestSuggestActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        Log.e("TAG","sssss ==" + s.toString())
        setComitStatus(s)
    }

    private fun setComitStatus(s: Editable?) {
        if ("".equals(s.toString())){
            //遍历判断
            checkParamStatus()
        }else{
            btn_commit.setBackgroundResource(R.drawable.bg_btn_blue)
        }
    }
    private fun checkParamStatus() {
        var isCheckEmpty = true
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
        for ((k, v) in investBean) {
                if (!v.isNullOrEmpty()) {
                    isCheckEmpty = false
                }
            }
        if (isCheckEmpty){
            btn_commit.setBackgroundResource(R.drawable.bg_btn_gray)
        }else{
            btn_commit.setBackgroundResource(R.drawable.bg_btn_blue)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}
