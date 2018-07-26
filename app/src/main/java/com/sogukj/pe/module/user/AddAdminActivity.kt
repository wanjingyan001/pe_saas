package com.sogukj.pe.module.user

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.register.MemberSelectActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_add_admin.*
import org.jetbrains.anko.*
import qdx.stickyheaderdecoration.NormalDecoration

class AddAdminActivity : ToolbarActivity() {
    private lateinit var mAdapter: RecyclerAdapter<UserBean>
    private val mine: UserBean? by lazy { Store.store.getUser(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_admin)
        title = "添加管理员"
        setBack(true)
        mAdapter = RecyclerAdapter(this) { _adapter, parent, type ->
            AdminHolder(_adapter.getView(R.layout.item_admin_list, parent))
        }
        val decoration = object : NormalDecoration() {
            override fun getHeaderName(p0: Int): String {
                return when (mAdapter.dataList[p0].is_admin) {
                    1 -> {
                        "子管理员"
                    }
                    2 -> {
                        "超级管理员"
                    }
                    else -> ""
                }
            }
        }
        decoration.setTextSize(sp(12))
        decoration.setTextColor(R.color.text_3)
        decoration.setHeaderHeight(dip(28))
        decoration.setHeaderContentColor(Color.parseColor("#F7F9FC"))
        adminList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = mAdapter
            addItemDecoration(decoration)
        }
        addAdmins.clickWithTrigger {
            startActivityForResult<MemberSelectActivity>(Extras.REQUESTCODE, Extras.FLAG2 to true)
        }
        getAdministratorList()
    }

    private fun getAdministratorList() {
        SoguApi.getService(application, UserService::class.java)
                .operateAdmin(1)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                mAdapter.refreshData(it.filter { it.uid != null }.sortedByDescending { it.is_admin })
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }


    private fun deleteAdmin(position: Int, id: Int) {
        SoguApi.getService(application, UserService::class.java)
                .operateAdmin(4, id = id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            mAdapter.dataList.removeAt(position)
                            mAdapter.notifyDataSetChanged()
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    private fun addAdmin(list: List<UserBean>) {
        val idStr = StringBuilder()
        list.map { it.uid.toString() }.forEach {
            idStr.append(it, ",")
        }
        SoguApi.getService(application, UserService::class.java)
                .operateAdmin(2, idStr = idStr.substring(0, idStr.length - 1))
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            mAdapter.dataList.addAll(list)
                            mAdapter.notifyDataSetChanged()
                            showSuccessToast("添加成功")
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            list.forEach {
                it.is_admin = 1
            }
            addAdmin(list)
        }
    }

    inner class AdminHolder(itemView: View) : RecyclerHolder<UserBean>(itemView) {
        val selectIcon = itemView.find<ImageView>(R.id.selectIcon)
        val userImg = itemView.find<CircleImageView>(R.id.userHeadImg)
        val userName = itemView.find<TextView>(R.id.userName)
        val userPosition = itemView.find<TextView>(R.id.userPosition)
        val delete = itemView.find<ImageView>(R.id.delete)
        override fun setData(view: View, data: UserBean, position: Int) {
            selectIcon.setVisible(false)
            userPosition.setVisible(true)
            if (data.headImage().isNullOrEmpty()) {
                val ch = data.name.first()
                userImg.setChar(ch)
            } else {
                Glide.with(context)
                        .load(data.headImage())
                        .listener(object : RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                userImg.setChar(data.name.first())
                                return false
                            }

                        })
                        .into(userImg)
            }
            userName.text = data.name
            userPosition.text = data.position
            delete.setVisible(mine?.is_admin!! > data.is_admin)
            delete.clickWithTrigger {
                MaterialDialog.Builder(this@AddAdminActivity)
                        .theme(Theme.LIGHT)
                        .title("删除管理员")
                        .content("确认删除${data.name}的管理员身份?")
                        .positiveText("确定")
                        .negativeText("取消")
                        .onPositive { dialog, which ->
                            deleteAdmin(position, data.uid!!)
                        }.show()
            }
        }
    }
}
