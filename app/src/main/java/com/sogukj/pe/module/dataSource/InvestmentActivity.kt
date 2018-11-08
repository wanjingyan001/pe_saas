package com.sogukj.pe.module.dataSource

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.core.view.get
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.CategoryChild1
import com.sogukj.pe.bean.InvestCategory
import com.sogukj.pe.bean.InvestmentEvent
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_investment_event.*
import kotlinx.android.synthetic.main.item_invest_primary.view.*
import kotlinx.android.synthetic.main.item_invest_secondary.view.*
import kotlinx.android.synthetic.main.item_investment_event_list.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity

@Route(path = ARouterPath.InvestmentActivity)
class InvestmentActivity : BaseRefreshActivity() {
    private lateinit var resultAdapter: RecyclerAdapter<InvestmentEvent>//查询结果adapter
    private lateinit var primaryAdapter: RecyclerAdapter<InvestCategory>//一级查询条件adapter
    private lateinit var secondaryAdapter: RecyclerAdapter<CategoryChild1>//二级查询条件adapter

    private var page = 1
    private var fIndustryId: Int? = null//投资分类id
    private var fYear: Int? = null//投资年份
    override val menuId: Int
        get() = R.menu.menu_investment_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investment_event)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "融资事件"
        setBack(true)
        resultAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ResultHolder(_adapter.getView(R.layout.item_investment_event_list, parent))
        }
        primaryAdapter = RecyclerAdapter(this) { ap, parent, _ ->
            PrimaryHolder(ap.getView(R.layout.item_invest_primary, parent))
        }
        secondaryAdapter = RecyclerAdapter(this) { ap, parent, _ ->
            SecondaryHolder(ap.getView(R.layout.item_invest_secondary, parent))
        }

        primaryAdapter.onItemClick = { v, position ->
            primaryAdapter.selectedPosition = position
            secondaryAdapter.selectedPosition = -1
            fIndustryId = primaryAdapter.dataList[position].id
            filterConditionTv.setVisible(true)
            filterConditionTv.text = primaryAdapter.dataList[position].category_name
            primaryAdapter.dataList[position].child?.let {
                secondaryAdapter.refreshData(it)
            }
        }
        secondaryAdapter.onItemClick = { v, position ->
            secondaryAdapter.selectedPosition = position
            filterConditionTv.setVisible(true)
            filterConditionTv.text = secondaryAdapter.dataList[position].category_name
            fIndustryId = secondaryAdapter.dataList[position].id
        }

        filterResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
            addItemDecoration(SpaceItemDecoration(dip(10)))
            adapter = resultAdapter
        }
        primaryOption.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = primaryAdapter
        }
        secondaryOption.apply {
            val manager = FlexboxLayoutManager(ctx)
            manager.alignItems = AlignItems.FLEX_START
            layoutManager = manager
            adapter = secondaryAdapter
        }
        getInvestList()
        getInvestCategory()
        initListener()
    }

    private fun initListener() {
        filterCondition.clickWithTrigger {
            drawer.openDrawer(Gravity.START)
            primaryOption[0].performClick()
        }
        resetCondition.clickWithTrigger {
            primaryAdapter.selectedPosition = -1
            secondaryAdapter.selectedPosition = -1
            secondaryAdapter.dataList.clear()
            secondaryAdapter.notifyDataSetChanged()
            fIndustryId = null
            fYear = null
            filterConditionTv.setVisible(false)
        }
        confirm.clickWithTrigger {
            page = 1
            getInvestList()
            drawer.closeDrawers()
        }
        filterConditionTv.clickWithTrigger {
            filterConditionTv.text = ""
            filterConditionTv.setVisible(false)
            page = 1
            fIndustryId = null
            fYear = null
            getInvestList()
        }
    }


    override fun doRefresh() {
        page = 1
        getInvestList()
    }

    override fun doLoadMore() {
        page += 1
        getInvestList()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun initRefreshFooter(): RefreshFooter? = null


    private fun getInvestList() {
        SoguApi.getService(application, DataSourceService::class.java)
                .getInvestList(fIndustryId, fYear, page = page)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (page == 1) {
                                    resultAdapter.refreshData(it)
                                } else {
                                    resultAdapter.dataList.addAll(it)
                                    resultAdapter.notifyDataSetChanged()
                                }
                            }
                            filterResultTv.text = Html.fromHtml(getString(R.string.tv_title_result_search2, (payload.total as Double).toInt()))
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        (page == 1).yes {
                            finishRefresh()
                        }.otherWise {
                            finishLoadMore()
                        }
                    }
                }
    }

    private fun getInvestCategory() {
        SoguApi.getService(application, DataSourceService::class.java)
                .getInvestCategory()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                primaryAdapter.refreshData(it)

                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                startActivity<InvestSearchActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class ResultHolder(itemView: View) : RecyclerHolder<InvestmentEvent>(itemView) {
        override fun setData(view: View, data: InvestmentEvent, position: Int) {
            view.eventName.text = data.invested_sname
            view.eventTime.text = data.invest_time
            view.investmentMoney.text = data.money
            view.industry.text = data.industry_name
            view.investor.text = data.investor.joinToString(" / ")
        }
    }

    inner class PrimaryHolder(itemView: View) : RecyclerHolder<InvestCategory>(itemView) {
        override fun setData(view: View, data: InvestCategory, position: Int) {
            view.primaryOptionTv.isSelected = primaryAdapter.selectedPosition == position
            view.primaryOptionTv.text = data.category_name
        }
    }

    inner class SecondaryHolder(itemView: View) : RecyclerHolder<CategoryChild1>(itemView) {
        override fun setData(view: View, data: CategoryChild1, position: Int) {
            view.secondaryOptionTv.isSelected = secondaryAdapter.selectedPosition == position
            view.secondaryOptionTv.text = data.category_name
        }
    }
}
