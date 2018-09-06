package com.sogukj.pe.module.lpassistant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PolicyBannerInfo
import com.sogukj.pe.module.lpassistant.adapter.AutoScrollAdapter
import com.sogukj.pe.module.lpassistant.adapter.PolicyExpressListAdapter
import com.sogukj.pe.module.lpassistant.autoscroll.AutoScrollViewPager
import com.sogukj.pe.module.lpassistant.presenter.PolocyExpressPresenter
import kotlinx.android.synthetic.main.activity_policy_express.*
import kotlinx.android.synthetic.main.pex_toolbar.*

/**
 * Created by CH-ZH on 2018/9/5.
 */
class PolicyExpressActivity : BaseRefreshActivity(),PolicyExpressCallBack {
    private var headView : View? = null
    private var scroll_viewpager : AutoScrollViewPager ? = null
    private var tv_title : TextView? = null
    private var banner_ll_indicator : LinearLayout ? = null
    private var presenter : PolocyExpressPresenter ? = null
    private var plAdapter : PolicyExpressListAdapter ? = null
    private var bannerInfos = ArrayList<PolicyBannerInfo.BannerInfo>()
    private var indicators = arrayOfNulls<ImageView>(1)     //轮播图指示器
    private var mScrollAdapter : AutoScrollAdapter ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy_express)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "政策速递"
        setBack(true)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        headView = View.inflate(this,R.layout.header_policy_express,null)
        scroll_viewpager = headView!!.findViewById(R.id.scroll_viewpager)
        banner_ll_indicator = headView!!.findViewById(R.id.banner_ll_indicator)
        tv_title = headView!!.findViewById(R.id.tv_title)
        presenter = PolocyExpressPresenter(this,this)
        headView!!.visibility = View.GONE
        lv_express.addHeaderView(headView)

        plAdapter = PolicyExpressListAdapter(this)
        mScrollAdapter = AutoScrollAdapter(this,null)
        lv_express.adapter = plAdapter

        initTopBanner()
    }

    private fun initTopBanner() {
        scroll_viewpager!!.startAutoScroll(3000)
        scroll_viewpager!!.setInterval(3000)
        scroll_viewpager!!.setCycle(true)
    }

    override fun onStart() {
        super.onStart()
        if (null != scroll_viewpager){
            scroll_viewpager!!.startAutoScroll()
        }
    }

    override fun onPause() {
        super.onPause()
        if (null != scroll_viewpager){
            scroll_viewpager!!.stopAutoScroll()
        }
    }

    private fun initData() {
        getBannerData()
        getListInfoData()
    }

    private fun getListInfoData() {
        if (null != presenter){
            presenter!!.doListInfoRequest(true)
        }
    }

    private fun getBannerData() {
        if (null != presenter){
            presenter!!.doBannerRequest()
        }
    }

    override fun setBannerInfo(bannerInfo: PolicyBannerInfo) {
        if (null != bannerInfo && null != bannerInfo.data && bannerInfo.data!!.size > 0){
            this.bannerInfos = bannerInfo.data!!
            initBannerFooter()
            setBannerAdapter()
        }else{
            headView!!.visibility = View.GONE
        }

        plAdapter!!.notifyDataSetChanged()
    }

    private fun setBannerAdapter() {
        mScrollAdapter!!.replaceData(bannerInfos)
        scroll_viewpager!!.adapter = mScrollAdapter
        mScrollAdapter!!.setClickItemListener { item, position ->
            //banner 跳转
        }
    }

    private fun initBannerFooter() {
        initBannerIndicator()
        setBannerIndicator(0)
    }

    private fun setBannerIndicator(selectedPosition: Int) {
        var i = 0
        while (indicators.size > 0 && i < indicators.size) {
            val indicator = indicators[i]
            if (indicator != null) {
                if (i == selectedPosition) {
                    indicator.setBackgroundResource(R.drawable.banner_indication_focus)
                } else {
                    indicator.setBackgroundResource(R.drawable.banner_indication_unfocus)
                }
            }
            i++
        }
    }

    private fun initBannerIndicator() {
        val ivSize = bannerInfos.size
        //设置指示器
        indicators = arrayOfNulls<ImageView>(ivSize)
        banner_ll_indicator!!.removeAllViews()
        //添加dot
        var params: LinearLayout.LayoutParams? = null
        var dot: ImageView? = null

        if (indicators!!.size > 1) {
            for (i in 0 until indicators!!.size) {
                dot = ImageView(this)
                params = LinearLayout.LayoutParams(Utils.dip2px(this, 5f), Utils.dip2px(this, 5f))
                params!!.leftMargin = 20
                indicators!![i] = dot
                dot!!.setLayoutParams(params)
                banner_ll_indicator!!.addView(dot)
            }
        }
    }

    private fun bindListener() {
        iv_search.setOnClickListener {
                //搜索
        }

        iv_fillter.setOnClickListener {
                //过滤
            drawer.openDrawer(Gravity.START)
        }
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {
        if (null != presenter){
            presenter!!.doRequest()
        }
    }

    override fun doLoadMore() {
        if (null != presenter){
            presenter!!.doListInfoRequest(false)
        }
    }

    companion object {
        val TAG = PolicyExpressActivity.javaClass.simpleName
        fun invoke(context: Context){
            var intent = Intent(context, PolicyExpressActivity::class.java)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}