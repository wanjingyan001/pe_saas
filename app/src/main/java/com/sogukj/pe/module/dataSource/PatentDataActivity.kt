package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.FrameLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Rom
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.PatentItem
import kotlinx.android.synthetic.main.activity_patent_data.*
import kotlinx.android.synthetic.main.item_patent_history.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.startActivity

class PatentDataActivity : ToolbarActivity() {
    private val model: PatentViewModel by lazy {
        ViewModelProviders.of(this, PatentModelFactory(this)).get(PatentViewModel::class.java)
    }
    private lateinit var listAdapter: RecyclerAdapter<PatentItem>

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
        listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            PatentHisHolder(_adapter.getView(R.layout.item_patent_history, parent))
        }
        patentList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = listAdapter
        }
        listAdapter.onItemClick = { view, position ->
            PatentDetailActivity.start(this, listAdapter.dataList[position])
        }
        initRefreshConfig()

        listAdapter.refreshData(model.getPatentHistory().toList().reversed())


        searchEdt.textChangedListener {
            afterTextChanged {
                searchEdt.textStr.isNotEmpty().yes {
                    Utils.closeInput(ctx, searchEdt)
                    startActivity<PatentListActivity>(Extras.CODE to searchEdt.textStr)
                }
            }
        }
        clearHistory.clickWithTrigger {
            model.clearHistory()
            listAdapter.dataList.clear()
            listAdapter.notifyDataSetChanged()
        }
    }


    fun initRefreshConfig() {
        refresh.apply {
            isEnableLoadMore = false
            isEnableRefresh = false
            isEnableAutoLoadMore = false
        }
    }

    inner class PatentHisHolder(itemView: View) : RecyclerHolder<PatentItem>(itemView) {
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: PatentItem, position: Int) {
            view.num.text = "${position + 1}"
            view.patentName.text = data.name
            view.applicantName.text = data.author
            view.timeTv.text = data.date
        }
    }
}
