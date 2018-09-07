package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.amap.api.mapcore.util.it
import com.chad.library.adapter.base.BaseQuickAdapter
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.R.layout.header
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Rom
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PatentItem
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_patent_data.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class PatentDataActivity : ToolbarActivity() {
    private val model: PatentViewModel by lazy {
        ViewModelProviders.of(this, PatentModelFactory(this)).get(PatentViewModel::class.java)
    }
    private lateinit var listAdapter: PatentAdapter
    private val data = mutableListOf<PatentItem>()
    private var page = 1
    private var searchkey: String? = null

    private var NAVIGATION_GESTURE: String = when {
        Rom.isEmui() -> "navigationbar_is_min"
        Rom.isMiui() -> "force_fsg_nav_bar"
        else -> "navigation_gesture_on"
    }

    /**
     * 判断设备是否存在NavigationBar
     *
     * @return true 存在, false 不存在
     */
    @SuppressLint("PrivateApi")
    private fun deviceHasNavigationBar() {
        var haveNav = false
        try {
            //1.通过WindowManagerGlobal获取windowManagerService
            // 反射方法：IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val getWmServiceMethod = windowManagerGlobalClass.getDeclaredMethod("getWindowManagerService")
            getWmServiceMethod.isAccessible = true
            //getWindowManagerService是静态方法，所以invoke null
            val iWindowManager = getWmServiceMethod.invoke(null)

            //2.获取windowMangerService的hasNavigationBar方法返回值
            // 反射方法：haveNav = windowManagerService.hasNavigationBar();
            val iWindowManagerClass = iWindowManager.javaClass
            val hasNavBarMethod = iWindowManagerClass.getDeclaredMethod("hasNavigationBar")
            hasNavBarMethod.isAccessible = true
            haveNav = hasNavBarMethod.invoke(iWindowManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (haveNav && !navigationGestureEnabled()) {
            val param1 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            param1.bottomMargin = Utils.dpToPx(ctx, 50)
            //var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            rootLayout.layoutParams = param1
        }
    }

    private fun navigationGestureEnabled(): Boolean {
        return Settings.Global.getInt(contentResolver, NAVIGATION_GESTURE, 0) != 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patent_data)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        toolbar?.setBackgroundColor(resources.getColor(R.color.transparent))
        deviceHasNavigationBar()
        searchkey = intent.getStringExtra(Extras.DATA)
        listAdapter = PatentAdapter(data)
        patentList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = listAdapter
        }
        listAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            PatentDetailActivity.start(this, data[position])
        }
        initRefreshConfig()
        initSearch()
        if (searchkey.isNullOrEmpty()) {
            val list = model.getPatentHistory().reversed()
            (list.size > 5).yes {
                data.addAll(list.toList().subList(0, 5))
            }.otherWise {
                data.addAll(list)
            }
            listAdapter.notifyDataSetChanged()
        } else {
            searchEdt.setText(searchkey)
        }
    }

    private fun initSearch() {
        searchEdt.textChangedListener {
            afterTextChanged {
                searchEdt.textStr.isNotEmpty().yes {
                    clear.setVisible(true)
                    getPatentList(searchEdt.textStr)
                }.otherWise {
                    headerLayout.setVisible(true)
                    clear.setVisible(false)
                }
            }
        }
        clear.clickWithTrigger {
            searchEdt.setText("")
        }
        back.clickWithTrigger {
            finish()
        }
    }


    fun initRefreshConfig() {
        refresh.apply {
            isEnableLoadMore = true
            isEnableRefresh = true
            isEnableAutoLoadMore = true
            setRefreshHeader(ClassicsHeader(ctx))
            setOnRefreshListener {
                page = 1
                getPatentList(searchkey)
            }
            setOnLoadMoreListener {
                page += 1
                getPatentList(searchkey)
            }
        }
    }

    private fun getPatentList(searchKey: String? = null) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getPatentList(page, searchKey)
                .execute {
                    onSubscribe {
                        showProgress("正在请求数据")
                    }
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                listAdapter.searchKey = searchKey
                                if (page == 1) {
                                    data.clear()
                                }
                                data.addAll(it)
                                listAdapter.notifyDataSetChanged()
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        hideProgress()
                        if (page == 1) {
                            refresh.finishRefresh()
                        }else{
                            refresh.finishLoadMore()
                        }
                        data.isNotEmpty().yes {
                            refresh.setVisible(true)
                            empty_img.setVisible(false)
                            headerLayout.setVisible(false)
                        }.otherWise {
                            refresh.setVisible(false)
                            empty_img.setVisible(true)
                            headerLayout.setVisible(true)
                        }
                    }
                    onError {
                        hideProgress()
                        refresh.setVisible(false)
                        empty_img.setVisible(true)
                        headerLayout.setVisible(true)
                    }
                }
    }


}
