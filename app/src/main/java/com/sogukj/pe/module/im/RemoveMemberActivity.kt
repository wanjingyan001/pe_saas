package com.sogukj.pe.module.im

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil.setContentView
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.R.layout.item
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.activity_remove_member.*
import kotlinx.android.synthetic.main.item_team_organization_chlid.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.imageResource

class RemoveMemberActivity : ToolbarActivity() {
    private lateinit var memberAdapter: RecyclerAdapter<UserBean>
    private lateinit var teamMembers: ArrayList<UserBean>
    private lateinit var alreadySelected: ArrayList<UserBean>
    private var team: Team? = null
    private val mine by lazy { Store.store.getUser(this) }
    override val menuId: Int
        get() = R.menu.arrange_edit_save

    companion object {
        fun start(context: Activity, teamMembers: ArrayList<UserBean>, team: Team? = null) {
            val intent = Intent(context, RemoveMemberActivity::class.java)
            intent.putExtra(Extras.LIST, teamMembers)
            intent.putExtra(Extras.DATA, team)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_member)
        setBack(true)
        title = "移除群成员"
        teamMembers = intent.getSerializableExtra(Extras.LIST) as ArrayList<UserBean>
        alreadySelected = intent.getSerializableExtra(Extras.LIST) as ArrayList<UserBean>
        team = intent.getSerializableExtra(Extras.DATA) as Team
        memberAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            val itemView = _adapter.getView(R.layout.item_team_organization_chlid, parent)
            object : RecyclerHolder<UserBean>(itemView) {
                override fun setData(view: View, data: UserBean, position: Int) {
                    view.userName.text = data.name
                    view.userPosition.text = data.position
                    view.selectIcon.setVisible(true)
                    if (team?.creator == data.accid || mine?.uid == data.uid) {
                        view.selectIcon.isSelected = false
                        view.selectIcon.imageResource = R.drawable.cannot_select
                    } else {
                        view.selectIcon.isSelected = alreadySelected.find { it.uid == data.uid } != null
                    }
                    Glide.with(ctx)
                            .load(MyGlideUrl(data.url))
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    view.userHeadImg.setChar(data.name.first())
                                    return false
                                }

                            })
                            .into(view.userHeadImg)
                }
            }
        }
        memberAdapter.onItemClick = { v, position ->
            val bean = memberAdapter.dataList[position]
            if (alreadySelected.contains(bean)) {
                alreadySelected.remove(bean)
            } else {
                alreadySelected.add(bean)
            }
            memberAdapter.notifyItemChanged(position)
        }
        memberAdapter.dataList.addAll(teamMembers)
        removeList.apply {
            layoutManager = LinearLayoutManager(this@RemoveMemberActivity)
            adapter = memberAdapter
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_edit -> {
                val intent = Intent()
                intent.putExtra(Extras.LIST2, alreadySelected)
                setResult(Extras.RESULTCODE2, intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
