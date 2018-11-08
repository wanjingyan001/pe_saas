package com.sogukj.pe.module.other

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.netease.nim.uikit.common.util.sys.TimeUtil
import com.netease.nimlib.sdk.NIMSDK
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.approve.*
import com.sogukj.pe.module.calendar.ModifyTaskActivity
import com.sogukj.pe.module.calendar.TaskDetailActivity
import com.sogukj.pe.module.im.msg_viewholder.ApproveAttachment
import com.sogukj.pe.module.im.msg_viewholder.CustomAttachment
import com.sogukj.pe.module.im.msg_viewholder.ProcessAttachment
import com.sogukj.pe.module.im.msg_viewholder.SystemAttachment
import com.sogukj.pe.module.project.originpro.OtherProjectShowActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity
import com.sogukj.pe.module.project.originpro.ProjectUploadShowActivity
import com.sogukj.pe.module.weekly.PersonalWeeklyActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.service.Payload
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_msg_assistant.*
import kotlinx.android.synthetic.main.item_msg_assistant.view.*
import kotlinx.android.synthetic.main.item_msg_content.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.startActivity

class MsgAssistantActivity : BaseRefreshActivity() {
    private lateinit var msgAdapter: MsgTypeAdapter
    private val immsgList = mutableListOf<IMMessage>()
    private val mine by lazy { Store.store.getUser(context) }
    private val account: String by extraDelegate(Extras.ID, "")
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

        msgAdapter = MsgTypeAdapter(mutableListOf())
        msgAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val data = adapter.data[position]
            when (data) {
                is NewApprovePush -> {
                    val isMine = if (mine!!.uid == data.subId) 1 else 0
                    context.startActivity<ApproveDetailActivity>(Extras.ID to data.id,
                            Extras.FLAG to isMine)
                }
                is SystemPushBean -> {
                    when (data.type) {
                        202 -> {
                            TaskDetailActivity.start(context, data.id, data.title, ModifyTaskActivity.Task)
                        }
                        203 -> {
                            TaskDetailActivity.start(context, data.id, data.title, ModifyTaskActivity.Schedule)
                        }
                        205 -> {
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
                is NewProjectProcess -> {
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
        messageList?.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = msgAdapter
        }

    }

    override fun onResume() {
        super.onResume()
        getDataByIM()
        clearUnReadCount()
    }

    override fun doRefresh() {
        page = 1
        getDataByIM()
    }

    override fun doLoadMore() {
        page += 1
        getDataByIM()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        return config
    }

    override fun initRefreshFooter(): RefreshFooter? = null


    private fun getDataByIM() {
        NIMSDK.getMsgService().pullMessageHistory(anchor(), 20, false)
                .setCallback(object : RequestCallbackWrapper<List<IMMessage>>() {
                    override fun onResult(code: Int, result: List<IMMessage>?, exception: Throwable?) {
                        (page == 1).yes {
                            finishRefresh()
                        }.otherWise {
                            finishLoadMore()
                        }
                        if (exception != null || code != 200) {
                            return
                        }
                        result?.let {
                            (it.isNotEmpty() && page == 1).yes {
                                msgAdapter.data.clear()
                            }
                            it.filter { it.attachment != null }.forEach {
                                info { it.jsonStr }
                                immsgList.add(it)
                                val attachment = it.attachment as CustomAttachment
                                when (attachment) {
                                    is ApproveAttachment -> {
                                        msgAdapter.data.add(attachment.messageBean)
                                    }
                                    is SystemAttachment -> {
                                        msgAdapter.data.add(attachment.systemBean)
                                    }
                                    is ProcessAttachment -> {
                                        msgAdapter.data.add(attachment.bean)
                                    }
                                }
                            }
                            msgAdapter.notifyDataSetChanged()
                        }
                        iv_empty.setVisible(msgAdapter.data.isEmpty())
                    }
                })
    }

    private var anchor: IMMessage? = null
    private fun anchor(): IMMessage {
//        return anchor ?: MessageBuilder.createEmptyMessage(account, SessionTypeEnum.P2P, 0)
        immsgList.isEmpty().yes {
            return anchor ?: MessageBuilder.createEmptyMessage(account, SessionTypeEnum.P2P, 0)
        }.otherWise {
            val index = (page == 1).yes {
                0
            }.otherWise {
                immsgList.size - 1
            }
            return immsgList[index]
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


    inner class MsgTypeAdapter(data: MutableList<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {

        init {
            addItemType(1, R.layout.item_msg_assistant_approvel)
            addItemType(2, R.layout.item_msg_assistant_pay_history)
            addItemType(3, R.layout.item_msg_assistant_renewal_fee)
            addItemType(4, R.layout.item_msg_system_push)
            addItemType(5, R.layout.item_msg_system_push)
            addItemType(6, R.layout.item_msg_assistant_approvel)
        }

        override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
            when (helper.itemViewType) {
                1 -> {
                    item as NewApprovePush
                    helper.setText(R.id.secondTitle, "今天还有1个审批单待你处理")
                    val l = System.currentTimeMillis() - (item.pushTime * 1000)
                    if (item.title.contains(item.subName)){
                        helper.setText(R.id.approvalTip,
                                "${item.title}，已等待${TimeUtil.getElapseTimeForShow(l.toInt())}；\n在手机或电脑上快速处理审批！")
                    }else{
                        helper.setText(R.id.approvalTip,
                                "${item.subName}的${item.title}，已等待${TimeUtil.getElapseTimeForShow(l.toInt())}；\n在手机或电脑上快速处理审批！")
                    }
                    helper.setText(R.id.msgTime, Utils.formatDingDate(Utils.getTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm")))
                }
                2 -> {

                }
                3 -> {

                }
                4 -> {
                    item as SystemPushBean
                    helper.setText(R.id.pushMsgTitle, item.title)
                    helper.setText(R.id.sponsor, "发起人：${item.name}")
                    helper.setText(R.id.schedule, Utils.formatDingDate(Utils.getTime(item.pushTime.toLong(), "yyyy-MM-dd HH:mm")))
                    helper.setTextColor(R.id.schedule, resources.getColor(R.color.text_1))
                    helper.setText(R.id.msgTime, Utils.formatDingDate(Utils.getTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm")))
                }
                5 -> {
                    item as NewApprovePush
                    helper.setText(R.id.msgTime, Utils.formatDingDate(Utils.getTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm")))
                    helper.setImageResource(R.id.header, R.mipmap.icon_msg_ass_item_header)
                    helper.setText(R.id.pushMsgTitle, item.title)
                    helper.setText(R.id.sponsor, "发起人：${item.subName}")
                    helper.setText(R.id.schedule, "审批已完成")
                }
                6 -> {
                    item as NewProjectProcess
                    if (item.title.contains(item.subName)){
                        helper.setText(R.id.approvalTip,
                                "${item.title}；\n在手机或电脑上快速处理审批！")
                    }else{
                        helper.setText(R.id.approvalTip,
                                "${item.subName}的${item.title}；\n在手机或电脑上快速处理审批！")
                    }
                    helper.setText(R.id.msgTime, Utils.formatDingDate(Utils.getTime(System.currentTimeMillis(), "yyyy-MM-dd HH:mm")))
                }
            }
        }
    }
}
