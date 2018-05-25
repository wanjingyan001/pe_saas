package com.sogukj.pe.module.im

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nim.uikit.business.session.module.list.MessageListPanelEx
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_display_message.*

class DisplayMessageActivity : UI(), ModuleProxy {


    lateinit var toolbar: Toolbar
    lateinit var sessionType: SessionTypeEnum
    lateinit var account: String
    lateinit var anchor: IMMessage
    lateinit var messageListPanel: MessageListPanelEx

    companion object {
        fun start(context: Context, anchor: IMMessage) {
            val intent = Intent(context, DisplayMessageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("anchor", anchor)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflate = layoutInflater.inflate(R.layout.activity_display_message, null)
        setContentView(inflate)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        toolbar.setNavigationOnClickListener { finish() }
        anchor = intent.getSerializableExtra("anchor") as IMMessage
        account = anchor.sessionId
        sessionType = anchor.sessionType
        team_tool.text = UserInfoHelper.getUserTitleName(account, sessionType)
        val container = Container(this, account, sessionType, this)
        messageListPanel = MessageListPanelEx(container, inflate, anchor, true, false)

    }

    override fun sendMessage(msg: IMMessage): Boolean {
        return false
    }

    override fun onInputPanelExpand() {

    }

    override fun onItemFooterClick(message: IMMessage) {

    }

    override fun shouldCollapseInputPanel() {

    }

    override fun isLongClickEnabled(): Boolean {
        return true
    }
}
