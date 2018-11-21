package com.sogukj.pe.module.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.extraDelegate
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.main.viewModel.TeamGroupModel
import com.sogukj.pe.peUtils.MyGlideUrl
import kotlinx.android.synthetic.main.activity_imteam_group.*
import kotlinx.android.synthetic.main.item_team_organization_chlid_2.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.imageResource
import java.lang.IllegalArgumentException

class IMTeamGroupActivity : BaseRefreshActivity() {
    private val titleStr by extraDelegate(Extras.TITLE, "")
    private val listType by extraDelegate(Extras.TYPE, -1)
    private lateinit var listAdapter: RecyclerAdapter<Team>
    private val model: TeamGroupModel by lazy { ViewModelProviders.of(this).get(TeamGroupModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imteam_group)
        title = titleStr
        setBack(true)

        listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            TeamHolder(_adapter.getView(R.layout.item_team_organization_chlid_2, parent))
        }
        listAdapter.onItemClick = { v, p ->
            val team = listAdapter.dataList[p]
            NimUIKit.startTeamSession(context, team.id)
        }
        teamList.apply {
            layoutManager = LinearLayoutManager(this@IMTeamGroupActivity)
            adapter = listAdapter
            addItemDecoration(DividerItemDecoration(this@IMTeamGroupActivity, DividerItemDecoration.VERTICAL))
        }

        model.requestTeamGroupData()
        when (listType) {
            1 -> {
                model.teamGroup
            }
            2 -> {
                model.discussionGroup
            }
            else -> throw  IllegalArgumentException("类型错误")
        }.observe(this, Observer {
            it?.let {
                listAdapter.refreshData(it)
            }
        })
    }


    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.refreshEnable = false
        config.autoLoadMoreEnable = false
        return config
    }

    override fun doRefresh() {
    }

    override fun doLoadMore() {
    }


    inner class TeamHolder(itemView: View) : RecyclerHolder<Team>(itemView) {
        override fun setData(view: View, data: Team, position: Int) {
            view.userName.text = data.name
            view.selectIcon.visibility = View.GONE
            if (data.icon != null) {
                Glide.with(this@IMTeamGroupActivity)
                        .load(MyGlideUrl(data.icon))
                        .apply(RequestOptions().error(R.drawable.im_team_default).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(view.userHeadImg)
            } else {
                view.userHeadImg.imageResource = R.drawable.im_team_default
            }
        }
    }

}
