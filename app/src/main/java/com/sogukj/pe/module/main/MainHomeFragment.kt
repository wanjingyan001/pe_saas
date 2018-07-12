package com.sogukj.pe.module.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
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
import com.sogukj.pe.baselibrary.Extended.arrayFromJson
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.database.MainFunIcon
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.bean.MessageIndexBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.database.FunctionViewModel
import com.sogukj.pe.database.Injection
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.creditCollection.ShareHolderDescActivity
import com.sogukj.pe.module.creditCollection.ShareHolderStepActivity
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.other.MessageListActivity
import com.sogukj.pe.module.partyBuild.PartyMainActivity
import com.sogukj.pe.module.user.UserActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.CacheUtils
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.CreditService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.util.ColorUtil
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.experimental.async
import me.leolin.shortcutbadger.ShortcutBadger
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
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
                party_build.backgroundResource = R.drawable.bg_party_build
                party_build.setOnClickListener {
                    PartyMainActivity.start(ctx)
                }
            }
            else -> {
                party_build.backgroundResource = R.drawable.bg_party_build_default
                party_build.isEnabled = false
            }
        }
        toolbar_title.text = when (getEnvironment()) {
            "civc" -> {
                "中缔资本"
            }
            "ht" -> {
                "海通创新"
            }
            "kk" -> {
                "夸克"
            }
            "yge" -> {
                "雅戈尔"
            }
            "sr"->{
                "尚融资本"
            }
            else -> {
                "搜股X-PE"
            }
        }
        val factory = Injection.provideViewModelFactory(ctx)
        val model = ViewModelProviders.of(this, factory).get(FunctionViewModel::class.java)
        model.generateData(baseActivity!!.application)

        moduleAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            val itemView = _adapter.getView(R.layout.item_function_icon, parent)
            object : RecyclerHolder<MainFunIcon>(itemView) {
                val icon = itemView.find<ImageView>(R.id.funIcon)
                val name = itemView.find<TextView>(R.id.functionName)
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
                        .navigation(activity!!, Extras.REQUESTCODE, object : NavigationCallback {
                            override fun onLost(postcard: Postcard?) {
                                Log.d("ARouter", "找不到了")
                            }

                            override fun onFound(postcard: Postcard?) {
                                Log.d("ARouter", "找到了")
                            }

                            override fun onInterrupt(postcard: Postcard?) {
                                Log.d("ARouter", "被拦截了")
                            }

                            override fun onArrival(postcard: Postcard?) {
                                Log.d("ARouter", "跳转完了")
                            }

                        })
            }
        }
        party_build.setOnClickListener {
            PartyMainActivity.start(ctx)
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
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == adapter.datas.size - 1) {
                    page++
                    doRequest()
                }
            }
        })
        noleftviewpager.isScrollble = false

        cache = CacheUtils(ctx)
        Glide.with(ctx).asGif().load(R.drawable.loading).into(pb)
        pb.visibility = View.VISIBLE
        doRequest()

        refresh.setOnRefreshListener {
            page = 1
            doRequest()
            refresh.finishRefresh(1000)
        }
        refresh.isEnableAutoLoadMore = false
    }

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

    lateinit var totalData: ArrayList<MessageBean>

    override fun onResume() {
        super.onResume()
        ShortcutBadger.removeCount(ctx)
        page = 1
        doRequest()
    }

    fun doRequest() {
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

    var local_sp: Int? = null

    inner class HomeAdapter : StackLayout.Adapter<HomeAdapter.MyViewHolder>() {

        var dataList = ArrayList<MessageBean>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(
                    context).inflate(R.layout.item_msg_content_main, parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            var data = dataList[position]
            val strType = when (data.type) {
                1 -> "出勤休假"
                2 -> "用印审批"
                3 -> "签字审批"
                else -> ""
            }
            ColorUtil.setColorStatus(holder.tvState!!, data)
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
                val intent = Intent(context, MessageListActivity::class.java)
                startActivity(intent)
            }
            holder.ll_content?.setOnClickListener {
                val is_mine = if (data.status == -1 || data.status == 4) 1 else 2
                if (data.type == 2) {
                    //SealApproveActivity.start(context, data, is_mine)
                    val intent = Intent(context, SealApproveActivity::class.java)
                    intent.putExtra("is_mine", is_mine)
                    intent.putExtra(Extras.DATA, data)
                    startActivity(intent)
                } else if (data.type == 3) {
                    //SignApproveActivity.start(context, data, is_mine)
                    val intent = Intent(context, SignApproveActivity::class.java)
                    intent.putExtra(Extras.DATA, data)
                    intent.putExtra("is_mine", is_mine)
                    startActivity(intent)
                } else if (data.type == 1) {
                    val intent = Intent(context, LeaveBusinessApproveActivity::class.java)
                    intent.putExtra(Extras.DATA, data)
                    intent.putExtra("is_mine", is_mine)
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class MyViewHolder(view: View) : StackLayout.ViewHolder(view) {

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

            init {
                tvTitle = view.findViewById<TextView>(R.id.tv_title) as TextView
                tvSeq = view.findViewById<TextView>(R.id.sequense) as TextView
                tvNum = view.findViewById<TextView>(R.id.tv_num) as TextView
                tvState = view.findViewById<TextView>(R.id.tv_state) as TextView
                tvFrom = view.findViewById<TextView>(R.id.tv_from) as TextView
                tvType = view.findViewById<TextView>(R.id.tv_type) as TextView
                tvMsg = view.findViewById<TextView>(R.id.tv_msg) as TextView
                tvUrgent = view.findViewById<TextView>(R.id.tv_urgent) as TextView
                ll_content = view.findViewById<LinearLayout>(R.id.ll_content) as LinearLayout
                tvMore = view.findViewById<TextView>(R.id.more) as TextView
            }
        }
    }

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

    inner class ViewPagerAdapter(var datas: ArrayList<MessageBean>, private val mContext: Context) : PagerAdapter() {

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