package com.sogukj.pe.module.project

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.InputType
import android.text.TextUtils
import android.util.Log
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
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.ArrayPagerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.user.UserActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.ProjectService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main_project.*
import kotlinx.android.synthetic.main.search_view.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColor
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class MainProjectFragment : BaseRefreshFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_project

    lateinit var fragments: ArrayList<BaseFragment>
    lateinit var adapter: RecyclerAdapter<ProjectBean>

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            loadHead()
        }
    }

    fun loadHead() {
        val user = Store.store.getUser(baseActivity!!)
        var header = headerIcon.getChildAt(0) as CircleImageView
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

    fun getViewPager(): ViewPager {
        return view_pager
    }

    lateinit var hisAdapter: RecyclerAdapter<String>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadHead()
        headerIcon.setOnClickListener {
            val intent = Intent(context, UserActivity::class.java)
            startActivityForResult(intent, 0x789)
        }

        dangerous.setOnClickListener {
            DangerousActivity.start(baseActivity)
        }

        adapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent)
            object : RecyclerHolder<ProjectBean>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                val tv2 = convertView.findViewById<TextView>(R.id.tv2) as TextView
                val tv3 = convertView.findViewById<TextView>(R.id.tv3) as TextView

                override fun setData(view: View, data: ProjectBean, position: Int) {
                    var label = data.name
                    data.shortName?.apply {
                        if (this != "") {
                            label = this
                        }
                    }
                    if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
                        label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
                    }
                    tv1.text = Html.fromHtml(label)
                    if (type == 1) {
                        tv2.text = when (data.status) {
                            2 -> "已完成"
                            else -> "准备中"
                        }
                    } else {
                        tv2.text = data.state
                        tv2.textColor = when (data.state) {
                            "初创" -> Color.parseColor("#9e9e9e")
                            "天使轮" -> Color.parseColor("#e64a19")
                            "A轮" -> Color.parseColor("#f57c00")
                            "B轮" -> Color.parseColor("#ffa000")
                            "C轮" -> Color.parseColor("#fbc02d")
                            "D轮" -> Color.parseColor("#afb42b")
                            "E轮" -> Color.parseColor("#689f38")
                            "PIPE轮" -> Color.parseColor("#388e3c")
                            "新三板" -> Color.parseColor("#00796b")
                            "IPO" -> Color.parseColor("#0097a7")
                            else -> Color.parseColor("#9e9e9e")
                        }
                        val bg = when (data.state) {
                            "初创" -> R.drawable.bg_border_proj1
                            "天使轮" -> R.drawable.bg_border_proj2
                            "A轮" -> R.drawable.bg_border_proj3
                            "B轮" -> R.drawable.bg_border_proj4
                            "C轮" -> R.drawable.bg_border_proj5
                            "D轮" -> R.drawable.bg_border_proj6
                            "E轮" -> R.drawable.bg_border_proj7
                            "PIPE轮" -> R.drawable.bg_border_proj8
                            "新三板" -> R.drawable.bg_border_proj9
                            "IPO" -> R.drawable.bg_border_proj10
                            else -> R.drawable.bg_border_proj1
                        }
                        tv2.setBackgroundResource(bg)

                    }
                    tv3.text = when (type) {
                        1 -> data.add_time
                        else -> data.update_time
                    }

                    if (tv2.text.isNullOrEmpty()) tv2.text = "--"
                    if (tv3.text.isNullOrEmpty()) tv3.text = "--"
                }

            }
        })
        hisAdapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_search_item, parent)
            object : RecyclerHolder<String>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                val delete = convertView.findViewById<ImageView>(R.id.delete) as ImageView

                override fun setData(view: View, data: String, position: Int) {
                    tv1.text = data
                    delete.setOnClickListener {
                        hisAdapter.dataList.removeAt(position)
                        hisAdapter.notifyDataSetChanged()
                        Store.store.projectSearchRemover(baseActivity!!, position)
                    }
                }

            }
        })
        run {
            val layoutManager = LinearLayoutManager(baseActivity)
            recycler_his.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
            recycler_his.layoutManager = layoutManager
            recycler_his.adapter = hisAdapter
            hisAdapter.onItemClick = { v, p ->
                val data = hisAdapter.dataList.get(p)
                search_view.search = (data)
                doSearch(data)

            }
        }
        run {
            val layoutManager = LinearLayoutManager(baseActivity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recycler_result.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
            recycler_result.layoutManager = layoutManager
            recycler_result.adapter = adapter

            adapter.onItemClick = { v, p ->
                val project = adapter.getItem(p)
                val intent = Intent(context, ProjectActivity::class.java)
                intent.putExtra(Extras.DATA, project)
                intent.putExtra(Extras.TYPE, mTypeList.get(view_pager.currentItem))
                startActivity(intent)
            }
        }
        tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_project, 0))

        search_view.tv_cancel.visibility = View.VISIBLE
        val search = Store.store.projectSearch(baseActivity!!)


        search_view.tv_cancel.setOnClickListener {
            this.key = ""
            search_view.search = ""
            ll_search.visibility = View.GONE
            toolbar.visibility = View.VISIBLE

            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(search)
            hisAdapter.notifyDataSetChanged()
            ll_history.visibility = View.VISIBLE
        }
        iv_clear.setOnClickListener {
            MaterialDialog.Builder(ctx)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确认全部删除?")
                    .positiveText("确认")
                    .negativeText("取消")
                    .onPositive { dialog, which ->
                        Store.store.projectSearchClear(baseActivity!!)
                        hisAdapter.dataList.clear()
                        hisAdapter.notifyDataSetChanged()
                        last_search_layout.visibility = View.GONE
                    }
                    .show()
        }
        search_view.onSearch = { text ->
            if (null != text && !TextUtils.isEmpty(text))
                doSearch(text)
        }

        fb_add.setOnClickListener {
            if (view_pager.currentItem == 0) {
                var intent = Intent(context, ProjectAddActivity::class.java)
                intent.putExtra(Extras.TYPE, "ADD")
                startActivityForResult(intent, 0x543)
            } else if (view_pager.currentItem == 1) {
                val intent = Intent(context, StoreProjectAddActivity::class.java)
                intent.putExtra(Extras.TYPE, StoreProjectAddActivity.TYPE_ADD)
                startActivityForResult(intent, 0x543)
            }
        }
        fb_search.setOnClickListener {
            toolbar.visibility = View.GONE
            ll_search.visibility = View.VISIBLE
            et_search.postDelayed({
                et_search.inputType = InputType.TYPE_CLASS_TEXT
                et_search.isFocusable = true
                et_search.isFocusableInTouchMode = true
                et_search.requestFocus()
                Utils.toggleSoftInput(baseActivity, et_search)
            }, 100)
            if (Store.store.projectSearch(baseActivity!!).isEmpty()) {
                last_search_layout.visibility = View.GONE
            } else {
                last_search_layout.visibility = View.VISIBLE
            }
        }

        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
            }

        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            //滑到新页面才算，没滑到又返回不算
            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
                fb_add.visibility = if (position == 0) View.VISIBLE else View.GONE
                if (previousState == "TOP") {
                    tabs.setBackgroundResource(R.drawable.tab_bg_2)
                    tabs.setTabTextColors(Color.parseColor("#ff7bb4fc"), Color.parseColor("#ffffff"))
                    for (i in 0 until tabs.tabCount) {
                        if (i == tabs.selectedTabPosition) {
                            setDrawable(i, "2", true)
                        } else {
                            setDrawable(i, "2", false)
                        }
                    }
                } else {
                    tabs.setBackgroundResource(R.drawable.tab_bg_1)
                    tabs.setTabTextColors(Color.parseColor("#a0a4aa"), Color.parseColor("#282828"))
                    for (i in 0 until tabs.tabCount) {
                        if (i == tabs.selectedTabPosition) {
                            setDrawable(i, "1", true)
                        } else {
                            setDrawable(i, "1", false)
                        }
                    }
                }
            }

        })

        hisAdapter.dataList.clear()
        hisAdapter.dataList.addAll(search)
        hisAdapter.notifyDataSetChanged()

        tabs.removeAllTabs()
        mTypeList.clear()
        mTypeMap.clear()
        SoguApi.getService(baseActivity!!.application,ProjectService::class.java)
                .showStage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        fragments = ArrayList<BaseFragment>()
                        payload?.payload?.forEach {
                            mTypeList.add(it.type!!)
                            mTypeMap.put(it.type!!, it.name!!)
                            fragments.add(ProjectListFragment.newInstance(it.type!!))
                            var tab = tabs.newTab().setText(it.name!!)
                            tabs.addTab(tab)

                            Thread {
                                var target = Glide.with(ctx).load(it.icon).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)//FutureTarget<File>
                                var imageFile = target.get()//File
                                activity?.runOnUiThread {
                                    tab.icon = BitmapDrawable(resources, imageFile.path)
                                    //icon.setImageDrawable(BitmapDrawable(resources, imageFile.path))
                                }
                            }.start()
                        }
                        var adapter = ArrayPagerAdapter(childFragmentManager, fragments.toTypedArray())
                        view_pager.adapter = adapter
                        view_pager.offscreenPageLimit = fragments.size

                        if (mTypeList.size % 2 == 0) {
                            tabs.getTabAt(mTypeList.size / 2 - 1)?.select()
                            view_pager?.currentItem = mTypeList.size / 2 - 1
                        } else {
                            tabs.getTabAt(mTypeList.size / 2)?.select()
                            view_pager?.currentItem = mTypeList.size / 2
                        }

                        (0 until tabs.tabCount)
                                .map { tabs.getTabAt(it) }
                                .forEach { Utils.changeTabIcon(it) }
                        if (tabs.tabCount < 5) {
                            tabs.tabMode = TabLayout.MODE_FIXED
                        } else {
                            tabs.tabMode = TabLayout.MODE_SCROLLABLE
                            tabs.viewTreeObserver.addOnGlobalLayoutListener {
                                Utils.setTabWidth(tabs, context, tabs.width / 5 + 5)
                            }
                        }
                    } else {
                        //showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })


        mAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (mAppBarLayout.height > 0) {
                var appBarHeight = mAppBarLayout.height
                var toolbarHeight = toolbar.height

                Log.e("appBarHeight", "${appBarHeight}")//256
                Log.e("toolbarHeight", "${toolbarHeight}")//112
                Log.e("verticalOffset", "${verticalOffset}")//112

                var currentState = ""

                if (toolbarHeight - Math.abs(verticalOffset).toFloat() < 5) {
                    //移动到顶端
                    currentState = "TOP"

                    tabs.alpha = 1.0f

                    if (currentState == previousState) {
                        return@OnOffsetChangedListener
                    }
                    previousState = currentState
                    tabs.setBackgroundResource(R.drawable.tab_bg_2)
                    tabs.setTabTextColors(Color.parseColor("#ff7bb4fc"), Color.parseColor("#ffffff"))
                    for (i in 0 until tabs.tabCount) {
                        if (i == tabs.selectedTabPosition) {
                            setDrawable(i, "2", true)
                        } else {
                            setDrawable(i, "2", false)
                        }
                    }
                } else {
                    //不是顶端
                    currentState = "NO_TOP"

                    var mAlpha = 1.0f - Math.abs(verticalOffset).toFloat() / toolbarHeight
                    tabs.alpha = mAlpha

                    if (currentState == previousState) {
                        return@OnOffsetChangedListener
                    }
                    previousState = currentState
                    tabs.setBackgroundResource(R.drawable.tab_bg_1)
                    tabs.setTabTextColors(Color.parseColor("#a0a4aa"), Color.parseColor("#282828"))
                    for (i in 0 until tabs.tabCount) {
                        if (i == tabs.selectedTabPosition) {
                            setDrawable(i, "1", true)
                        } else {
                            setDrawable(i, "1", false)
                        }
                    }
                }
            }
        })
    }
    override fun doRefresh() {
        offset = 0
        handler.post(searchTask)
    }

    override fun doLoadMore() {
        offset = this@MainProjectFragment.adapter.dataList.size
        handler.post(searchTask)
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x543) {
            (fragments[view_pager.currentItem] as ProjectListFragment).request()
        } else if (requestCode == 0x789) {
            loadHead()
        }
    }

    var previousState = "TOP"// NO_TOP

    /**
     * @param index--------(tabs对应的index，分别对应dy,cb等)
     * @param state---------（1，2）  1不是顶端，2是顶端
     * @param isSelect--------是否选中
     */
    private fun setDrawable(index: Int, state: String, isSelect: Boolean) {
        var drawable = tabs.getTabAt(index)!!.icon
        if (state == "1") {
            drawable?.clearColorFilter()
        } else {
            drawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }
        if (isSelect) {
            drawable?.alpha = 255
        } else {
            drawable?.alpha = 128
        }
        tabs.getTabAt(index)!!.icon = drawable
    }

    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var key = ""
    var offset = 0
    @SuppressLint("StringFormatMatches")
    fun doSearch(text: String) {
        this.key = text
        if (TextUtils.isEmpty(key)) return
        val user = Store.store.getUser(baseActivity!!)
        var type = mTypeList.get(tabs.selectedTabPosition)
        val tmplist = LinkedList<String>()
        tmplist.add(text)
        Store.store.projectSearch(baseActivity!!, tmplist)
        SoguApi.getService(baseActivity!!.application,NewService::class.java)
                .listProject(offset = offset, pageSize = 20, uid = user?.uid, type = type, fuzzyQuery = text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.apply {
                            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_project, (total as Double).toInt()))
                        }
                        if (offset == 0)
                            adapter.dataList.clear()
                        payload.payload?.let {
                            adapter.dataList.addAll(it)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                }, {
                    ll_history.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                    if (offset == 0)
                        finishRefresh()
                    else
                      finishLoadMore()

                    hisAdapter.dataList.clear()
                    hisAdapter.dataList.addAll(Store.store.projectSearch(baseActivity!!))
                    hisAdapter.notifyDataSetChanged()
                    if (Store.store.projectSearch(baseActivity!!).isEmpty()) {
                        last_search_layout.visibility = View.GONE
                    } else {
                        last_search_layout.visibility = View.VISIBLE
                    }

                })
    }


    companion object {
        val TAG = MainProjectFragment::class.java.simpleName
        val mTypeList = ArrayList<Int>()
        val mTypeMap = HashMap<Int, String>()//type和项目类别对应

        fun newInstance(): MainProjectFragment {
            val fragment = MainProjectFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }
}