/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: TeamGroupModel
 * Author: admin
 * Date: 2018/11/21 下午5:07
 * Description: 群组,项目讨论组viewModel
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.module.main.viewModel;

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.amap.api.mapcore.util.it
import com.google.gson.Gson
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.TeamBean
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * @ClassName: TeamGroupModel
 * @Description: 群组,项目讨论组viewModel
 * @Author: admin
 * @Date: 2018/11/21 下午5:07
 */
class TeamGroupModel : ViewModel() {
    var teamGroup: MutableLiveData<List<Team>> = MutableLiveData()

    var discussionGroup: MutableLiveData<List<Team>> = MutableLiveData()

    fun requestTeamGroupData() {
        NIMClient.getService(TeamService::class.java).queryTeamList()
                .setCallback(object : RequestCallbackWrapper<List<Team>>() {
                    override fun onResult(code: Int, result: List<Team>?, exception: Throwable?) {
                        result?.let {
                            val ql = ArrayList<Team>()
                            val tlz = ArrayList<Team>()
                            it.forEach { team ->
                                if (team.isMyTeam) {
                                    val bean = Gson().fromJson(team.extension, TeamBean::class.java)
                                    if (bean != null) {
                                        tlz.add(team)
                                    } else {
                                        ql.add(team)
                                    }
                                }
                            }
                            teamGroup.value = ql
                            discussionGroup.value = tlz
                        }
                    }
                })
    }
}