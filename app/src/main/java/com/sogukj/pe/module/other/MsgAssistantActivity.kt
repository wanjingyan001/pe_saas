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
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MessageBean
import kotlinx.android.synthetic.main.activity_msg_assistant.*
import kotlinx.android.synthetic.main.item_msg_assistant.view.*
import org.jetbrains.anko.ctx

class MsgAssistantActivity : BaseRefreshActivity() {
    private lateinit var listAdapter: RecyclerAdapter<MessageBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_msg_assistant)
        setBack(true)
        listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            MsgAssHolder(_adapter.getView(R.layout.item_msg_assistant, parent))
        }
        listAdapter.onItemClick = { v, position ->

        }
        messageList?.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = listAdapter
        }
    }

    override fun doRefresh() {

    }

    override fun doLoadMore() {

    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        return config
    }

    override fun initRefreshFooter(): RefreshFooter? = null





    inner class MsgAssHolder(itemView: View) : RecyclerHolder<MessageBean>(itemView) {
        override fun setData(view: View, data: MessageBean, position: Int) {
            view.msgTime.text = data.time
            view.approveType.text = data.type_name
            view.approveNum.text = data.approval_id.toString()
            view.sponsor.text = replaceText("发起人：", data.username)
            view.reason.text = replaceText("审批事由：", data.contents)
            view.schedule.text = data.status_str
        }

        private fun replaceText(hintStr: String, str: String?): String {
            val spannable = SpannableString(hintStr + str)
            spannable.setSpan(ForegroundColorSpan(Color.parseColor("#A0A4AA"))
                    , 0, hintStr.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return spannable.toString()
        }
    }
}
