package com.sogukj.pe.module.user

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarFragment
import com.sogukj.pe.baselibrary.utils.HeaderImgKey
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.project.ProjectFocusActivity
import com.sogukj.pe.module.project.ProjectListFragment
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user.*
import org.jetbrains.anko.support.v4.ctx

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
/**
 * Created by qinfei on 17/7/18.
 */
class UserFragment : ToolbarFragment(), View.OnClickListener {
    override val containerViewId: Int
        get() = R.layout.activity_user

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_title.text = "个人中心"
        toolbar_back.setOnClickListener {
            baseActivity!!.finish()
        }
        SoguApi.getService(baseActivity!!.application, UserService::class.java)
                .userDepart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        departList.clear()
                        payload.payload?.forEach {
                            departList.add(it)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })

        ll_user.clickWithTrigger {
            UserEditActivity.start(activity, departList)
        }
        documentManagement.clickWithTrigger {
            FileMainActivity.start(ctx)
        }
        setting.clickWithTrigger {
            SettingActivity.start(context)
        }
        focus_layout.clickWithTrigger {
            ProjectFocusActivity.start(activity, ProjectListFragment.TYPE_GZ)
        }
        tv_1.setOnClickListener(this)
        tv_11.setOnClickListener(this)
        tv_2.setOnClickListener(this)
        tv_22.setOnClickListener(this)
        tv_3.setOnClickListener(this)
        tv_33.setOnClickListener(this)
        tv_4.setOnClickListener(this)
        tv_44.setOnClickListener(this)
        tv_5.setOnClickListener(this)
        tv_55.setOnClickListener(this)
        toolbar_menu.clickWithTrigger {
            //切换用户没有正确显示
            Store.store.getUser(ctx)?.let {
                CardActivity.start(activity, it)
            }
        }

    }

    val departList = ArrayList<DepartmentBean>()


    private fun getBelongBean(userId: Int) {
        SoguApi.getService(baseActivity!!.application, UserService::class.java)
                .getBelongProject(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            it.dy?.let {
                                tv_1.text = it.count.toString()
                                point1.visibility = if (it.red == null || it.red == 0) View.GONE else View.VISIBLE
                            }
                            it.cb?.let {
                                tv_2.text = it.count.toString()
                                point2.visibility = if (it.red == null || it.red == 0) View.GONE else View.VISIBLE
                            }
                            it.lx?.let {
                                tv_3.text = it.count.toString()
                                point3.visibility = if (it.red == null || it.red == 0) View.GONE else View.VISIBLE
                            }
                            it.yt?.let {
                                tv_4.text = it.count.toString()
                                point4.visibility = if (it.red == null || it.red == 0) View.GONE else View.VISIBLE
                            }
                            it.tc?.let {
                                tv_5.text = it.count.toString()
                                point5.visibility = if (it.red == null || it.red == 0) View.GONE else View.VISIBLE
                            }
                            it.gz?.let {
                                tv_6.text = it.count.toString()
                                point.visibility = if (it.red == null || it.red == 0) View.GONE else View.VISIBLE
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            val user = Store.store.getUser(ctx)
            user?.uid?.let { getBelongBean(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        val user = Store.store.getUser(ctx)
        if (null != user?.uid) {
            SoguApi.getService(baseActivity!!.application, UserService::class.java)
                    .userInfo(user.uid!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                Store.store.setUser(ctx, this)
                                updateUser(this)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        ToastError(e)
                    })
        }
        user?.uid?.let { getBelongBean(it) }
    }

    private fun updateUser(user: UserBean?) {
        if (null == user) return
        tv_name?.text = user.name
        tv_position?.text = user.position
        if (!TextUtils.isEmpty(user.email))
            tv_mail?.text = user.email
        if (user.url != null && !TextUtils.isEmpty(user.url)) {
            Glide.with(this@UserFragment)
                    .load(user.headImage())
                    .apply(RequestOptions().signature(HeaderImgKey(user.url)))
                    .transition(GenericTransitionOptions())
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user.name.first()
                            iv_user.setChar(ch)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                    })
                    .into(iv_user)
        } else {
            val ch = user.name.first()
            iv_user.setChar(ch)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_1, R.id.tv_11 -> {
                ProjectFocusActivity.start(activity, ProjectListFragment.TYPE_DY)
            }
            R.id.tv_2, R.id.tv_22 -> {
                ProjectFocusActivity.start(activity, ProjectListFragment.TYPE_CB)
            }
            R.id.tv_3, R.id.tv_33 -> {
                ProjectFocusActivity.start(activity, ProjectListFragment.TYPE_LX)
            }
            R.id.tv_4, R.id.tv_44 -> {
                ProjectFocusActivity.start(activity, ProjectListFragment.TYPE_YT)
            }
            R.id.tv_5, R.id.tv_55 -> {
                ProjectFocusActivity.start(activity, ProjectListFragment.TYPE_TC)
            }
        }
    }


    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserFragment::class.java))
        }

        fun newInstance(): UserFragment {
            val fragment = UserFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
