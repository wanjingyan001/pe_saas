package com.sogukj.pe.module.im

import android.app.Activity
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.FrameLayout
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.netease.nim.uikit.common.util.sys.ScreenUtil
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.no
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import kotlinx.android.synthetic.main.item_im_approve_message.view.*
import org.jetbrains.anko.backgroundResource

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
        view.approveItem.setPadding(0,0,0,0)
        val params = view.approveType.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = Utils.dpToPx(context,15)
        view.approveType.layoutParams = params
    }

    override fun bindContentView() {
        val attachment = message.attachment as ApproveAttachment
        data = attachment.messageBean

        view.approveType.text = data.type_name
        view.approveNum.text = data.title
        view.sponsor.text = replaceText("发起人：", data.username)
        view.reason.text = replaceText("审批事由：", data.reasons)
        view.schedule.text = data.status_str
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
    }

}