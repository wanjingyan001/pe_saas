package com.sogukj.pe.module.im

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.netease.nimlib.sdk.NIMSDK
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.TeamMsgAckInfo

/**
 * Created by admin on 2018/6/13.
 */
class AckModelRepository {

    fun getMsgAckInfo(imMsg:IMMessage):LiveData<TeamMsgAckInfo>{
        val list = MutableLiveData<TeamMsgAckInfo>()
        NIMSDK.getTeamService().fetchTeamMessageReceiptDetail(imMsg).setCallback(object :RequestCallback<TeamMsgAckInfo>{
            override fun onFailed(code: Int) {
            }

            override fun onSuccess(param: TeamMsgAckInfo) {
                list.value = param
            }

            override fun onException(exception: Throwable?) {
            }

        })
        return list
    }
}