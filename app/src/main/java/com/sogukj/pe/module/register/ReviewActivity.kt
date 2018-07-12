package com.sogukj.pe.module.register

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.extraDelegate
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.MechanismInfo
import com.sogukj.pe.bean.RegisterVerResult
import com.sogukj.pe.interf.ReviewStatus
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_register_review.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColorResource

class ReviewActivity : ToolbarActivity() {
    private lateinit var status: ReviewStatus
    private var result: RegisterVerResult? = null
    private var userId: Int = 0
    private var mechanismInfo: MechanismInfo? = null

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
        result = intent.getParcelableExtra(Extras.BEAN)
        userId = intent.getIntExtra(Extras.DATA2, 0)

        when (status) {
            ReviewStatus.UNDER_REVIEW -> {
                reviewTitle.text = "已提交审核"
                reviewStatusImg.imageResource = R.mipmap.bg_under_review
                tipsIcon.visibility = View.VISIBLE
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
                tipsIcon.visibility = View.VISIBLE
                tipsIcon.imageResource = R.mipmap.icon_red_tips
                tipsContent.text = "很抱歉您的审核未通过"
                tipsContent.textColorResource = R.color.prompt_error
                failure_reason.setVisible(true)
                failure_reason.text = result?.reason
                joinNow.setVisible(true)
                joinNow.text = "返回修改申请信息"
                getMechanismInfo()
            }
        }
        ActivityHelper.finishAllWithoutTop()
        joinNow.clickWithTrigger {
            when (status) {
                ReviewStatus.SUCCESSFUL_REVIEW -> {
                    startActivity<UploadBasicInfoActivity>(Extras.NAME to result?.mechanism_name,
                            Extras.CODE to result?.phone)
                }
                ReviewStatus.FAILURE_REVIEW -> {
                    startActivity<InfoSupplementActivity>(Extras.DATA to result?.phone, Extras.DATA2 to mechanismInfo, Extras.ID to userId.toString())
                }
                else -> {

                }
            }
        }

    }


    private fun getMechanismInfo() {
        SoguApi.getService(application, RegisterService::class.java).getMechanismInfo(userId)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            mechanismInfo = payload.payload
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }
}
