package com.sogukj.pe.module.im

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.TeamMsgAckInfo

/**
 * Created by admin on 2018/6/13.
 */
class AckMsgViewModel : ViewModel() {
    private var teamMsgAckInfo: LiveData<TeamMsgAckInfo>? = null
    private var ackModelRepository: AckModelRepository? = null

    fun init(message: IMMessage) {
        if (this.teamMsgAckInfo != null) {
            return
        }
        ackModelRepository = AckModelRepository()
        teamMsgAckInfo = ackModelRepository!!.getMsgAckInfo(message)
    }

    fun getTeamMsgAckInfo(): LiveData<TeamMsgAckInfo>? {
        return teamMsgAckInfo
    }
}