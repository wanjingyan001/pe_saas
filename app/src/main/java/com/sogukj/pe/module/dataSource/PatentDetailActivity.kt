package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.PatentItem
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_patent_detail.*
import org.jetbrains.anko.info

class PatentDetailActivity : ToolbarActivity() {
    private val model: PatentViewModel by lazy {
        ViewModelProviders.of(this, PatentModelFactory(this)).get(PatentViewModel::class.java)
    }
    private val item: PatentItem? by extraDelegate(Extras.DATA, null)

    companion object {
        fun start(ctx: Context, bean: PatentItem) {
            val intent = Intent(ctx, PatentDetailActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patent_detail)
        setBack(true)
        title = item?.name
        item?.let {
            getPatentDetail(it)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun getPatentDetail(bean: PatentItem) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getPatentDetail(bean.url)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            model.saveLocalData(bean)
                            payload.payload?.apply {
                                titleTv.text = name
                                numberTv.text = "申请号 ：$number"
                                timeTv.text = "申请日 ：$date"
                                summaryTv.text = "【摘要】：$summary"
                                infoTv.text = "申请人 ：${apply_name}\n" +
                                        "地址 ：${apply_addr}\n" +
                                        "发明(设计人) ：${invent}\n" +
                                        "主分类号 ：${main_cnumber}\n" +
                                        "分类号 ：${branch_cnumber?.joinToString(",")}"
                                val builder = StringBuilder()
                                law?.forEach {
                                    builder.append(it.date).append("  ").append(it.status).append("\n")
                                }
                                legalStatusInfo.text = builder.toString()
                                other?.apply {
                                    otherInfo.text = "主权项 ：$主权项\n" +
                                            "公开号 ：$公开号\n" +
                                            "公开日 ：$公开日\n" +
                                            "专利代理机构 ：$专利代理机构\n" +
                                            "代理人 ：$代理人\n" +
                                            "颁证日 ：$颁证日\n" +
                                            "优先权 ：$优先权\n" +
                                            "国际申请 ：$国际申请\n" +
                                            "国际公布 ：$国际公布\n" +
                                            "进入国家日期 ：$进入国家日期"
                                }
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }
}
