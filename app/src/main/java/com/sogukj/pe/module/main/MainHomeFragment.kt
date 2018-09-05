package com.sogukj.pe.module.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.fashare.stack_layout.StackLayout
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.database.FunctionViewModel
import com.sogukj.pe.database.Injection
import com.sogukj.pe.database.MainFunIcon
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.creditCollection.ShareHolderDescActivity
import com.sogukj.pe.module.creditCollection.ShareHolderStepActivity
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.news.MainNewsActivity
import com.sogukj.pe.module.news.NewsDetailActivity
import com.sogukj.pe.module.other.MessageListActivity
import com.sogukj.pe.module.partyBuild.PartyMainActivity
import com.sogukj.pe.module.user.UserActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.CacheUtils
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.CreditService
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.util.ColorUtil
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_main_home_party_build.view.*
import kotlinx.android.synthetic.main.layout_main_home_module.view.*
import me.leolin.shortcutbadger.ShortcutBadger
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by qinfei on 17/10/11.
 */
class MainHomeFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_home

    lateinit var moduleAdapter: RecyclerAdapter<MainFunIcon>

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
                                            bundle.putInt(Extras.FLAG, Extras.ROUTH_FLAG)
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
        }
        val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(company)
        toolbar_title.text = detail?.mechanism_name ?: "X-PE"
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
            info { functions.jsonStr }
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
            if (mainFunIcon.name == "征信") {
                XmlDb.open(ctx).set("INNER", "FALSE")
                var first = XmlDb.open(ctx).get("FIRST", "TRUE")
                if (first == "FALSE") {
                    SoguApi.getService(baseActivity!!.application, CreditService::class.java)
                            .showCreditList()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    if (payload.payload == null) {
                                        ShareHolderStepActivity.start(context, 1, 0, "")
                                    } else {
                                        if (payload.payload!!.size == 0) {
                                            ShareHolderStepActivity.start(context, 1, 0, "")
                                        } else {
                                            var project = ProjectBean()
                                            project.name = ""
                                            project.company_id = 0
                                            ShareholderCreditActivity.start(context, project)
                                        }
                                    }
                                } else {
                                    ShareHolderStepActivity.start(context, 1, 0, "")
                                }
                            }, { e ->
                                Trace.e(e)
                                ShareHolderStepActivity.start(context, 1, 0, "")
                            })
                } else if (first == "TRUE") {
                    ShareHolderDescActivity.start(context, ProjectBean(), "OUTER")
                    XmlDb.open(ctx).set("FIRST", "FALSE")
                }
            } else {
                val path = mainFunIcon.address + mainFunIcon.port
                //fragment中使用路由调用startActivityForResult回调将在Activity中
                val bundle = Bundle()
                bundle.putInt(Extras.DATA, local_sp!!)
                bundle.putInt(Extras.FLAG, Extras.ROUTH_FLAG)
                ARouter.getInstance().build(path)
                        .with(bundle)
                        .navigation(activity!!, Extras.REQUESTCODE)
            }
        }

        loadHead()
        toolbar_back.setOnClickListener {
            //UserActivity.start(context)
            val intent = Intent(context, UserActivity::class.java)
            startActivityForResult(intent, 0x789)
        }

        adapter = ViewPagerAdapter(ArrayList(), ctx)
        noleftviewpager.adapter = adapter
        noleftviewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            // 没划过去 1---2---0
            // 划过去了 1---2---onPageSelected---0
            // 划到底   1--—0
            override fun onPageScrollStateChanged(state: Int) {
                squence.add("$state")
                if (state == 0) {
                    if (squence.size == 3) {
                        Log.e("没划过去", "没划过去")
                    } else if (squence.size == 4) {
                        Log.e("划过去了", "划过去了")
                    } else if (squence.size == 2) {
                        Log.e("划到底", "划到底")
                        page++
                        doRequest()
                    }
                    squence.clear()
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                squence.add("onPageSelected")
            }
        })
        noleftviewpager.isScrollble = false

        cache = CacheUtils(ctx)
        Glide.with(ctx).asGif().load(R.drawable.loading).into(pb)
        pb.visibility = View.VISIBLE
        //doRequest()

        refresh.setOnRefreshListener {
            page = 1
            doRequest()
            refresh.finishRefresh(1000)
        }
        refresh.isEnableAutoLoadMore = false
    }

    var squence = ArrayList<String>()

    lateinit var adapter: ViewPagerAdapter
    lateinit var cache: CacheUtils
    var page = 1

    override fun onDestroy() {
        super.onDestroy()
        cache.close()
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
        var header = toolbar_back.getChildAt(0) as CircleImageView
        if (user?.url.isNullOrEmpty()) {
            val ch = user?.name?.first()
            header.setChar(ch)
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user?.name?.first()
                            header.setChar(ch)
                            return true
                        }
                    })
                    .into(header)
        }
    }

    fun initHeadTitle(title: String?) {

    }

    lateinit var totalData: ArrayList<Any>

    override fun onResume() {
        super.onResume()
        ShortcutBadger.removeCount(ctx)
        page = 1
        doRequest()
    }

    fun doRequest() {
        var flag = sp.getInt(Extras.main_flag, 1)
        if (flag == 1) {
            doRequestMsgList()
        } else {
            doRequestQB()
        }
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .getNumber()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            local_sp = sp
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    fun doRequestQB() {
        iv_empty.visibility = View.GONE
        noleftviewpager.visibility = View.VISIBLE
        totalData = ArrayList()
        var cacheData = cache.getDiskCacheNews("${Store.store.getUser(ctx)?.uid}")
        if (cacheData != null) {
            if (page == 1) {
                if (Utils.isNetworkError(context)) {
                    adapter.datas.clear()
                    adapter.datas.addAll(cacheData)
                    adapter.notifyDataSetChanged()

                    totalData.clear()
                    totalData.addAll(cacheData)
                } else {
                    adapter.datas.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        }
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .listNews(page = page, type = null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            if (page == 1) {
                                //adapter.datas.clear()
                                //重新设置adapter
                                adapter = ViewPagerAdapter(ArrayList(), ctx)
                                noleftviewpager.adapter = adapter
                            }
                            adapter.datas.addAll(this)
                            adapter.notifyDataSetChanged()

                            if (this.size == 0) {
                                iv_empty.visibility = View.VISIBLE
                                iv_empty.setBackgroundResource(R.drawable.sl)
                                noleftviewpager.visibility = View.GONE
                                iv_empty.setOnClickListener {
                                    iv_empty.visibility = View.GONE
                                    noleftviewpager.visibility = View.VISIBLE
                                    page = 1
                                    doRequest()
                                }
                            }

                            if (page == 1) {
                                cache.addToDiskCacheNews("${Store.store.getUser(ctx)?.uid}", this)
                                totalData.clear()
                                totalData.addAll(this)
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    pb.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                    pb.visibility = View.GONE
                    if (adapter.datas.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                        if (page == 1) {
                            iv_empty.setBackgroundResource(R.drawable.zw)
                        } else {
                            showCustomToast(R.drawable.icon_toast_common, "暂无最新数据")
                            iv_empty.setBackgroundResource(R.drawable.sl)
                        }
                        noleftviewpager.visibility = View.GONE
                    }
                    if (e is UnknownHostException) {
                        iv_empty.visibility = View.VISIBLE
                        iv_empty.setBackgroundResource(R.drawable.dw)
                        noleftviewpager.visibility = View.GONE
                        iv_empty.setOnClickListener {
                            iv_empty.visibility = View.GONE
                            noleftviewpager.visibility = View.VISIBLE
                            page = 1
                            doRequest()
                        }
                    }
                }, {
                    pb.visibility = View.GONE
                    if (adapter.datas.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                        if (page == 1) {
                            iv_empty.setBackgroundResource(R.drawable.zw)
                        } else {
                            showCustomToast(R.drawable.icon_toast_common, "暂无最新数据")
                            iv_empty.setBackgroundResource(R.drawable.sl)
                        }
                        noleftviewpager.visibility = View.GONE
                    }
                })
    }

    fun doRequestMsgList() {
        iv_empty.visibility = View.GONE
        noleftviewpager.visibility = View.VISIBLE
        totalData = ArrayList()
        var cacheData = cache.getDiskCache("${Store.store.getUser(ctx)?.uid}")
        if (cacheData != null) {
            if (page == 1) {
                if (Utils.isNetworkError(context)) {
                    adapter.datas.clear()
                    adapter.datas.addAll(cacheData)
                    adapter.notifyDataSetChanged()

                    totalData.clear()
                    totalData.addAll(cacheData)
                } else {
                    adapter.datas.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        }
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .msgList(page = page, pageSize = 20, status = 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            if (page == 1) {
                                //adapter.datas.clear()
                                //重新设置adapter
                                adapter = ViewPagerAdapter(ArrayList(), ctx)
                                noleftviewpager.adapter = adapter
                            }
                            adapter.datas.addAll(this)
                            adapter.notifyDataSetChanged()

                            if (this.size == 0) {
                                iv_empty.visibility = View.VISIBLE
                                iv_empty.setBackgroundResource(R.drawable.sl)
                                noleftviewpager.visibility = View.GONE
                                iv_empty.setOnClickListener {
                                    iv_empty.visibility = View.GONE
                                    noleftviewpager.visibility = View.VISIBLE
                                    page = 1
                                    doRequest()
                                }
                            }

                            if (page == 1) {
                                cache.addToDiskCache("${Store.store.getUser(ctx)?.uid}", this)
                                totalData.clear()
                                totalData.addAll(this)
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    pb.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                    pb.visibility = View.GONE
                    if (adapter.datas.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                        if (page == 1) {
                            iv_empty.setBackgroundResource(R.drawable.zw)
                        } else {
                            showCustomToast(R.drawable.icon_toast_common, "暂无最新数据")
                            iv_empty.setBackgroundResource(R.drawable.sl)
                        }
                        noleftviewpager.visibility = View.GONE
                    }
                    if (e is UnknownHostException) {
                        iv_empty.visibility = View.VISIBLE
                        iv_empty.setBackgroundResource(R.drawable.dw)
                        noleftviewpager.visibility = View.GONE
                        iv_empty.setOnClickListener {
                            iv_empty.visibility = View.GONE
                            noleftviewpager.visibility = View.VISIBLE
                            page = 1
                            doRequest()
                        }
                    }
                }, {
                    pb.visibility = View.GONE
                    if (adapter.datas.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                        if (page == 1) {
                            iv_empty.setBackgroundResource(R.drawable.zw)
                        } else {
                            showCustomToast(R.drawable.icon_toast_common, "暂无最新数据")
                            iv_empty.setBackgroundResource(R.drawable.sl)
                        }
                        noleftviewpager.visibility = View.GONE
                    }
                })
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

    inner class ViewPagerAdapter(var datas: ArrayList<Any>, private val mContext: Context) : PagerAdapter() {

        private var mViewCache: LinkedList<View>? = null

        private var mLayoutInflater: LayoutInflater? = null


        init {
            this.mLayoutInflater = LayoutInflater.from(mContext)
            this.mViewCache = LinkedList()
        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return this.datas!!.size
        }

        override fun getItemPosition(`object`: Any): Int {
            return super.getItemPosition(`object`)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var holder: ViewHolder? = null
            var convertView: View? = null
            if (mViewCache!!.size == 0) {
                convertView = mLayoutInflater!!.inflate(R.layout.item_msg_content_main, null, false)
                holder = ViewHolder()
                holder.tvTitle = convertView.findViewById<TextView>(R.id.tv_title) as TextView
                holder.tvSeq = convertView.findViewById<TextView>(R.id.sequense) as TextView
                holder.tvNum = convertView.findViewById<TextView>(R.id.tv_num) as TextView
                holder.tvState = convertView.findViewById<TextView>(R.id.tv_state) as TextView
                holder.tvFrom = convertView.findViewById<TextView>(R.id.tv_from) as TextView
                holder.tvType = convertView.findViewById<TextView>(R.id.tv_type) as TextView
                holder.tvMsg = convertView.findViewById<TextView>(R.id.tv_msg) as TextView
                holder.tvUrgent = convertView.findViewById<TextView>(R.id.tv_urgent) as TextView
                holder.ll_content = convertView.findViewById<LinearLayout>(R.id.ll_content) as LinearLayout
                holder.tvMore = convertView.findViewById<TextView>(R.id.more) as TextView

                convertView.tag = holder
            } else {
                convertView = mViewCache!!.removeFirst()
                holder = convertView!!.tag as ViewHolder
            }

            var data = datas!![position]
            if (data is MessageBean) {
                val strType = when (data.type) {
                    1 -> "出勤休假"
                    2 -> "用印审批"
                    3 -> "签字审批"
                    else -> ""
                }
                //ColorUtil.setColorStatus(holder.tvState!!, data)
                holder.tvState!!.text = data.status_str
                try {
                    holder.tvTitle?.text = strType
                    holder.tvSeq?.text = data.title
                } catch (e: Exception) {
                }
                holder.tvFrom?.text = "发起人:" + data.username
                holder.tvType?.text = "类型:" + data.type_name
                holder.tvMsg?.text = "审批事由:" + data.reasons
                val cnt = data.message_count
                holder.tvNum?.text = "${cnt}"
                if (cnt != null && cnt > 0)
                    holder.tvNum?.visibility = View.VISIBLE
                else
                    holder.tvNum?.visibility = View.GONE
                val urgnet = data.urgent_count
                holder.tvUrgent?.text = "加急x${urgnet}"
                if (urgnet != null && urgnet > 0)
                    holder.tvUrgent?.visibility = View.VISIBLE
                else
                    holder.tvUrgent?.visibility = View.GONE

                holder.tvMore?.setOnClickListener {
                    var bean = MessageIndexBean()
                    bean.flag = 1
                    MessageListActivity.start(baseActivity, bean)
                }
                holder.ll_content?.setOnClickListener {
                    val is_mine = if (data.status == -1 || data.status == 4) 1 else 2
                    if (data.type == 2) {
                        //SealApproveActivity.start(context, data, is_mine)
                        val intent = Intent(context, SealApproveActivity::class.java)
                        intent.putExtra("is_mine", is_mine)
                        intent.putExtra(Extras.DATA, data)
                        startActivityForResult(intent, SHENPI)
                    } else if (data.type == 3) {
                        //SignApproveActivity.start(context, data, is_mine)
                        val intent = Intent(context, SignApproveActivity::class.java)
                        intent.putExtra(Extras.DATA, data)
                        intent.putExtra("is_mine", is_mine)
                        startActivityForResult(intent, SHENPI)
                    } else if (data.type == 1) {
                        val intent = Intent(context, LeaveBusinessApproveActivity::class.java)
                        intent.putExtra(Extras.DATA, data)
                        intent.putExtra("is_mine", is_mine)
                        startActivityForResult(intent, SHENPI)
                    }
                }

                container.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            } else if (data is NewsBean) {
                holder.tvTitle?.text = "舆情"
                holder.tvNum?.visibility = View.GONE

                holder.tvSeq?.text = data.company
                if (data.company.isNullOrEmpty()) {
                    holder.tvSeq?.visibility = View.GONE
                } else {
                    holder.tvSeq?.visibility = View.VISIBLE
                }

//                if (data.tag.isNullOrEmpty()) {
//                    holder.tvState!!.visibility = View.GONE
//                } else {
//                    holder.tvState?.text = data.tag?.split("#")?.get(1)
//                }
                holder.tvUrgent?.visibility = View.GONE
                holder.tvFrom?.text = data.source
                if (data.source.isNullOrEmpty()) {
                    holder.tvFrom?.visibility = View.GONE
                } else {
                    holder.tvFrom?.visibility = View.VISIBLE
                }
                holder.tvType?.text = data.time
                holder.tvMsg?.text = Html.fromHtml(data.title)


                holder.tvMore?.setOnClickListener {
                    MainNewsActivity.start(baseActivity)
                }
                holder.ll_content?.setOnClickListener {
                    NewsDetailActivity.start(baseActivity, data)
                }

                if (data.table_id == 1) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "法律诉讼"
                } else if (data.table_id == 2) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "法院公告"
                } else if (data.table_id == 3) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "失信人"
                } else if (data.table_id == 4) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "被执行人"
                } else if (data.table_id == 5) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "行政处罚"
                } else if (data.table_id == 6) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "严重违法"
                } else if (data.table_id == 7) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "股权出质"
                } else if (data.table_id == 8) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "动产抵押"
                } else if (data.table_id == 9) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "欠税公告"
                } else if (data.table_id == 10) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "经营异常"
                } else if (data.table_id == 11) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "开庭公告"
                } else if (data.table_id == 12) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "司法拍卖"
                } else if (data.table_id == 13) {
                    holder.tvState?.visibility = View.VISIBLE
                    holder.tvState?.text = "新闻舆情"
                } else {
                    holder.tvState?.visibility = View.GONE
                }

                container.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }

            return convertView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val contentView = `object` as View
            container.removeView(contentView)
            this.mViewCache!!.add(contentView)
        }

        override fun isViewFromObject(view: View, o: Any): Boolean {
            return view === o
        }

        inner class ViewHolder {
            var tvTitle: TextView? = null
            var tvSeq: TextView? = null
            var tvNum: TextView? = null
            var tvState: TextView? = null
            var tvFrom: TextView? = null
            var tvType: TextView? = null
            var tvMsg: TextView? = null
            var tvUrgent: TextView? = null
            var ll_content: LinearLayout? = null
            var tvMore: TextView? = null
        }
    }
}