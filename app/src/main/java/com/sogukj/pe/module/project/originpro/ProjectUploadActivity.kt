package com.sogukj.pe.module.project.originpro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.project.originpro.adapter.ExpandableItemAdapter
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_project_upload.*
import kotlinx.android.synthetic.main.layout_link_fund.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectUploadActivity : ToolbarActivity() {
    private var list = ArrayList<MultiItemEntity>()
    private lateinit var adapter : ExpandableItemAdapter
    lateinit var fundAdapter: RecyclerAdapter<LinkFundBean>
    val REQ_SELECT_FILE = 0x2018
    val SELECT_FUND = 0x110
    private var addPosition = 0
    private val fundMap = HashMap<Int,Int>()
    private val funNameMap = HashMap<Int,TextView>()
    private val amountMap = HashMap<Int,EditText>()
    private val ratioMap = HashMap<Int,EditText>()
    private var approveDatas: List<ProjectApproveInfo> ? = null
    private var linkFundDatas : List<LinkFundBean> ? = null
    private var project : ProjectBean ? = null
    private var selectPos = 0
    private var uploadFiles = ArrayList<FileDataBean>()
    private var class_id = 0
    private var floor = 0
    private var title = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_upload)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        approveDatas = intent.getSerializableExtra(Extras.DATA) as List<ProjectApproveInfo>
        linkFundDatas = intent.getSerializableExtra(Extras.FUND) as List<LinkFundBean>
        project = intent.getSerializableExtra(Extras.PROJECT) as ProjectBean
        floor = intent.getIntExtra(Extras.FLAG,-1)
        title = intent.getStringExtra(Extras.TITLE)
        setTitle(title)
        if ("签约付款".equals(title)){
            link_fund.visibility = View.VISIBLE
        }else{
            link_fund.visibility = View.GONE
        }
    }

    private fun initData() {
        list = getLocalData()
        adapter = ExpandableItemAdapter(list,0,this)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        adapter.expandAll()

        fundAdapter = RecyclerAdapter<LinkFundBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.add_fund_item, parent)
            val rl_invest_subject = convertView.findViewById<RelativeLayout>(R.id.rl_invest_subject)
            val tv_invest = convertView.findViewById<TextView>(R.id.tv_invest)
            val et_amount_name = convertView.findViewById<EditText>(R.id.et_amount_name)
            val et_stock_ratio = convertView.findViewById<EditText>(R.id.et_stock_ratio)
            object : RecyclerHolder<LinkFundBean>(convertView) {
                override fun setData(view: View, data: LinkFundBean, position: Int) {
                    if (null == data) return
                    rl_invest_subject.setOnClickListener {
                        //选择投资主体
                        selectPos = position
                        SelectFundActivity.start(this@ProjectUploadActivity,SELECT_FUND)
                    }
                    if (!data.fundName.isNullOrEmpty()){
                        tv_invest.text = data.fundName
                    }
                    if (!data.had_invest.isNullOrEmpty()){
                        et_amount_name.setText(data.had_invest)
                    }
                    if (!data.proportion.isNullOrEmpty()){
                        et_stock_ratio.setText(data.proportion)
                    }
                    funNameMap.put(position,tv_invest)
                    fundMap.put(position,data.fund_id)
                    amountMap.put(position,et_amount_name)
                    ratioMap.put(position,et_stock_ratio)
                }

            }

        })

        rv_add_fund.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                Utils.dip2px(this, 8f), Color.parseColor("#f7f9fc")))
        rv_add_fund.layoutManager = LinearLayoutManager(this)
        fundAdapter.dataList.addAll(linkFundDatas!!)
        rv_add_fund.adapter = fundAdapter
    }

    private fun bindListener() {
        tv_add_fund.setOnClickListener {
            //添加关联基金
            val dataList = fundAdapter.dataList
            dataList.add(LinkFundBean())
            fundAdapter.dataList = dataList
            fundAdapter.notifyDataSetChanged()
        }

        adapter.setOnItemChildClickListener { _adapter, view, position ->
            val data = _adapter.data
            val entity = data[position]
            when(view.id){
                R.id.iv_delete,
                R.id.cons_item -> {
                    Log.e("TAG","deletePosition ==" + position)
                    //移除文件
                    if (entity is Level2Item){
                        val level2Item = entity
                        if (null != level2Item.localFile){
                            removeDataFroOss(level2Item.file!!,position)
                        }else{
                            removeDataFromNet(level2Item.file!!,position)
                        }
                    }
                }
                R.id.tv_add_file -> {
                    //添加文件
                    addPosition = position
                    Log.e("TAG","addPosition ===" + addPosition)
                    FileMainActivity.start(context, 1,requestCode = REQ_SELECT_FILE)
                    if (entity is Level2Item){
                        class_id = entity.class_id
                    }

                }
            }

        }

        ll_create.clickWithTrigger {
            if (null == project) return@clickWithTrigger
            modifyYshData()
        }
    }

    private fun modifyYshData() {
        showProgress("正在提交")
        if ("签约付款".equals(title)){
            uploadLinkFund()
        }else{
            if ("投后管理".equals(title)){
                uploadInvestFiles(2,1)
            }else{
                uploadInvestFiles(2,0)
            }

        }

    }

    override fun onBackPressed() {
        editSaveDialog()
    }

    private fun editSaveDialog() {
        var mDialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .canceledOnTouchOutside(true)
                .customView(R.layout.dialog_yongyin, false).build()
        val content = mDialog.find<TextView>(R.id.content)
        val cancel = mDialog.find<Button>(R.id.cancel)
        val yes = mDialog.find<Button>(R.id.yes)
        content.text = "是否需要保存草稿"
        cancel.text = "否"
        yes.text = "是"
        cancel.setOnClickListener {
            mDialog.dismiss()
            super.onBackPressed()
        }
        yes.setOnClickListener {
            if ("投后管理".equals(title)){
                uploadInvestFiles(1,1)
            }else{
                uploadInvestFiles(1,0)
            }

            mDialog.dismiss()
        }

        mDialog.show()
    }

    private fun uploadInvestFiles(type : Int,current : Int) {
        val map = HashMap<String, Any>()
        map.put("company_id", project!!.company_id!!)
        map.put("type", type)
        map.put("floor", project!!.floor!!)
        map.put("current", current)
        map.put("files", uploadFiles)
        map.put("iconfloor",floor)
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .createProjectApprove(map)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (type == 2){
                                showSuccessToast("提交成功")
                                if ("预审会".equals(title)){
                                    startActivity<ProjectUploadShowActivity>(Extras.DATA to project,Extras.FLAG to floor)
                                }else{
                                    startActivity<OtherProjectShowActivity>(Extras.DATA to project,Extras.FLAG to floor,Extras.TITLE to title)
                                }
                                finish()
                            }else{
                                showSuccessToast("保存成功")
                                super.onBackPressed()
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                        hideProgress()
                    }
                    onError {
                        it.printStackTrace()
                        showErrorToast("提交失败")
                        hideProgress()
                    }
                }
    }

    private fun uploadLinkFund() {
        val dataList = fundAdapter.dataList
        val fundInfos = ArrayList<UploadFundBean>()
        if (null != dataList && dataList.size > 0){
            for (i in dataList.indices){
                val fund = UploadFundBean()
                fund.id = dataList[i].id
                fund.fund_id = fundMap[i]!!
                fund.had_invest = amountMap[i]!!.textStr
                fund.proportion = ratioMap[i]!!.textStr
                if (null != fund.id || !fund.had_invest.isNullOrEmpty() || !fund.proportion.isNullOrEmpty()){
                    fundInfos.add(fund)
                }
            }
        }
        val map = HashMap<String,Any>()
        map.put("company_id",project!!.company_id!!)
        map.put("data",fundInfos)
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .addLinkFund(map)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if ("投后管理".equals(title)){
                                uploadInvestFiles(2,1)
                            }else{
                                uploadInvestFiles(2,0)
                            }

                        }else{
                            showErrorToast(payload.message)
                            hideProgress()
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("添加基金信息失败")
                        hideProgress()
                    }
                }

    }

    private fun removeDataFromNet(entity: ProjectApproveInfo.ApproveFile, position: Int) {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .deleteProjectFile(entity.file_id!!)
                .execute {
                    onNext { payload ->
                        if(payload.isOk){
                            adapter.remove(position)
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("文件删除失败")
                    }
                }
    }

    private fun removeDataFroOss(entity: ProjectApproveInfo.ApproveFile, position: Int) {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .deleteProjectOssFile(entity.filePath)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            adapter.remove(position)
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("文件删除失败")
                    }
                }
    }

    private fun getLocalData(): ArrayList<MultiItemEntity> {
        val res = ArrayList<MultiItemEntity>()
        for (data in approveDatas!!){
            val level0Item = Level0Item(data.name)
            if (null != data.son && data.son!!.size > 0){
                for (son in data.son!!){
                    val level1Item = Level1Item(son.name)
                    if (null != son.files && son.files!!.size > 0){
                        for (file in son.files!!){
                            val level2Item = Level2Item()
                            level2Item.type = 0
                            level2Item.file = file
                            level2Item.class_id = son.class_id!!
                            level1Item.addSubItem(level2Item)
                        }
                        val addItem = Level2Item()
                        addItem.type = -1
                        addItem.class_id = son.class_id!!
                        level1Item.addSubItem(addItem)
                    }else{
                        val level2Item = Level2Item()
                        level2Item.type = -1
                        level2Item.class_id = son.class_id!!
                        level1Item.addSubItem(level2Item)
                    }
                    level0Item.addSubItem(level1Item)
                }
            }else{
                if (null != data.files && data.files!!.size > 0){
                    //有文件
                    for (file in data.files!!){
                        val level2Item = Level2Item()
                        level2Item.type = 0
                        level2Item.file = file
                        level2Item.class_id = data.class_id!!
                        level0Item.addSubItem(level2Item)
                    }
                    val addItem = Level2Item()
                    addItem.type = -1
                    addItem.class_id = data.class_id!!
                    level0Item.addSubItem(addItem)
                }else{
                    //无文件
                    val level2Item = Level2Item()
                    level2Item.type = -1
                    level2Item.class_id = data.class_id!!
                    level0Item.addSubItem(level2Item)
                }
            }
            res.add(level0Item)
        }
        return res
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data && resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQ_SELECT_FILE -> {
                    val paths = data?.getStringArrayListExtra(Extras.LIST)
                    val filePath = paths[0]
                    val file = File(filePath)
                    if (null == file) return
                    uploadFileToOss(file)
                }
                SELECT_FUND -> {
                    val info = data.getSerializableExtra(Extras.DATA) as InvestFundBean
                    if (null == info) return
                    val dataList = fundAdapter.dataList
                    if (null != dataList && dataList.size > 0){
                        val linkFundBean = dataList[selectPos]
                        if (null != linkFundBean){
                            linkFundBean.fundName = info.name
                            linkFundBean.fund_id = info.id
                        }
                    }
                    fundAdapter.notifyDataSetChanged()
                }
            }

        }
    }

    private fun uploadFileToOss(file: File) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("type",1.toString()) //1 项目文件 2审批文件
        val body = builder.build()
        showProgress("正在上传")
        SoguApi.getService(App.INSTANCE, OtherService::class.java)
                .uploadProFile(body)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val fileBean = payload.payload
                            if (null != adapter){
                                val level2Item = Level2Item()
                                val approveFile = ProjectApproveInfo.ApproveFile()
                                approveFile.file_name = fileBean!!.file_name
                                approveFile.size = fileBean!!.size
                                approveFile.url = fileBean!!.url
                                approveFile.filePath = fileBean!!.filePath
                                level2Item.file = approveFile
                                level2Item.localFile = file
                                level2Item.type = 0
                                if (null != adapter){
                                    adapter.addData(addPosition,level2Item)
                                }
                            }
                            showSuccessToast("上传成功")

                            val fileDataBean = FileDataBean()
                            fileDataBean.class_id = class_id
                            fileDataBean.filepath = fileBean!!.filePath
                            fileDataBean.filename = fileBean!!.file_name
                            fileDataBean.size = fileBean!!.size
                            uploadFiles.add(fileDataBean)
                        }else{
                           showErrorToast(payload.message)
                        }
                        hideProgress()
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("上传失败")
                        hideProgress()
                    }
                }
    }
}