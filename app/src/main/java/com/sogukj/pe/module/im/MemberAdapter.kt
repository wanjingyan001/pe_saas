package com.sogukj.pe.module.im

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.R
import com.sogukj.pe.R.id.dataList
import com.sogukj.pe.baselibrary.utils.DiffCallBack
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.widgets.CircleImageView

/**
 * Created by admin on 2018/1/25.
 */
class MemberAdapter(val ctx: Context) : RecyclerView.Adapter<MemberAdapter.MemberHolder>() {
    var isMyTeam = false
    val members = mutableListOf<UserBean>()

    var onItemClick: ((v: View, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberHolder {
        val inflate = LayoutInflater.from(ctx).inflate(R.layout.item_team_member, parent, false)
        return MemberHolder(inflate)
    }

    override fun onBindViewHolder(holder: MemberHolder, position: Int) {
//        if (position == members.size) {
//            holder.headImg.setImageResource(R.drawable.invalid_name3)
//        } else if (position == members.size + 1) {
//            holder.headImg.setImageResource(R.mipmap.invalid_name4)
//        } else {
//            val userBean = members[position]
//            if (userBean.url.isNullOrEmpty()) {
//                val ch = userBean.name.first()
//                holder.headImg.setChar(ch)
//            } else {
//                Glide.with(ctx)
//                        .load(userBean.headImage())
//                        .listener(object : RequestListener<Drawable> {
//                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                                return false
//                            }
//
//                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                                holder.headImg.setChar(userBean.name.first())
//                                return false
//                            }
//
//                        })
//                        .into(holder.headImg)
//            }
//        }
        if (isMyTeam) {
            when (position) {
                members.size -> {
                    holder.headImg.setImageResource(R.drawable.invalid_name3)
                    holder.headImg.setTag(R.id.member_headImg,"ADD")
                }
                members.size + 1 -> {
                    holder.headImg.setImageResource(R.mipmap.invalid_name4)
                    holder.headImg.setTag(R.id.member_headImg,"REMOVE")
                }
                else -> setData(holder, position)
            }
        } else {
            when (position) {
                members.size -> {
                    holder.headImg.setImageResource(R.drawable.invalid_name3)
                    holder.headImg.setTag(R.id.member_headImg,"ADD")
                }
                else -> setData(holder, position)
            }
        }

        holder.headImg.setOnClickListener { v ->
            if (null != onItemClick) {
                onItemClick!!(v, position)
            }
        }
    }

    /**
     * 使用DiffUtil刷新数据
     */
    fun refreshData(newData: List<UserBean>) {
        val data = if (isMyTeam) {
            if (newData.size > 5) {
                newData.subList(0, 5)
            } else {
                newData
            }
        } else {
            if (newData.size > 6) {
                newData.subList(0, 6)
            } else {
                newData
            }
        }
        val result = DiffUtil.calculateDiff(DiffCallBack(members, data))
        result.dispatchUpdatesTo(this)
        members.clear()
        members.addAll(data)
        notifyDataSetChanged()
    }

    private fun setData(holder: MemberHolder, position: Int) {
        val userBean = members[position]
        if (userBean.url.isNullOrEmpty()) {
            val ch = userBean.name.first()
            holder.headImg.setChar(ch)
        } else {
            Glide.with(ctx)
                    .load(userBean.headImage())
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            holder.headImg.setChar(userBean.name.first())
                            return false
                        }

                    })
                    .into(holder.headImg)
        }
    }

    override fun getItemCount(): Int {
        return if (isMyTeam) {
            if (members.size > 5) {
                7
            } else {
                members.size + 2
            }
        } else {
            if (members.size > 6) {
                7
            } else {
                members.size + 1
            }
        }
    }

    class MemberHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headImg: CircleImageView = itemView.findViewById(R.id.member_headImg)

    }
}