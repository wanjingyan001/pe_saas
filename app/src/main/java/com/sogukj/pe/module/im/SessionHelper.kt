package com.sogukj.pe.module.im

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.session.SessionCustomization
import com.netease.nim.uikit.api.model.session.SessionEventListener
import com.netease.nim.uikit.api.wrapper.NimMessageRevokeObserver
import com.netease.nim.uikit.business.session.actions.BaseAction
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderTip
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.sogukj.pe.R
import com.sogukj.pe.module.im.clouddish.CloudFileAction
import com.sogukj.pe.peUtils.Store

/**
 * Created by admin on 2018/1/25.
 */
object SessionHelper {
    private var p2pCustomization: SessionCustomization? = null
    private var teamCustomization: SessionCustomization? = null

    fun init() {
        // 注册自定义消息附件解析器
        NIMClient.getService(MsgService::class.java).registerCustomAttachmentParser(CustomAttachParser())
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
        NimUIKit.registerMsgItemViewHolder(ApproveAttachment::class.java, MsgViewHolderApprove::class.java)
    }

    private fun setSessionListener() {
        val listener = object : SessionEventListener {
            override fun onAckMsgClicked(context: Context, message: IMMessage) {
                // 已读回执事件处理，用于群组的已读回执事件的响应，弹出消息已读详情
                AckMsgInfoActivity.start(context, message)
            }

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
            actions.add(FileAction(com.netease.nim.uikit.R.drawable.im_file, R.string.select_file))
            actions.add(CloudFileAction(com.netease.nim.uikit.R.drawable.im_cloud, R.string.cloud_file))
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
            val phoneBut = object : SessionCustomization.OptionsButton() {
                @SuppressLint("MissingPermission")
                override fun onClick(context: Context, view: View, sessionId: String) {
                    NimUIKit.getUserInfoProvider().getUserInfoAsync(sessionId) { success, result, code ->
                        val info = result as NimUserInfo
                        if (info.mobile.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${info.mobile}"))
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "找不到正确的手机号", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            phoneBut.iconId = R.mipmap.im_call_phone
            buttons.add(phoneBut)
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
            actions.add(FileAction(com.netease.nim.uikit.R.drawable.im_file, R.string.select_file))
            actions.add(CloudFileAction(com.netease.nim.uikit.R.drawable.im_cloud, R.string.cloud_file))
            teamCustomization?.actions = actions
            teamCustomization?.withSticker = true
            val buttons = ArrayList<SessionCustomization.OptionsButton>()
            val infoBtn = object : SessionCustomization.OptionsButton() {
                override fun onClick(context: Context, view: View?, sessionId: String?) {
                    if (!Store.store.getUser(context)?.accid.isNullOrEmpty()) {
                        TeamInfoActivity.start(context, sessionId!!)
                    }
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