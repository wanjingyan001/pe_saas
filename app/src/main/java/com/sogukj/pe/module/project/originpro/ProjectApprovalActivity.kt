package com.sogukj.pe.module.project.originpro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bigkoo.pickerview.TimePickerView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.project.originpro.callback.ProjectApproveCallBack
import com.sogukj.pe.module.project.originpro.presenter.ProjectApprovePresenter
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import kotlinx.android.synthetic.main.activity_project_approval.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by CH-ZH on 2018/9/18.
 * 立项申请
 */
class ProjectApprovalActivity : ToolbarActivity(), ProjectApproveCallBack {
    private var project: ProjectBean? = null
    lateinit var adapter: RecyclerAdapter<ApproveFrameInfo>
    private var presenter: ProjectApprovePresenter? = null
    private var frameInfos = ArrayList<ApproveFrameInfo>()
    private var yearMap = HashMap<Int, TextView>()
    private var propertyMap = HashMap<Int, EditText>()
    private var incomeMap = HashMap<Int, EditText>()
    private var profitMap = HashMap<Int, EditText>()
    private var class_id: Int? = null
    private var class_file_id : Int ? = null
    private var type = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_approval)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle(R.string.project_info)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean?
        presenter = ProjectApprovePresenter(this, this)
    }

    private fun initData() {
        if (null != project) {
            tv_name_simple.text = project!!.name
        }
        adapter = RecyclerAdapter<ApproveFrameInfo>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.layout_pro_edit, parent)
            object : RecyclerHolder<ApproveFrameInfo>(convertView) {
                val rl_project_time = convertView.findViewById<RelativeLayout>(R.id.rl_project_time)
                val tv_time = convertView.findViewById<TextView>(R.id.tv_time)
                val et_property_name = convertView.findViewById<EditText>(R.id.et_property_name)
                val et_income_name = convertView.findViewById<EditText>(R.id.et_income_name)
                val et_profit_name = convertView.findViewById<EditText>(R.id.et_profit_name)
                override fun setData(view: View, data: ApproveFrameInfo, position: Int) {
                    rl_project_time.setOnClickListener {
                        val selectedDate = Calendar.getInstance()//系统当前时间
                        val startDate = Calendar.getInstance()
                        startDate.set(1949, 10, 1)
                        val endDate = Calendar.getInstance()
                        endDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH))
                        val timePicker = TimePickerView.Builder(this@ProjectApprovalActivity, { date, view ->
                            tv_time.text = Utils.getTime_(date)
                        })
                                //年月日时分秒 的显示与否，不设置则默认全部显示
                                .setType(booleanArrayOf(true, true, false, false, false, false))
                                .setDividerColor(Color.DKGRAY)
                                .setContentSize(21)
                                .setDate(selectedDate)
                                .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                                .setRangDate(startDate, endDate)
                                .build()
                        timePicker.show()
                    }

                    if (data.year.startsWith("__年")) {
                        tv_time.text = ""
                    } else {
                        tv_time.text = data.year
                    }
                    et_property_name.setText(data.property_amount)
                    et_income_name.setText(data.income_amount)
                    et_profit_name.setText(data.profit_amount)

                    yearMap.put(position, tv_time)
                    propertyMap.put(position, et_property_name)
                    incomeMap.put(position, et_income_name)
                    profitMap.put(position, et_profit_name)
                }

            }

        })

        recycler_view.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                Utils.dip2px(this, 8f), Color.parseColor("#f7f9fc")))
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter

        if (null != presenter && null != project) {
            presenter!!.getProApproveInfo(project!!.company_id!!, project!!.floor!!)
        }
    }

    private fun bindListener() {
        ll_create.setOnClickListener {
            //提交立项申请
            commitProjectData(2)
        }
        //添加文件
        add_file_view.addFileListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                FileMainActivity.start(context, 1, requestCode = REQ_SELECT_FILE)
            }
        })

        rl_project_name.setOnClickListener {
            //选择项目名称
//            ProjectSearchResultActivity.start(this, tv_name_simple.text.toString(), REQ_PROJECT_NAME)
        }
    }

    private fun commitProjectData(type: Int) {
        this.type = type
        val infos = ArrayList<TableFrameInfo>()
        val list = adapter.dataList
        for (i in list.indices) {
            val info = TableFrameInfo()
            for ((k, v) in yearMap) {
                if (k == i) {
                    info.year = v.textStr
                }
            }

            for ((k, v) in propertyMap) {
                if (k == i) {
                    info.asset = v.textStr
                }
            }

            for ((k, v) in incomeMap) {
                if (k == i) {
                    info.income = v.textStr
                }
            }

            for ((k, v) in profitMap) {
                if (k == i) {
                    info.profit = v.textStr
                }
            }
            infos.add(info)
        }
        var years = ""
        for (info in infos){
            if (!info.year.isNullOrEmpty()){
                if (years.contains(info.year.subSequence(0,4))){
                    showErrorToast("不能有相同的年份")
                    return
                }else{
                    years += "${info.year.subSequence(0,4)},"
                }
            }
        }
        val files = ArrayList<FileDataBean>()
        if (null != add_file_view) {
            val fileData = add_file_view.getFileData()
            for (info in fileData) {
                val fileBean = FileDataBean()
                fileBean.class_id = class_file_id
                fileBean.filepath = info.filePath
                fileBean.filename = info.file_name
                fileBean.size = info.size

                files.add(fileBean)
            }
        }
        commitProjectDataToNet(infos, files, type)
    }

    private fun commitProjectDataToNet(infos: ArrayList<TableFrameInfo>, files: ArrayList<FileDataBean>, type: Int) {
        val map = HashMap<String, Any>()
        map.put("company_id", project!!.company_id!!)
        map.put("type", type)
        map.put("floor", project!!.floor!!)
        map.put("current", 0)
        map.put("table", infos)
        map.put("files", files)
        showProgress("正在提交")
        if (null != presenter) {
            presenter!!.createApprove(map,type)
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
            commitProjectData(1)
            mDialog.dismiss()
        }

        mDialog.show()
    }

    companion object {
        val REQ_SELECT_FILE = 0x1001
        val REQ_PROJECT_NAME = 0x1002
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_SELECT_FILE -> {
                    val paths = data?.getStringArrayListExtra(Extras.LIST)
                    paths?.forEach {
                        val info = ProjectApproveInfo.ApproveFile()
                        val file = File(it)
                        info.file = file
                        info.file_name = file.name
                        add_file_view.addFileData(info, file)
                    }
                }
                REQ_PROJECT_NAME -> {
                    val extra = data.getStringExtra(Extras.DATA)
                    if (!extra.isNullOrEmpty()) {
                        tv_name_simple.text = extra.toString()
                    }
                }
            }

        }
    }

    override fun goneCommitLodding() {
        hideProgress()
    }

    override fun createApproveSuccess() {
        if (type == 2){
            showSuccessToast("提交成功")
            startActivity<ProjectApprovalShowActivity>(Extras.DATA to project)
            finish()
        }else{
            showSuccessToast("保存成功")
            super.onBackPressed()
        }
    }

    override fun setProApproveInfo(infos: List<ProjectApproveInfo>) {
        if (null != infos) {
            if (infos.size > 0) {
                val approveInfo = infos[0]
                if (null != approveInfo) {
                    class_id = approveInfo.class_id
                    tv_title.text = approveInfo.name
                    val frames = approveInfo.frame
                    setAmountData(frames!!)
                }
            }
            if (infos.size > 1) {
                val approveInfo = infos[1]
                if (null != approveInfo) {
                    class_file_id = approveInfo.class_id
                    setFilesData(approveInfo.files)
                }
            }
        }
    }

    private fun setFilesData(files: List<ProjectApproveInfo.ApproveFile>?) {
        if (null != files && files.size > 0) {
            if (null != add_file_view) {
                add_file_view.setFileData(files!!)
            }
        }
    }

    private fun setAmountData(frames: List<ProjectApproveInfo.ApproveInfo>) {
        frameInfos.clear()
        if (null != frames && frames.size > 0) {
            for (frame in frames) {
                val info = ApproveFrameInfo()
                info.year = frame.year
                info.property_amount = frame.asset
                info.income_amount = frame.income
                info.profit_amount = frame.profit
                frameInfos.add(info)
            }

        } else {
            for (j in 0..2) {
                val info = ApproveFrameInfo()
                frameInfos.add(info)
            }
        }

        adapter.dataList.clear()
        adapter.dataList.addAll(frameInfos)
        adapter.notifyDataSetChanged()
    }
}