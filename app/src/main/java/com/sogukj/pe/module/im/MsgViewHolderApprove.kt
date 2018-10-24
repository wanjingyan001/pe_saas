package com.sogukj.pe.module.im

import android.app.Activity
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.netease.nim.uikit.common.util.sys.ScreenUtil
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.project.originpro.OtherProjectShowActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity
import com.sogukj.pe.module.project.originpro.ProjectUploadShowActivity
import kotlinx.android.synthetic.main.item_im_approve_message.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor

/**
 * Created by admin on 2018/9/21.
 */
class MsgViewHolderApprove(adapter: BaseMultiItemFetchLoadAdapter<*, *>) : MsgViewHolderBase(adapter) {
    private lateinit var data: MessageBean
    override fun getContentResId(): Int = R.layout.item_im_approve_message

    override fun inflateContentView() {
        val width = (0.6 * ScreenUtil.screenWidth).toInt()
        setLayoutParams(width, (0.63 * width).toInt(), view.approveItem)
        view.approveItem.backgroundResource = 0
        view.approveItem.setPadding(0, 0, 0, 0)
        val params = view.approveType.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = Utils.dpToPx(context, 15)
        view.approveType.layoutParams = params
    }

    override fun bindContentView() {
        val attachment = message.attachment as ApproveAttachment
        data = attachment.messageBean
        view.approveType.text = data.title
//        view.approveNum.text = data.title
        view.sponsor.text = replaceText("发起人：", data.subName)
//        view.reason.text = replaceText("审批事由：", data.reasons)
        view.schedule.text = data.preapproval ?: "待审批"
        data.preapproval.isNullOrEmpty().yes {
            view.schedule.textColor = R.color.text_3
        }
//        if (!data.reasons.isNullOrEmpty()){
//            view.reason.visibility = View.VISIBLE
//        }else{
//            view.reason.visibility = View.GONE
//        }
//        if (data.title.isNullOrEmpty()){
//            view.approveNum.visibility = View.GONE
//        }else{
//            view.approveNum.visibility = View.VISIBLE
//        }
    }

    private fun replaceText(hintStr: String, str: String?): SpannableString {
        val spannable = SpannableString(hintStr + str)
        spannable.setSpan(ForegroundColorSpan(Color.parseColor("#A0A4AA"))
                , 0, hintStr.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

//    override fun leftBackground(): Int = 0
//
//    override fun rightBackground(): Int = 0

    override fun onItemLongClick(): Boolean {
        return true
    }

    override fun onItemClick() {
        super.onItemClick()
        val isMine = if (data.status == -1 || data.status == 4) 1 else 2
        when (data.type) {
            2 -> SealApproveActivity.start(context as Activity, data, isMine)
            3 -> SignApproveActivity.start(context as Activity, data, isMine)
            1 -> LeaveBusinessApproveActivity.start(context as Activity, data, isMine)//出差  SealApproveActivity
        }
        if (null != data.floor) {
            val floor = data.floor
            val project = ProjectBean()
            project.company_id = data.id
            project.floor = data.floor
            project.name = data.cName
            when (floor) {
                2 -> {
                    //立项
                    context.startActivity<ProjectApprovalShowActivity>(Extras.DATA to project, Extras.FLAG to floor)
                }
                3 -> {
                    //预审会
                    context.startActivity<ProjectUploadShowActivity>(Extras.DATA to project, Extras.FLAG to floor)
                }
                4 -> {
                    //投决会
                    context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                            Extras.TITLE to "投决会")
                }
                5 -> {
                    //签约付款
                    context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                            Extras.TITLE to "签约付款")
                }
                6 -> {
                    //投后管理
                    context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                            Extras.TITLE to "投后管理")
                }
                7 -> {
                    //退出管理
                    context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                            Extras.TITLE to "退出管理")
                }
            }
        }
    }

}