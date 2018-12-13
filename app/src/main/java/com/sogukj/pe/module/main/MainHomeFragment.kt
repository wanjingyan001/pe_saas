package com.sogukj.pe.module.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.database.FunctionViewModel
import com.sogukj.pe.database.Injection
import com.sogukj.pe.database.MainFunIcon
import com.sogukj.pe.module.main.adapter.MainMsgPageAdapter
import com.sogukj.pe.module.main.adapter.MsgPageScrollListener
import com.sogukj.pe.module.partyBuild.PartyMainActivity
import com.sogukj.pe.module.user.UserActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.fragment_home.*
import me.leolin.shortcutbadger.ShortcutBadger
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import java.net.UnknownHostException


/**
 * Created by qinfei on 17/10/11.
 */
class MainHomeFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_home

    private lateinit var moduleAdapter: RecyclerAdapter<MainFunIcon>
    private val msgPageAdapter: MainMsgPageAdapter by lazy {
        MainMsgPageAdapter(ctx)
    }
    var page = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x789) {
            loadHead()
        } else if (requestCode == SHENPI) {
            page = 1
            doRequest()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (getEnvironment()) {
            "ht" -> {
                partyLayout.inflate()
                find<ImageView>(R.id.party_build).clickWithTrigger {
                    PartyMainActivity.start(ctx)
                }
            }
            else -> {
                modulesLayout.inflate()
                getTopModules()
            }
        }
        val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(company)
        toolbar_title.text = detail?.mechanism_name ?: "XPE"

        homeMsgPage.apply {
            layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
            adapter = msgPageAdapter
            addOnScrollListener(MsgPageScrollListener {
                page += 1
                doRequest()
            })
        }
        PagerSnapHelper().attachToRecyclerView(homeMsgPage)

        val factory = Injection.provideViewModelFactory(ctx)
        val model = ViewModelProviders.of(this, factory).get(FunctionViewModel::class.java)

        moduleAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            val itemView = _adapter.getView(R.layout.item_function_icon, parent)
            object : RecyclerHolder<MainFunIcon>(itemView) {
                val icon = itemView.find<ImageView>(R.id.funIcon)
                val name = itemView.find<TextView>(R.id.functionName)
                val count = itemView.find<TextView>(R.id.pointCount)
                override fun setData(view: View, data: MainFunIcon, position: Int) {
                    Glide.with(ctx)
                            .load(data.icon)
                            .thumbnail(0.1f)
                            .into(icon)
                    name.text = data.name
                }
            }
        }

        model.getMainModules().observe(this, Observer<List<MainFunIcon>> { functions ->
            functions?.let {
                moduleAdapter.dataList.clear()
                moduleAdapter.dataList.addAll(it)
                moduleAdapter.notifyDataSetChanged()
            }
        })
        mainModuleList.apply {
            layoutManager = GridLayoutManager(ctx, 4)
            adapter = moduleAdapter
        }
        moduleAdapter.onItemClick = { v, p ->
            val mainFunIcon = moduleAdapter.dataList[p]
            val path = mainFunIcon.address + mainFunIcon.port
            //fragment中使用路由调用startActivityForResult回调将在Activity中
            when (path) {
                ARouterPath.ReceiptHeaderActivity -> ARouter.getInstance().build(path)
                        .withString(Extras.TITLE, "发票抬头")
                        .withInt(Extras.TYPE, 1)
                        .navigation()
                ARouterPath.ApproveListActivity -> ARouter.getInstance().build(path)
                        .navigation()
                else -> {
                    val bundle = Bundle()
                    bundle.putInt(Extras.DATA, local_sp ?: 0)
                    bundle.putInt(Extras.FLAG, Extras.ROUTH_FLAG)
                    bundle.putString(Extras.TITLE, mainFunIcon.name)
                    ARouter.getInstance().build(path)
                            .with(bundle)
                            .navigation(activity!!, Extras.REQUESTCODE)
                }
            }
        }

        loadHead()
        toolbar_back.setOnClickListener {
            val intent = Intent(context, UserActivity::class.java)
            startActivityForResult(intent, 0x789)
        }
        Glide.with(ctx).asGif().load(R.drawable.loading).into(pb)
        pb.visibility = View.VISIBLE
        refresh.setOnRefreshListener {
            page = 1
            doRequest()
            refresh.finishRefresh(1000)
        }
        refresh.isEnableAutoLoadMore = false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            onResume()
            loadHead()
        }
    }

    fun loadHead() {
        val user = Store.store.getUser(baseActivity!!)
        val header = toolbar_back.getChildAt(0) as CircleImageView
        if (user?.url.isNullOrEmpty()) {
            if (!user?.name.isNullOrEmpty()) {
                val ch = user?.name?.first()
                header.setChar(ch)
            }
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            if (!user?.name.isNullOrEmpty()) {
                                val ch = user?.name?.first()
                                header.setChar(ch)
                            }
                            return true
                        }
                    })
                    .into(header)
        }
    }

    override fun onResume() {
        super.onResume()
        ShortcutBadger.removeCount(ctx)
        page = 1
        doRequest()
        if (getEnvironment() != "ht") {
            getTopModules()
        }
    }

    private fun getTopModules() {
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .getFourModules()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val layout = find<LinearLayout>(R.id.fourModules)
                            payload.payload?.forEachIndexed { index, mainModule ->
                                val childView = layout.getChildAt(index) as LinearLayout
                                val numTv = childView.getChildAt(0) as TextView
                                val nameTv = childView.getChildAt(1) as TextView
                                numTv.text = mainModule.num
                                nameTv.text = mainModule.name
                                childView.clickWithTrigger {
                                    val path = mainModule.address + mainModule.port
                                    val bundle = Bundle()
                                    bundle.putInt(Extras.FLAG2, Extras.ROUTH_FLAG)
                                    //fragment中使用路由调用startActivityForResult回调将在Activity中
                                    ARouter.getInstance()
                                            .build(path)
                                            .with(bundle)
                                            .navigation(activity!!)
                                }
                            }
                        }
                    }
                }
    }

    fun doRequest() {
        getMsgPageDatas()
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .getNumber()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                local_sp = sp
                            }
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }
    }


    var local_sp: Int? = null


    val colorGray = Color.parseColor("#D9D9D9")
    fun disable(view: TextView) {
        view.compoundDrawables[1]?.setColorFilter(colorGray, PorterDuff.Mode.SRC_ATOP)
        view.setOnClickListener(null)
    }

    companion object {
        val TAG = MainHomeFragment::class.java.simpleName

        fun newInstance(): MainHomeFragment {
            val fragment = MainHomeFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }

    var SHENPI = 0x500


    // 根据情况请求审批或舆情接口
    private fun getMsgPageDatas() {
        val flag = sp.getInt(Extras.main_flag, 1)
//        val flag = 1
        if (flag == 1) {
            //获取审批列表
            SoguApi.getService(ctx, ApproveService::class.java).getApproveList(1, page)
                    .execute {
                        onNext {
                            it.isOk.yes {
                                it.payload?.let {
                                    if (page == 1) {
                                        msgPageAdapter.dataList.clear()
                                    }
                                    msgPageAdapter.refreshData(it.data)
                                }
                            }.otherWise {
                                showErrorToast(it.message)
                            }
                        }
                        onError { e ->
                            getMsgPageDataError(e)
                        }
                        onComplete {
                            getMsgPageDataComplete()
                        }
                    }
        } else {
            //获取舆情
            SoguApi.getService(ctx, NewService::class.java).listNews(page = page, type = 0)
                    .execute {
                        onNext {
                            it.isOk.yes {
                                it.payload?.let {
                                    if (page == 1) {
                                        msgPageAdapter.dataList.clear()
                                    }
                                    msgPageAdapter.refreshData(it)
                                }
                            }.otherWise {
                                showErrorToast(it.message)
                            }
                        }
                        onError { e ->
                            getMsgPageDataError(e)
                        }
                        onComplete {
                            getMsgPageDataComplete()
                        }
                    }
        }
    }

    private fun getMsgPageDataComplete() {
        if (null != pb) {
            pb.visibility = View.GONE
        }
        if (msgPageAdapter.dataList.size == 0) {
            iv_empty.visibility = View.VISIBLE
            if (page == 1) {
                iv_empty.setBackgroundResource(R.drawable.zw)
            } else {
                showCustomToast(R.drawable.icon_toast_common, "暂无最新数据")
                iv_empty.setBackgroundResource(R.drawable.sl)
            }
            homeMsgPage.visibility = View.GONE
        }
    }

    private fun getMsgPageDataError(e: Throwable) {
        Trace.e(e)
        ToastError(e)
        pb.visibility = View.GONE
        if (msgPageAdapter.dataList.size == 0) {
            iv_empty.visibility = View.VISIBLE
            if (page == 1) {
                iv_empty.setBackgroundResource(R.drawable.zw)
            } else {
                showCustomToast(R.drawable.icon_toast_common, "暂无最新数据")
                iv_empty.setBackgroundResource(R.drawable.sl)
            }
            homeMsgPage.visibility = View.GONE
        }
        if (e is UnknownHostException) {
            iv_empty.visibility = View.VISIBLE
            iv_empty.setBackgroundResource(R.drawable.dw)
            homeMsgPage.visibility = View.GONE
            iv_empty.setOnClickListener {
                iv_empty.visibility = View.GONE
                homeMsgPage.visibility = View.VISIBLE
                page = 1
                doRequest()
            }
        }
    }
}