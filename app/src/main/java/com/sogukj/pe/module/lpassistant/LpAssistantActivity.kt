package com.sogukj.pe.module.lpassistant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Consts
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.LpAssisBean
import com.sogukj.pe.module.dataSource.*
import com.sogukj.pe.module.dataSource.lawcase.LowCaseHisActivity
import com.sogukj.pe.peExtended.RootPath
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_la_assistant.*
import kotlinx.android.synthetic.main.commom_title.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/9/5.
 * 尽调助手
 */
@Route(path = ARouterPath.LpAssistantActivity)
class LpAssistantActivity : BaseActivity() {

    lateinit var lpaAdapter: RecyclerAdapter<LpAssisBean>
    private var infos = ArrayList<LpAssisBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_la_assistant)
        Utils.setWindowStatusBarColor(this, R.color.white)
        RetrofitUrlManager.getInstance().putDomain(Consts.DATA_SOURCE, RootPath)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        refresh.setOnRefreshListener {
            refresh.finishRefresh(1000)
        }
        refresh.isEnableAutoLoadMore = false
    }

    private fun initData() {
        toolbar_title.text = resources.getString(R.string.pl_assis)
        initAdapter()
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@LpAssistantActivity)
            adapter = lpaAdapter
        }
        initAssistantData()
    }

    private fun initAssistantData() {
        val lp1 = LpAssisBean()
        lp1.resId = R.drawable.ic_patent_query
        lp1.name = "专利查询"
        val lp2 = LpAssisBean()
        lp2.resId = R.drawable.ic_law_nrule
        lp2.name = "法务查询"
        val lp3 = LpAssisBean()
        lp3.resId = R.drawable.ic_hot_post
        lp3.name = "行业研报"
        val lp4 = LpAssisBean()
        lp4.resId = R.drawable.ic_prospectus
        lp4.name = "招股书"
        val lp5 = LpAssisBean()
        lp5.resId = R.drawable.ic_recent_his
        lp5.name = "融资事件"
        val lp6 = LpAssisBean()
        lp6.resId = R.drawable.ic_policy_express
        lp6.name = "政策速递"
        val lp7 = LpAssisBean()
        lp7.resId = R.drawable.ic_invest_event
        lp7.name = "投资事件"
        infos.add(lp1)
        infos.add(lp2)
        infos.add(lp3)
        infos.add(lp4)
        infos.add(lp5)
        infos.add(lp6)
//        infos.add(lp7)
        lpaAdapter.dataList.addAll(infos)
        lpaAdapter.notifyDataSetChanged()
    }

    private fun initAdapter() {
        lpaAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            val itemView = _adapter.getView(R.layout.item_lp_assis, parent)
            object : RecyclerHolder<LpAssisBean>(itemView) {
                val icon = itemView.find<ImageView>(R.id.iv_assis)
                val name = itemView.find<TextView>(R.id.tv_name)
                override fun setData(view: View, data: LpAssisBean, position: Int) {
                    if (null != data.resId) {
                        icon.setImageResource(data.resId!!)
                    }
                    name.text = data.name
                }
            }
        }
    }

    private fun bindListener() {
        lpaAdapter.onItemClick = { v: View, position: Int ->
            when (position) {
                0 -> {
                    if (sp.getBoolean(Extras.IS_FIRST_PATENT, true)) {
                        startActivity<PatentSearchActivity>(Extras.DATA to 0)
                    } else {
                        startActivity<PatentDataActivity>()
                    }
                }
                1 -> {
                    //法律助手
                    if (!XmlDb.open(this).get(Extras.IS_FIRST_LAW)) {
                        startActivity<PatentSearchActivity>(Extras.DATA to 1)
                    } else {
                        startActivity<LowCaseHisActivity>()
                    }
                }

                2 -> {
                    //热门行业研报
                    isHasSelectedIndustry { is_select ->
                        when (is_select) {
                            0 -> startActivity<IndustrySubActivity>(Extras.TYPE to is_select)
                            1 -> startActivity<DocumentsListActivity>(
                                    Extras.TYPE to DocumentType.INDUSTRY_REPORTS)
                        }
                    }
                }
                3 -> {
                    //招股书
                    DocumentsListActivity.start(this, DocumentType.EQUITY)
                }
                4 -> {
                    //行业近期投融资历史
                    startActivity<InvestmentActivity>()
                }
                5 -> {
                    //政策速递
                    PolicyExpressActivity.invoke(this)
                }
            }
        }

        toolbar_back.clickWithTrigger {
            onBackPressed()
        }
    }


    private fun isHasSelectedIndustry(block: (is_select: Int) -> Unit) {
        SoguApi.getService(application, DataSourceService::class.java)
                .isFirstCome()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                it as LinkedTreeMap<*, *>
                                val flag = it["is_select"] as Double
                                block.invoke(flag.toInt())
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    companion object {
        fun invoke(context: Context) {
            var intent = Intent(context, LpAssistantActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}