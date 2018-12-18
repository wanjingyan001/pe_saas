package com.sogukj.pe.module.register

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.widgets.CircleImageView
import org.jetbrains.anko.find

/**
 * Created by admin on 2018/7/3.
 */
class MemberAdapter(val ctx: Context, val members: ArrayList<UserBean>) : RecyclerView.Adapter<MemberAdapter.MemberHolder>() {
    var onItemClick: ((v: View, position: Int) -> Unit)? = null

    override fun onBindViewHolder(holder: MemberHolder, position: Int) {
        if (position == members.size) {
            holder.headImg.setImageResource(R.drawable.invalid_name3)
            holder.headImg.setTag(R.id.member_headImg,Extras.ADD)
        } else if (position == members.size + 1){
            holder.headImg.setImageResource(R.mipmap.invalid_name4)
            holder.headImg.setTag(R.id.member_headImg,Extras.SUBTRACT)
        } else {
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
        holder.headImg.setOnClickListener { v ->
            if (null != onItemClick) {
                onItemClick!!(v, position)
            }
        }
    }

    override fun getItemCount(): Int = members.size + 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberHolder {
        val inflate = LayoutInflater.from(ctx).inflate(R.layout.item_team_member, parent, false)
        return MemberHolder(inflate)
    }

    inner class MemberHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headImg: CircleImageView = itemView.find(R.id.member_headImg)
    }
}