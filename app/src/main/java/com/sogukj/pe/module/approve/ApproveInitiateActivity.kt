package com.sogukj.pe.module.approve

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.amap.api.mapcore.util.it
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.controlView.*
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_approve_initiate.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.info

class ApproveInitiateActivity : ToolbarActivity() {
    private val tid: Int by extraDelegate(Extras.ID, 0)//模板id
    private val aid: Int? by extraDelegate(Extras.ID2)//修改的审批id
    private val titleName: String by extraDelegate(Extras.NAME, "审批")//标题

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_initiate)
        title = titleName
        setBack(true)
        showTemplate()
        getApprovers()
    }

    /**
     * 展示模板
     */
    private fun showTemplate() {
        SoguApi.getService(application, ApproveService::class.java)
                .showTemplate(tid, aid)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                it.forEach {
                                    info { it.jsonStr }
                                    val factory = ControlFactory(this@ApproveInitiateActivity)
                                    val control = when (it.control) {
                                        1 -> factory.createControl(SingLineInput::class.java, it)
                                        2 -> factory.createControl(MultiLineInput::class.java, it)
                                        3 -> factory.createControl(NumberInput::class.java, it)
                                        4 -> factory.createControl(SingSelection::class.java, it)
                                        5 -> factory.createControl(MultiSelection::class.java, it)
                                        6 -> factory.createControl(DateSelection::class.java, it)
                                        7 -> factory.createControl(PhoneSelection::class.java, it)
                                        8 -> factory.createControl(NoticText::class.java, it)
                                        9 -> factory.createControl(MoneyInput::class.java, it)
                                        10 -> factory.createControl(AttachmentSelection::class.java, it)
                                        11 -> factory.createControl(ContactSelection::class.java, it)
                                        12 -> factory.createControl(DepartmentControl::class.java, it)
                                    //13当前地点暂时不做
                                        14 -> factory.createControl(DocumentAssociate::class.java, it)
                                        15 -> factory.createControl(SealSelection::class.java, it)
                                        16 -> factory.createControl(RadioControl::class.java, it)
                                        17 -> factory.createControl(CheckBoxControl::class.java, it)
                                        18 -> {
                                            val project = factory.createControl(ProjectSelection::class.java, it)
                                            (project as ProjectSelection).block = { value ->
                                                getApprovers(projectId = value.id)
                                            }
                                            project
                                        }
                                        19 -> {
                                            val fund = factory.createControl(FundSelection::class.java, it)
                                            (fund as FundSelection).block = {
                                                getApprovers(fundId = it.id)
                                            }
                                            fund
                                        }
                                        20 -> factory.createControl(ForeignControl::class.java, it)
                                        21 -> {
                                            val fap = factory.createControl(FAPControl::class.java, it)
                                            (fap as FAPControl).block = { v1, v2 ->
                                                getApprovers(fundId = v1, projectId = v2)
                                            }
                                            fap
                                        }
                                        22 -> factory.createControl(DateRangeControl::class.java, it)
                                        24 -> factory.createControl(SMSNotification::class.java, it)
                                        25 -> factory.createControl(CitySelection::class.java, it)
                                        -1 -> factory.createControl(LeaveControl::class.java, it)
                                        -2 -> factory.createControl(TravelControl::class.java, it)
                                        -3 -> factory.createControl(GoOutControl::class.java, it)
                                        -21 -> {
                                            val fs = factory.createControl(FundSealControl::class.java, it)
                                            (fs as FundSealControl).block = { v1, v2 ->
                                                getApprovers(fundId = v1, projectId = v2)
                                            }
                                            fs
                                        }
                                        -22 -> factory.createControl(CompanySealControl::class.java, it)
                                        -23 -> factory.createControl(ForeignSealControl::class.java, it)
                                        -41 -> factory.createControl(ReimburseControl::class.java, it)
                                        else -> return@forEach
                                    }
                                    val view = View(ctx)
                                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(10))
                                    view.layoutParams = lp
                                    controlLayout.addView(view)
                                    controlLayout.addView(control)

                                }
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }


    private fun getApprovers(fundId: String? = null, projectId: String? = null) {
        SoguApi.getService(application, ApproveService::class.java)
                .getApprovers(tid, fund_id = fundId, project_id = projectId)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                info { it.jsonStr }
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (0 until controlLayout.childCount).forEach {
            if (controlLayout.getChildAt(it) is PhoneSelection) {
                (controlLayout.getChildAt(it) as PhoneSelection).onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
