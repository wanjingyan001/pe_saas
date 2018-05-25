package com.sogukj.pe.module.project.operate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.BondBean
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_bond_info.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class BondInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: BondBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(BondBean::class.java.simpleName) as BondBean
        setContentView(R.layout.activity_bond_info)
        setBack(true)
        title = "债券信息"
        data.apply {
            tv_bondName.text = bondName
            tv_bondNum.text = bondNum
            tv_publisherName.text = publisherName
            tv_bondType.text = bondType
            tv_publishTime.text = publishTime
            tv_publishExpireTime.text = publishExpireTime
            tv_bondTradeTime.text = bondTradeTime
            tv_bondStopTime.text = bondStopTime
            tv_startCalInterestTime.text = startCalInterestTime
            tv_bondTimeLimit.text = bondTimeLimit
            tv_calInterestType.text = calInterestType
            tv_debtRating.text = debtRating
            tv_creditRatingGov.text = creditRatingGov
            tv_faceValue.text = faceValue
            tv_refInterestRate.text = refInterestRate
            tv_faceInterestRate.text = faceInterestRate
            tv_realIssuedQuantity.text = realIssuedQuantity
            tv_planIssuedQuantity.text = planIssuedQuantity
            tv_issuedPrice.text = issuedPrice
            tv_interestDiff.text = interestDiff
            tv_payInterestHZ.text = payInterestHZ
            tv_exeRightType.text = exeRightType
            tv_exeRightTime.text = exeRightTime
            tv_flowRange.text = flowRange
            tv_escrowAgent.text = escrowAgent
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: BondBean) {
            val intent = Intent(ctx, BondInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(BondBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}