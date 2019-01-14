package com.sogukj.pe.module.im.clouddish

import android.content.Intent
import com.netease.nim.uikit.business.session.actions.BaseAction
import com.netease.nim.uikit.business.session.constant.RequestCode
import com.netease.nim.uikit.common.util.file.FileUtil
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sogukj.pe.Extras
import java.io.File

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudFileAction : BaseAction {

    constructor(iconResId : Int, titleId : Int):super(iconResId,titleId)

    override fun onClick() {
        CloudDishActivity.invokeForResult(activity,1,makeRequestCode(RequestCode.GET_LOCAL_FILE),"",false,"","")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when(requestCode){
                RequestCode.GET_LOCAL_FILE -> {
                    if (null != data){
                        val paths = data.getStringArrayListExtra(Extras.DATA)
                        paths.forEach {
                            val file = File(it)
                            var message : IMMessage
                            //图片类型
                            if (FileUtil.getFileType(it) != null) {
                                //图片类型
                                if (container != null && container.sessionType == SessionTypeEnum.ChatRoom) {
                                    message = ChatRoomMessageBuilder.createChatRoomImageMessage(account, file, file.name)
                                }else{
                                    message = MessageBuilder.createImageMessage(account, sessionType, file, file.name)
                                }
                            } else {
                                //文件类型
                                message = MessageBuilder.createFileMessage(account,
                                        sessionType, file, file.name)
                            }
                            sendMessage(message)
                        }
                    }
                }
        }
    }

    override fun sendCloudFile(paths: MutableList<String>?) {
        paths!!.forEach {
            val file = File(it)
            var message : IMMessage
            //图片类型
            if (FileUtil.getFileType(it) != null) {
                //图片类型
                if (container != null && container.sessionType == SessionTypeEnum.ChatRoom) {
                    message = ChatRoomMessageBuilder.createChatRoomImageMessage(account, file, file.name)
                }else{
                    message = MessageBuilder.createImageMessage(account, sessionType, file, file.name)
                }
            } else {
                //文件类型
                message = MessageBuilder.createFileMessage(account,
                        sessionType, file, file.name)
            }
            sendMessage(message)
        }
    }

    companion object {
        val REQ_SELETE_FILE = 1002
    }

}