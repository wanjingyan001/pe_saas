package com.sogukj.pe.module.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.interf.ReviewStatus
import kotlinx.android.synthetic.main.activity_register_review.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColorResource

class ReviewActivity : ToolbarActivity() {
    private lateinit var status: ReviewStatus


    companion object {
        fun start(context: Context, status: ReviewStatus) {
            val intent = Intent(context, ReviewActivity::class.java)
            intent.putExtra(Extras.DATA, status)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_review)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        status = intent.getSerializableExtra(Extras.DATA) as ReviewStatus

        when (status) {
            ReviewStatus.UNDER_REVIEW -> {
                reviewTitle.text = "已提交审核"
                reviewStatusImg.imageResource = R.mipmap.bg_under_review
                tipsIcon.imageResource = R.mipmap.icon_blue_tips
                tipsContent.text = resources.getText(R.string.tips_under_review)
            }
            ReviewStatus.SUCCESSFUL_REVIEW -> {
                reviewTitle.text = "审核通过"
                reviewStatusImg.imageResource = R.mipmap.bg_success_review
                tips.setVisible(true)
                joinNow.setVisible(true)
            }
            ReviewStatus.FAILURE_REVIEW -> {
                reviewTitle.text = "审核失败"
                reviewStatusImg.imageResource = R.mipmap.bg_under_review
                tipsIcon.imageResource = R.mipmap.icon_red_tips
                tipsContent.text = "很抱歉您的审核未通过"
                tipsContent.textColorResource = R.color.prompt_error
                failure_reason.setVisible(true)
                failure_reason.text = "未通过的原因 \n1、凑字数的原因 \n2、因此我们需要对您填写的信息真实性进行审核"
                joinNow.setVisible(true)
                joinNow.text = "返回修改申请信息"
            }
        }

        joinNow.clickWithTrigger {
            when (status) {
                ReviewStatus.SUCCESSFUL_REVIEW -> {

                }
                ReviewStatus.FAILURE_REVIEW -> {

                }
                else->{

                }
            }
        }
    }
}
