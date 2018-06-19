package com.sogukj.pe.module.im


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.TeamMsgAckInfo
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.widgets.CircleImageView
import kotlinx.android.synthetic.main.fragment_read_ack_msg_tab.*
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.ctx


/**
 * A simple [Fragment] subclass.
 */
class ReadAckMsgTabFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_read_ack_msg_tab
    private lateinit var mAdapter: RecyclerAdapter<NimUserInfo>
    private lateinit var model: AckMsgViewModel
    private lateinit var message: IMMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getSerializable(ARG_PARAM) as IMMessage
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProviders.of(this).get(AckMsgViewModel::class.java)
        model.init(message)
        val liveData = model.getTeamMsgAckInfo()
        info { "已读:${liveData.jsonStr}" }
        liveData?.observe(baseActivity!!, Observer<TeamMsgAckInfo> { t ->
            NimUIKit.getUserInfoProvider().getUserInfoAsync(t?.ackAccountList) { success, result, code ->
                val newData = result as List<NimUserInfo>
                mAdapter.refreshData(newData)
                mAdapter.dataList.clear()
                mAdapter.dataList.addAll(newData)
            }
        })
        mAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            AckMsgHolder(_adapter.getView(R.layout.item_immsg_read, parent))
        }
        readAckList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = mAdapter
        }

    }


    companion object {
        private val ARG_PARAM = "param"
        fun newInstance(imMsg: IMMessage): ReadAckMsgTabFragment {
            val fragment = ReadAckMsgTabFragment()
            val args = Bundle()
            args.putSerializable(ARG_PARAM, imMsg)
            fragment.arguments = args
            return fragment
        }
    }

    inner class AckMsgHolder(itemView: View) : RecyclerHolder<NimUserInfo>(itemView) {
        val avatarImg = itemView.find<CircleImageView>(R.id.avatarImg)
        val userName = itemView.find<TextView>(R.id.userName)
        override fun setData(view: View, data: NimUserInfo, position: Int) {
            Glide.with(ctx).load(data.avatar)
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            avatarImg.setChar(data.name.first())
                            return false
                        }

                    }).into(avatarImg)
            userName.text = data.name
        }
    }

}
