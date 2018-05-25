package com.sogukj.pe.module.im

import android.content.Context
import android.view.View
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.session.SessionCustomization
import com.netease.nim.uikit.api.model.session.SessionEventListener
import com.netease.nim.uikit.api.wrapper.NimMessageRevokeObserver
import com.netease.nim.uikit.business.session.actions.BaseAction
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderTip
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.sogukj.pe.R

/**
 * Created by admin on 2018/1/25.
 */
object SessionHelper {
    private var p2pCustomization: SessionCustomization? = null
    private var teamCustomization: SessionCustomization? = null

    fun init() {
        //注册消息转发过滤器
        registerMsgForwardFilter()
        // 注册消息撤回过滤器
        registerMsgRevokeFilter()
        // 注册消息撤回监听器
        registerMsgRevokeObserver()
        setSessionListener()
        NimUIKit.setCommonP2PSessionCustomization(getP2pCustomization())
        NimUIKit.setCommonTeamSessionCustomization(getTeamCustomization())
        NimUIKit.registerTipMsgViewHolder(MsgViewHolderTip::class.java)
    }

    private fun setSessionListener() {
        val listener = object : SessionEventListener {
            override fun onAvatarClicked(context: Context?, message: IMMessage?) {
                //头像点击事件
                message?.let {
                    if (it.msgType == MsgTypeEnum.robot && it.direct == MsgDirectionEnum.In) {
                        return
                    }
                    context?.let {
                        val accounts = java.util.ArrayList<String>(1)
                        accounts.add(message.fromAccount)
                        NimUIKit.getUserInfoProvider().getUserInfoAsync(message.fromAccount) { success, result, code ->
                            val info = result as NimUserInfo
                            val uid = info.extensionMap["uid"].toString().toInt()
                            PersonalInfoActivity.start(context, uid)
                        }
                    }
                }
            }

            override fun onAvatarLongClicked(context: Context?, message: IMMessage?) {
                //头像长按事件
            }
        }
        NimUIKit.setSessionListener(listener)
    }

    private fun getP2pCustomization(): SessionCustomization? {
        if (p2pCustomization == null) {
            p2pCustomization = object : SessionCustomization() {

            }
            val actions = ArrayList<BaseAction>()
            actions.add(FileAction(com.netease.nim.uikit.R.drawable.im_file,R.string.select_file))
            p2pCustomization?.actions = actions
            p2pCustomization?.withSticker = true
            val buttons = ArrayList<SessionCustomization.OptionsButton>()
            val infoBtn = object : SessionCustomization.OptionsButton() {
                override fun onClick(context: Context?, view: View?, sessionId: String?) {
                    NimUIKit.getUserInfoProvider().getUserInfoAsync(sessionId) { success, result, code ->
                        val info = result as NimUserInfo
                        val uid = info.extensionMap["uid"].toString().toInt()
                        PersonalInfoActivity.start(context, uid)
                    }
                }
            }
            infoBtn.iconId = R.drawable.im_personal_info
            buttons.add(infoBtn)
            p2pCustomization?.buttons = buttons
            p2pCustomization?.withSticker = true
        }
        return p2pCustomization
    }

    private fun getTeamCustomization(): SessionCustomization? {
        if (teamCustomization == null) {
            teamCustomization = object : SessionCustomization() {

            }
            val actions = ArrayList<BaseAction>()
            actions.add(FileAction(com.netease.nim.uikit.R.drawable.im_file,R.string.select_file))
            teamCustomization?.actions = actions
            teamCustomization?.withSticker = true
            val buttons = ArrayList<SessionCustomization.OptionsButton>()
            val infoBtn = object : SessionCustomization.OptionsButton() {
                override fun onClick(context: Context?, view: View?, sessionId: String?) {
                    TeamInfoActivity.start(context!!, sessionId!!)
                }
            }
            infoBtn.iconId = R.drawable.im_team_info
            buttons.add(infoBtn)
            teamCustomization?.buttons = buttons
            teamCustomization?.withSticker = true
        }
        return teamCustomization
    }

    private fun registerMsgForwardFilter() {
        NimUIKit.setMsgForwardFilter { return@setMsgForwardFilter false }
    }

    private fun registerMsgRevokeFilter() {
        NimUIKit.setMsgRevokeFilter { return@setMsgRevokeFilter false }
    }

    private fun registerMsgRevokeObserver() {
        NIMClient.getService(MsgServiceObserve::class.java).observeRevokeMessage(NimMessageRevokeObserver(), true)
    }
}