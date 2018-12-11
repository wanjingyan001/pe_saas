/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: MainMsgPageAdapter
 * Author: admin
 * Date: 2018/12/6 上午10:44
 * Description: 首页顶部卡片adapter
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.module.main.adapter

import android.content.Context
import android.text.Html
import android.view.View
import android.view.ViewGroup
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.module.approve.ApproveDetailActivity
import com.sogukj.pe.module.approve.NewApproveListActivity
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveListBean
import com.sogukj.pe.module.news.MainNewsActivity
import com.sogukj.pe.module.news.NewsDetailActivity
import kotlinx.android.synthetic.main.item_msg_content_main.view.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity

/**
 * @ClassName: MainMsgPageAdapter
 * @Description: 首页顶部卡片adapter
 * @Author: admin
 * @Date: 2018/12/6 上午10:44
 */
class MainMsgPageAdapter(context: Context, creator: (RecyclerAdapter<Any>, ViewGroup, Int) -> MainMsgHolder
= { _adapter, parent, _ -> MainMsgHolder(_adapter.getView(R.layout.item_msg_content_main, parent)) })
    : RecyclerAdapter<Any>(context, creator)


class MainMsgHolder(item: View) : RecyclerHolder<Any>(item) {
    override fun setData(view: View, data: Any, position: Int) {
        view.tv_num.setVisible(false)
        view.tv_urgent.setVisible(false)
        when (data) {
            is ApproveListBean -> {
                //审批数据
                view.tv_title.text = data.temName
                view.sequense.text = data.number
                view.tv_from.text = "发起人:${data.name}"
                view.tv_type.text = "类型:${data.temName}"
                view.tv_msg.text = ""
                view.tv_state.text = data.status_str ?: when (data.status) {
                    -1 -> "审批不通过"
                    0 -> "待审批"
                    1 -> "审批中"
                    2 -> "审批通过"
                    3 -> "待用印"
                    4 -> "审批完成"
                    5 -> "撤销成功"
                    else -> ""
                }
                view.more.clickWithTrigger {
                    view.context.startActivity<NewApproveListActivity>()
                }
                view.ll_content.clickWithTrigger {
                    val isMine = if (data.status == -1 || data.status == 4) 1 else 0
                    view.context.startActivity<ApproveDetailActivity>(Extras.ID to data.approval_id,
                            Extras.FLAG to isMine)
                }
            }
            is NewsBean -> {
                //舆情数据
                view.tv_title.text = "舆情"
                view.sequense.setVisible(!data.company.isNullOrEmpty())
                view.sequense.text = data.company
                view.tv_from.setVisible(!data.source.isNullOrEmpty())
                view.tv_from.text = data.source
                view.tv_type.text = data.time
                view.tv_msg.text = Html.fromHtml(data.title)
                view.tv_state.text = when (data.table_id) {
                    1 -> "法律诉讼"
                    2 -> "法院公告"
                    3 -> "失信人"
                    4 -> "被执行人"
                    5 -> "行政处罚"
                    6 -> "严重违法"
                    7 -> "股权出质"
                    8 -> "动产抵押"
                    9 -> "欠税公告"
                    10 -> "经营异常"
                    11 -> "开庭公告"
                    12 -> "司法拍卖"
                    13 -> "新闻舆情"
                    else -> ""
                }
                view.tv_state.setVisible(view.tv_state.text.isNotEmpty())

                view.more.clickWithTrigger {
                    view.context.startActivity<MainNewsActivity>()
                }
                view.ll_content.clickWithTrigger {
                    view.context.startActivity<NewsDetailActivity>(Extras.DATA to data)
                }
            }
        }

    }
}