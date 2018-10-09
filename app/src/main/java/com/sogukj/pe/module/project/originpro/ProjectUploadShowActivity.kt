package com.sogukj.pe.module.project.originpro

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.project.originpro.adapter.ExpandableItemAdapter
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_upload_show.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Created by CH-ZH on 2018/9/20.
 */
class ProjectUploadShowActivity : ToolbarActivity(){
    private  var adapter : ExpandableItemAdapter ?  = null
    private lateinit var fundAdapter : RecyclerAdapter<LinkFundBean>
    private var project : ProjectBean ? = null
    private var floor : Int ?= null
    private var user : UserBean ? = null
    private var approveDatas = ArrayList<ProjectApproveInfo>()
    private var linkFundDatas = ArrayList<LinkFundBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_show)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("信息填写")

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean?
        floor = intent.getIntExtra(Extras.FLAG,-1)
        user = Store.store.getUser(this)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
    }

    private fun initData() {
        rv.layoutManager = LinearLayoutManager(this)

        fundAdapter = RecyclerAdapter(this){adapter,parent,_ ->
            ProjectFundHolder(adapter.getView(R.layout.item_link_fund,parent))
        }

        rv_fund.layoutManager = LinearLayoutManager(this)
        rv_fund.adapter = fundAdapter
        if (null != project){
            setLoadding()
            getProjectComBase()
            getFundData()
            getApproveShowData()
        }
    }

    private fun getApproveShowData() {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getProApproveInfo(project!!.company_id!!,floor!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val datas = payload.payload
                            if (null != datas && datas.size > 0){
                                val list = datas[0]
                                if (null != list && list.size > 0){
                                    approveDatas = list as ArrayList<ProjectApproveInfo>
                                    setMultiItemData(list)
                                }

                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                        goneLoadding()
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        goneLoadding()
                    }
                }
    }

    private fun setMultiItemData(datas: List<ProjectApproveInfo>) {
        val res = ArrayList<MultiItemEntity>()
        for (data in datas){
            val level0Item = Level0Item(data.name)
            if (null != data.son && data.son!!.size > 0){
                for (son in data.son!!){
                    val level1Item = Level1Item(son.name)
                    if (null != son.files && son.files!!.size > 0){
                        for (file in son.files!!){
                            val level2Item = Level2Item()
                            level2Item.type = 0
                            level2Item.file = file
                            level1Item.addSubItem(level2Item)
                        }
                    }else{
                        val level2Item = Level2Item()
                        level2Item.type = -1
                        level1Item.addSubItem(level2Item)
                    }


                    level0Item.addSubItem(level1Item)
                }
            }
            res.add(level0Item)
        }
        adapter = ExpandableItemAdapter(res,1,this)
        rv.adapter = adapter
        adapter!!.expandAll()
    }

    private fun getProjectComBase() {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getProBuildInfo(project!!.company_id!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val projectInfo = payload.payload
                            if (null != projectInfo){
                                setEditStatus(projectInfo)
                            }
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                    }
                    onComplete {

                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                    }
                }
    }

    private fun setEditStatus(projectInfo: NewProjectInfo) {
        var pm = 0
        var pl = 0
        if (null != projectInfo.duty){
            pm = projectInfo.duty!!.principal!!
        }
        if (null != projectInfo.lead){
            pl = projectInfo.lead!!.leader!!
        }

        if (user!!.uid == pm || user!!.uid == pl){
            //可编辑
            tv_edit.visibility = View.VISIBLE
            tv_edit.text = "编辑"
        }else{
            //不可编辑
            tv_edit.visibility = View.GONE
        }
    }

    private fun getFundData() {
      if (null == project) return
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getLinkFund(project!!.company_id!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val fundInfos = payload.payload
                            if (null != fundInfos && fundInfos.size > 0){
                                linkFundDatas = fundInfos as ArrayList<LinkFundBean>
                                fundAdapter.dataList.clear()
                                fundAdapter.dataList.addAll(fundInfos)
                                fundAdapter.notifyDataSetChanged()
                            }
                        }else{
                            showErrorToast(payload.message)
                        }

                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun bindListener() {
        if (null != adapter){
            adapter!!.setOnItemChildClickListener { adapter, view, position ->
//                adapter.remove(position)
            }
        }

        tv_edit.setOnClickListener {
            //编辑
            startActivity<ProjectUploadActivity>(Extras.DATA to approveDatas,Extras.FUND to linkFundDatas
                    ,Extras.PROJECT to project,Extras.FLAG to floor)
        }
    }


    inner class ProjectFundHolder(itemView: View) : RecyclerHolder<LinkFundBean>(itemView) {
        val tv_invest = itemView.findViewById<TextView>(R.id.tv_invest)
        val tv_amount_name = itemView.findViewById<TextView>(R.id.tv_amount_name)
        val tv_stock_ratio = itemView.findViewById<TextView>(R.id.tv_stock_ratio)
        override fun setData(view: View, data: LinkFundBean, position: Int) {
             if (null == data) return
            tv_invest.text = data.fundName
            tv_amount_name.text = data.had_invest
            tv_stock_ratio.text = data.proportion
        }

    }
    private fun setLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }
}


