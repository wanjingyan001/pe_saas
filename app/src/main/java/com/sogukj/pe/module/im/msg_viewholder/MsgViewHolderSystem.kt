package com.sogukj.pe.module.im.msg_viewholder

import android.content.Intent
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.netease.nim.uikit.common.util.sys.ScreenUtil
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.SystemPushBean
import com.sogukj.pe.module.calendar.ModifyTaskActivity
import com.sogukj.pe.module.calendar.TaskDetailActivity
import com.sogukj.pe.module.weekly.PersonalWeeklyActivity
import kotlinx.android.synthetic.main.item_im_approve_message.view.*
import org.jetbrains.anko.backgroundResource

/**
 * 系统消息助手
 * Created by admin on 2018/11/2.
 */
class MsgViewHolderSystem(adapter: BaseMultiItemFetchLoadAdapter<*, *>) : MsgViewHolderBase(adapter)  {
    private lateinit var data: SystemPushBean
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
        val attachment = message.attachment as SystemAttachment
        data = attachment.systemBean
        view.approveType.text = data.title
        view.sponsor.text = replaceText("发起人：", data.name)
        view.schedule.setVisible(false)

    }
    override fun onItemLongClick(): Boolean {
        return true
    }

    private fun replaceText(hintStr: String, str: String?): SpannableString {
        val spannable = SpannableString(hintStr + str)
        spannable.setSpan(ForegroundColorSpan(Color.parseColor("#A0A4AA"))
                , 0, hintStr.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    override fun onItemClick() {
        super.onItemClick()
//        202 日历
//        203 任务
//        205 周报
        when(data.type){
            202->{
                TaskDetailActivity.start(context, data.id, data.title, ModifyTaskActivity.Task)
            }
            203->{
                TaskDetailActivity.start(context, data.id, data.title, ModifyTaskActivity.Schedule)
            }
            205->{
                val weekId = data.week_id
                val userId = data.sub_uid
                val postName = data.title
                val intent = Intent(context, PersonalWeeklyActivity::class.java)
                intent.putExtra(Extras.ID, weekId)
                intent.putExtra(Extras.NAME, "Push")
                intent.putExtra(Extras.TYPE1, userId)
                intent.putExtra(Extras.TYPE2, postName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
}