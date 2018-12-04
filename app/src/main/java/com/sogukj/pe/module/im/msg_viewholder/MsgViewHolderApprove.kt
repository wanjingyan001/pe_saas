package com.sogukj.pe.module.im.msg_viewholder

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.LinearLayout
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.netease.nim.uikit.common.util.sys.ScreenUtil
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.NewApprovePush
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.approve.ApproveDetailActivity
import com.sogukj.pe.module.project.originpro.OtherProjectShowActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity
import com.sogukj.pe.module.project.originpro.ProjectUploadShowActivity
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.item_im_approve_message.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.startActivity

/**
 * Created by admin on 2018/9/21.
 */
class MsgViewHolderApprove(adapter: BaseMultiItemFetchLoadAdapter<*, *>) : MsgViewHolderBase(adapter) {
    private lateinit var data: NewApprovePush
    override fun getContentResId(): Int = R.layout.item_im_approve_message
    private val mine by lazy { Store.store.getUser(context) }

    override fun inflateContentView() {
        val width = (0.6 * ScreenUtil.screenWidth).toInt()
        setLayoutParams(width, ConstraintLayout.LayoutParams.WRAP_CONTENT, view.approveItem)
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
        view.schedule.text = data.statusStr
        view.expedited.setVisible(data.type == 301)
//        data.preapproval.isNullOrEmpty().yes {
//            view.schedule.textColor = R.color.text_3
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
        val isMine = if (mine!!.uid == data.subId) 1 else 0
        context.startActivity<ApproveDetailActivity>(Extras.ID to data.id,
                Extras.FLAG to isMine)
    }

}