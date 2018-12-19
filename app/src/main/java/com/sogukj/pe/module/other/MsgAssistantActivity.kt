package com.sogukj.pe.module.other

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
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
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.approve.ApproveDetailActivity
import com.sogukj.pe.module.calendar.ModifyTaskActivity
import com.sogukj.pe.module.calendar.TaskDetailActivity
import com.sogukj.pe.module.im.msg_viewholder.*
import com.sogukj.pe.module.project.originpro.OtherProjectShowActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity
import com.sogukj.pe.module.project.originpro.ProjectUploadShowActivity
import com.sogukj.pe.module.weekly.PersonalWeeklyActivity
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.activity_msg_assistant.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
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
                    val isMine = if (data.type == 301 || data.type == 302) {
                        0
                    } else {
                        if (mine!!.uid == data.subId) 1 else 0
                    }
                    context.startActivity<ApproveDetailActivity>(Extras.ID to data.id,
                            Extras.FLAG to isMine)
                }
                is SystemPushBean -> {
                    when (data.type) {
                        201, 202 -> {
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
                    val icFloor = data.floor + 1
                    val project = ProjectBean()
                    project.company_id = data.id
                    project.floor = data.floor
                    project.name = data.cName
                    when (icFloor) {
                        2 -> {
                            //立项
                            context.startActivity<ProjectApprovalShowActivity>(Extras.DATA to project, Extras.FLAG to icFloor)
                        }
                        3 -> {
                            //预审会
                            context.startActivity<ProjectUploadShowActivity>(Extras.DATA to project, Extras.FLAG to icFloor)
                        }
                        4 -> {
                            //投决会
                            context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to icFloor,
                                    Extras.TITLE to "投决会")
                        }
                        5 -> {
                            //签约付款
                            context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to icFloor,
                                    Extras.TITLE to "签约付款")
                        }
                        6 -> {
                            //投后管理
                            context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to icFloor,
                                    Extras.TITLE to "投后管理")
                        }
                        7 -> {
                            //退出管理
                            context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to icFloor,
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
                            it.filter { it.attachment != null }.forEachIndexed { index, imMessage ->
                                info { "第$page 页, 第$index 条==>${imMessage.jsonStr}" }
                                immsgList.add(imMessage)
                                val attachment = imMessage.attachment as CustomAttachment
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
                                    is PayPushAttachment -> {
                                        msgAdapter.data.add(attachment.payPushBean)
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
        (page == 1).yes {
            return MessageBuilder.createEmptyMessage(account, SessionTypeEnum.P2P, 0)
        }.otherWise {
            immsgList.isEmpty().yes {
                return anchor ?: MessageBuilder.createEmptyMessage(account, SessionTypeEnum.P2P, 0)
            }.otherWise {
                return immsgList[immsgList.size - 1]
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
            val imMessage = immsgList[helper.adapterPosition]
            helper.setText(R.id.msgTime, TimeUtil.getTimeShowString(imMessage.time, false))
            if (null == item) return
            when (helper.itemViewType) {
                1 -> {
                    item as NewApprovePush
                    helper.setText(R.id.secondTitle, "今天还有1个审批单待你处理")
                    val l = System.currentTimeMillis() - (item.pushTime * 1000)
                    val timeForShow = TimeUtil.getElapseTimeForShow(l.toInt())
                    if (item.title.contains(item.subName)) {
                        helper.setText(R.id.approvalTip,
                                "${item.title}${timeForShow.isEmpty().yes { "" }.otherWise { " ，已等待$timeForShow" }}；\n在手机或电脑上快速处理审批！")
                    } else {
                        helper.setText(R.id.approvalTip,
                                "${item.subName}的${item.title}${timeForShow.isEmpty().yes { "" }.otherWise { " ，已等待$timeForShow" }}；\n在手机或电脑上快速处理审批！")
                    }

                    helper.getView<TextView>(R.id.expedited).setVisible(item.type == 301)
                }
                2 -> {
                    item as PayHistory
                    when (item.type) {
                        402 -> {
                            helper.setImageResource(R.id.payHistoryBg, R.mipmap.bg_znws_pay_history)
                            helper.setImageResource(R.id.payTypeIcon, R.mipmap.icon_msg_assis_znws)
                            helper.setBackgroundColor(R.id.line1, Color.parseColor("#D1F6FF"))
                        }
                        403 -> {
                            helper.setImageResource(R.id.payHistoryBg, R.mipmap.bg_zx_pay_history)
                            helper.setImageResource(R.id.payTypeIcon, R.mipmap.icon_msg_assis_zx)
                            helper.setBackgroundColor(R.id.line1, Color.parseColor("#B8DCFF"))
                        }
                        404 -> {
                            helper.setImageResource(R.id.payHistoryBg, R.mipmap.bg_yqjk_pay_history)
                            helper.setImageResource(R.id.payTypeIcon, R.mipmap.icon_msg_assis_yqjk)
                            helper.setBackgroundColor(R.id.line1, Color.parseColor("#FFE7E7"))
                        }
                        405 -> {
                            helper.setImageResource(R.id.payHistoryBg, R.mipmap.bg_yqtc_pay_history)
                            helper.setImageResource(R.id.payTypeIcon, R.mipmap.icon_msg_assis_yqtc)
                            helper.setBackgroundColor(R.id.line1, Color.parseColor("#CBD5FF"))
                        }
                        401, 406 -> {
                            helper.setImageResource(R.id.payHistoryBg, R.mipmap.bg_qbcz_pay_history)
                            helper.setImageResource(R.id.payTypeIcon, R.mipmap.icon_msg_assis_qbcz)
                            helper.setBackgroundColor(R.id.line1, Color.parseColor("#FFE6A3"))
                        }
                    }
                    helper.setImageResource(R.id.header, R.mipmap.icon_msg_system_push)
                    helper.setText(R.id.title, item.title)
                    helper.setText(R.id.payTypeTitle, item.content)
                    helper.setText(R.id.payTypeUnitPrice, "￥${item.unit_price}")
                    helper.setText(R.id.payTypeNum, "x${item.order_count}")
                    helper.setText(R.id.payUserName, "购买人：${item.pay_userName.withOutEmpty}")
                    helper.setText(R.id.payOrderNum, "订单编号：${item.order_str}")
                    helper.setText(R.id.payTotalPrice, "￥${item.money}")
                    helper.setText(R.id.orderTime, "订单时间：${item.order_time}")
                }
                3 -> {

                }
                4 -> {
                    item as SystemPushBean
                    helper.setText(R.id.pushMsgTitle, item.title)
                    if (item.type == 202 || item.type == 203 || item.type == 201) {
                        helper.setText(R.id.sponsor, "发起人：${item.name}")
                        item.satrtTime?.let {
                            helper.setText(R.id.schedule, Utils.formatDingDate(Utils.getTime(it * 1000, "yyyy-MM-dd HH:mm")))
                            helper.setTextColor(R.id.schedule, resources.getColor(R.color.text_1))
                        }
                    } else if (item.type == 205) {
                        helper.setVisible(R.id.sponsor, false)
                    }
                }
                5 -> {
                    item as NewApprovePush
                    helper.setImageResource(R.id.header, R.mipmap.icon_msg_ass_item_header)
                    helper.setText(R.id.pushMsgTitle, item.title)
                    helper.setText(R.id.sponsor, "发起人：${item.subName}")
                    helper.setText(R.id.schedule, "审批已完成")
                    helper.getView<TextView>(R.id.expedited).setVisible(item.type == 301)
                }
                6 -> {
                    item as NewProjectProcess
                    if (null != item.title && null != item.subName){
                        if (item.title.contains(item.subName)) {
                            helper.setText(R.id.approvalTip,
                                    "${item.title}；\n在手机或电脑上快速处理审批！")
                        } else {
                            helper.setText(R.id.approvalTip,
                                    "${item.subName}的${item.title}；\n在手机或电脑上快速处理审批！")
                        }
                    }
                    helper.getView<TextView>(R.id.expedited).setVisible(item.type == 301)
                }
            }
        }
    }
}
