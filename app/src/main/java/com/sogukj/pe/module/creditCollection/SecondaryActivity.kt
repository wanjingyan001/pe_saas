package com.sogukj.pe.module.creditCollection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.SecondaryBean
import com.sogukj.pe.bean.SensitiveInfo
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_secondary.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.find
import kotlin.properties.Delegates

class SecondaryActivity : BaseActivity(), View.OnClickListener {
    var type: Int by Delegates.notNull()
    var id: Int by Delegates.notNull()
    lateinit var listType: String
    lateinit var adapter: RecyclerAdapter<Any>
    lateinit var info: SensitiveInfo


    companion object {
        private val CPWS = "cpws"//执行文书
        private val ZXGG = "zxgg"//执行公告
        private val KTGG = "ktgg"//开庭公告
        private val FYGG = "fygg"//法院公告
        fun start(ctx: Context?, type: Int, info: SensitiveInfo, id: Int) {
            val intent = Intent(ctx, SecondaryActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, info)
            intent.putExtra(Extras.ID, id)
            ctx?.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_back.visibility = View.VISIBLE

        type = intent.getIntExtra(Extras.TYPE, -1)
        info = intent.getSerializableExtra(Extras.DATA) as SensitiveInfo
        id = intent.getIntExtra(Extras.ID, -1)

        adapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            when (this.type) {
                SensitiveInfoActivity.COURTNOTICE -> NoticeHolder(_adapter.getView(R.layout.item_court_notice, parent))
                SensitiveInfoActivity.LOSSCREDIT -> LostHolder(_adapter.getView(R.layout.item_lost, parent))
                else -> RoomtHolder(_adapter.getView(R.layout.item_roomt_court, parent))
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        when (type) {
            SensitiveInfoActivity.COURTNOTICE -> {
                toolbar_title.text = "法庭公告"
                listType = FYGG
                declarationList(listType)
            }
            SensitiveInfoActivity.COURTROOMT -> {
                toolbar_title.text = "开庭公告"
                listType = KTGG
                declarationList(listType)
            }
            SensitiveInfoActivity.REFEREEDOCUMENTS -> {
                toolbar_title.text = "裁判文书"
                listType = CPWS
                declarationList(listType)
            }
            SensitiveInfoActivity.EXECUTIVEBULLETIN -> {
                toolbar_title.text = "执行公告"
                listType = ZXGG
                declarationList(listType)
            }
            SensitiveInfoActivity.LOSSCREDIT -> {
                toolbar_title.text = "法院失信名单"
                info.dishonest?.let {
                    adapter.dataList.addAll(it.item)
                    adapter.notifyDataSetChanged()
                }
            }
            SensitiveInfoActivity.CASEDETEIL -> {
                toolbar_title.text = "案件详情"
                info.crime?.let {
                    // TODO
                    //adapter.dataList.addAll(it.item)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        toolbar_back.setOnClickListener(this)

    }


    private fun declarationList(type: String) {
        SoguApi.getService(application,CreditService::class.java)
                .declarationList(id, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter.dataList.clear()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.toolbar_back -> finish()
        }
    }

    inner class NoticeHolder(itemView: View) : RecyclerHolder<Any>(itemView) {
        val releaseTime = itemView.find<TextView>(R.id.releaseTime)
        val CourtName = itemView.find<TextView>(R.id.CourtName)
        val NoticeType = itemView.find<TextView>(R.id.NoticeType)
        val NoticeContent = itemView.find<TextView>(R.id.NoticeContent)
        override fun setData(view: View, data: Any, position: Int) {
            if (data is SecondaryBean) {
                if (FYGG == listType) {
                    releaseTime.text = data.sortTime
                    CourtName.text = data.court
                    NoticeType.text = data.ggType
                    NoticeContent.text = data.body
                }
            }
            releaseTime.viewTreeObserver.addOnGlobalLayoutListener {
                if (releaseTime.layout.lineCount > 1) {
                    releaseTime.gravity = Gravity.LEFT
                }
            }
            CourtName.viewTreeObserver.addOnGlobalLayoutListener {
                if (CourtName.layout.lineCount > 1) {
                    CourtName.gravity = Gravity.LEFT
                }
            }
            NoticeType.viewTreeObserver.addOnGlobalLayoutListener {
                if (NoticeType.layout.lineCount > 1) {
                    NoticeType.gravity = Gravity.LEFT
                }
            }
            NoticeContent.viewTreeObserver.addOnGlobalLayoutListener {
                if (NoticeContent.layout.lineCount > 1) {
                    NoticeContent.gravity = Gravity.LEFT
                }
            }
        }
    }

    inner class RoomtHolder(itemView: View) : RecyclerHolder<Any>(itemView) {
        val courtTime = itemView.find<TextView>(R.id.courtTime)
        val plaintiff = itemView.find<TextView>(R.id.plaintiff)
        val courtName = itemView.find<TextView>(R.id.courtName)
        val abstractTv = itemView.find<TextView>(R.id.abstractTv)
        val tv1 = itemView.find<TextView>(R.id.tv1)
        val tv2 = itemView.find<TextView>(R.id.tv2)
        val tv3 = itemView.find<TextView>(R.id.tv3)
        val tv4 = itemView.find<TextView>(R.id.tv4)
        override fun setData(view: View, data: Any, position: Int) {
            when (type) {
                SensitiveInfoActivity.CASEDETEIL -> {

                }
                else -> {
                    if (data is SecondaryBean)
                        when (listType) {
                            KTGG -> {
                                tv1.text = "开庭时间"
                                tv2.text = "原告"
                                tv3.text = "法院名称"
                                tv4.text = "内容概要"
                                courtTime.text = if(data.sortTime.isNullOrEmpty()) "无" else data.sortTime
                                plaintiff.text = if(data.plaintiff.isNullOrEmpty()) "无" else data.plaintiff
                                courtName.text = if(data.court.isNullOrEmpty()) "无" else data.court
                                abstractTv.text = if(data.body.isNullOrEmpty()) "无" else data.body
                            }
                            CPWS -> {
                                tv1.text = "标题"
                                tv2.text = "审结时间"
                                tv3.text = "案由"
                                tv4.text = "判决结果"
                                courtTime.text = if(data.title.isNullOrEmpty()) "无" else data.title
                                plaintiff.text = if(data.sortTime.isNullOrEmpty()) "无" else data.sortTime
                                courtName.text = if(data.caseCause.isNullOrEmpty()) "无" else data.caseCause
                                abstractTv.text = if(data.judgeResult.isNullOrEmpty()) "无" else data.judgeResult
                            }
                            ZXGG -> {
                                tv1.text = "执行时间"
                                tv2.text = "申请人"
                                tv3.text = "案件状态"
                                tv4.text = "内容概要"
                                courtTime.text = if(data.sortTime.isNullOrEmpty()) "无" else data.sortTime
                                plaintiff.text = if(data.proposer.isNullOrEmpty()) "无" else data.proposer
                                courtName.text = if(data.caseState.isNullOrEmpty()) "无" else data.caseState
                                abstractTv.text = if(data.body.isNullOrEmpty()) "无" else data.body
                            }
                        }
                }
            }
            courtTime.viewTreeObserver.addOnGlobalLayoutListener {
                if (courtTime.layout.lineCount > 1) {
                    courtTime.gravity = Gravity.LEFT
                }
            }
            plaintiff.viewTreeObserver.addOnGlobalLayoutListener {
                if (plaintiff.layout.lineCount > 1) {
                    plaintiff.gravity = Gravity.LEFT
                }
            }
            courtName.viewTreeObserver.addOnGlobalLayoutListener {
                if (courtName.layout.lineCount > 1) {
                    courtName.gravity = Gravity.LEFT
                }
            }
            abstractTv.viewTreeObserver.addOnGlobalLayoutListener {
                if (abstractTv.layout.lineCount > 1) {
                    abstractTv.gravity = Gravity.LEFT
                }
            }
        }
    }

    inner class LostHolder(itemView: View) : RecyclerHolder<Any>(itemView) {
        val filingTime = itemView.find<TextView>(R.id.filingTime)
        val caseNumber = itemView.find<TextView>(R.id.caseNumber)
        val executionCourt = itemView.find<TextView>(R.id.executionCourt)
        val fulfillState = itemView.find<TextView>(R.id.fulfillState)
        val executionText = itemView.find<TextView>(R.id.executionText)
        override fun setData(view: View, data: Any, position: Int) {
            if (data is SensitiveInfo.Dishonest.Item) {
                filingTime.text = data.time
                caseNumber.text = data.casenum
                executionCourt.text = data.court
                fulfillState.text = data.performance
                executionText.text = data.base
            }
            filingTime.viewTreeObserver.addOnGlobalLayoutListener {
                if (filingTime.layout.lineCount > 1) {
                    filingTime.gravity = Gravity.LEFT
                }
            }
            caseNumber.viewTreeObserver.addOnGlobalLayoutListener {
                if (caseNumber.layout.lineCount > 1) {
                    caseNumber.gravity = Gravity.LEFT
                }
            }
            executionCourt.viewTreeObserver.addOnGlobalLayoutListener {
                if (executionCourt.layout.lineCount > 1) {
                    executionCourt.gravity = Gravity.LEFT
                }
            }
            fulfillState.viewTreeObserver.addOnGlobalLayoutListener {
                if (fulfillState.layout.lineCount > 1) {
                    fulfillState.gravity = Gravity.LEFT
                }
            }
            executionText.viewTreeObserver.addOnGlobalLayoutListener {
                if (executionText.layout.lineCount > 1) {
                    executionText.gravity = Gravity.LEFT
                }
            }
        }
    }


}
