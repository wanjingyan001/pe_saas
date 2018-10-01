package com.sogukj.pe.module.other

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import com.amap.api.mapcore.util.it
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMSDK
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.module.approve.ApproveListActivity
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.service.Payload
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_msg_assistant.*
import kotlinx.android.synthetic.main.item_msg_assistant.view.*
import kotlinx.android.synthetic.main.item_msg_content.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.singleLine

class MsgAssistantActivity : BaseRefreshActivity() {
    private lateinit var listAdapter: RecyclerAdapter<MessageBean>
    private lateinit var observable: Observable<Payload<ArrayList<MessageBean>>>
    private val type: Int by ExtrasDelegate(Extras.TYPE, 0)
    private var page = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_msg_assistant)
        setBack(true)
        title = when (type) {
            1 -> "审批消息助手"
            2 -> "系统消息助手"
            else -> throw ClassCastException("类型错误")
        }
        listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            when (type) {
                1 -> MsgAssHolder(_adapter.getView(R.layout.item_msg_assistant, parent))
                else -> SysMsgHolder(_adapter.getView(R.layout.item_msg_content, parent))
            }
        }
        listAdapter.onItemClick = { v, position ->
            val data = listAdapter.dataList[position]
            if (type == 1) {
                val isMine = if (data.status == -1 || data.status == 4) 1 else 2
                when {
                    data.type == 2 -> SealApproveActivity.start(this, data, isMine)
                    data.type == 3 -> SignApproveActivity.start(this, data, isMine)
                    data.type == 1 -> LeaveBusinessApproveActivity.start(this, data, isMine)//出差  SealApproveActivity
                }
            } else if (type == 2) {
                if (data.type == 1) {
                    // 内部公告
                    GongGaoDetailActivity.start(context, data)
                } else {
                    // 审批统计信息
                    ApproveListActivity.start(this, 6, data.news_id, data)
                }
            }
        }
        messageList?.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = listAdapter
        }

    }

    override fun onResume() {
        super.onResume()
        doRequest()
        clearUnReadCount()
    }

    override fun doRefresh() {
        page = 1
        doRequest()
    }

    override fun doLoadMore() {
        page += 1
        doRequest()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        return config
    }

    override fun initRefreshFooter(): RefreshFooter? = null


    private fun doRequest() {
        when (type) {
            1 -> {
                observable = SoguApi.getService(application, OtherService::class.java)
                        .msgList(page = page)
            }
            2 -> {
                observable = SoguApi.getService(application, OtherService::class.java)
                        .sysMsgList(page = page)
            }
        }
        observable.execute {
            onNext { payload ->
                payload.isOk.yes {
                    payload.payload?.let {
                        if (page == 1) {
                            listAdapter.dataList.clear()
                        }
                        listAdapter.dataList.addAll(it)
                        listAdapter.notifyDataSetChanged()
                    }
                }.otherWise {
                    showErrorToast(payload.message)
                }
            }
            onError {
                showErrorToast("暂无可用数据")
            }
            onComplete {
                SupportEmptyView.checkEmpty(this@MsgAssistantActivity, listAdapter)
                isLoadMoreEnable = listAdapter.dataList.size % 20 == 0
                if (page == 1)
                    finishRefresh()
                else
                    finishLoadMore()
            }
        }
    }

    private fun clearUnReadCount() {
        val account = when (type) {
            1 -> "58d0c67d134fbc6c"
            2 -> "50a0500b1773be39"
            else -> ""
        }
        NIMSDK.getMsgService().clearUnreadCount(account, SessionTypeEnum.P2P)
    }

    inner class MsgAssHolder(itemView: View) : RecyclerHolder<MessageBean>(itemView) {
        override fun setData(view: View, data: MessageBean, position: Int) {
            view.msgTime.text = data.time
            view.approveType.text = data.type_name
            view.approveNum.text = data.title
            view.sponsor.text = replaceText("发起人：", data.username)
            view.reason.text = replaceText("审批事由：", data.reasons)
            view.schedule.text = data.preapproval.isNullOrEmpty().yes { data.status_str }.otherWise { data.preapproval }
        }

        private fun replaceText(hintStr: String, str: String?): SpannableString {
            val spannable = SpannableString(hintStr + str)
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#A0A4AA"))
                    , 0, hintStr.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return spannable
        }
    }

    inner class SysMsgHolder(itemView: View) : RecyclerHolder<MessageBean>(itemView) {
        override fun setData(view: View, data: MessageBean, position: Int) {
            view.tv_time.text = data.time
            view.tv_urgent.setVisible(false)
            view.point.setVisible(data.has_read == 0)
            view.tv_title.text = when (data.type) {
                1 -> "内部公告:${data.title}"
                else -> "${data.title}"
            }
            view.tv_num.setVisible(false)
            view.tv_state.setVisible(false)
            view.tv_from.text = "发起人:" + data.username
            view.tv_type.setVisible(false)
            view.tv_msg.text = data.contents
            view.tv_msg.singleLine = true
        }
    }
}
