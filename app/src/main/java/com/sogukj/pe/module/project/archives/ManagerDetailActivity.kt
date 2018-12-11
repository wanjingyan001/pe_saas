package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil.setContentView
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.*
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setOnClickFastListener
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ManagerDetailBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manager_detail.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.sdk25.coroutines.onFocusChange
import org.jetbrains.anko.textColor
import java.io.File

class ManagerDetailActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, moduleId: Int, title: String) {
            val intent = Intent(ctx, ManagerDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.MODULE_ID, moduleId)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }

    var moduleId: Int? = null
    lateinit var bean: ProjectBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_detail)
        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)
        moduleId = intent.getIntExtra(Extras.MODULE_ID, 0)
        bean = intent.getSerializableExtra(Extras.DATA) as ProjectBean

        doRequest()

        btn_commit.setOnClickListener {

            //只要有一个为true就可以提交
            var flag = false
            for (check in checkList) {
                flag = flag.or(check())
            }
            if (!flag) {
                finish()
            } else {
                var map = HashMap<String, Any>()
                map.put("projId", bean.company_id!!)
                map.put("moduleId", moduleId!!)
                map.put("data", oriData)

                SoguApi.getService(application, ProjectService::class.java)
                        .subInvestMan(map)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                finish()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
                            showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                        })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideProgress()
    }

    lateinit var oriData: ArrayList<ManagerDetailBean>

    private fun doRequest() {
        SoguApi.getService(application, ProjectService::class.java)
                .getInvestManDetail(bean.company_id!!, moduleId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.payload?.apply {
                            if (this.size == 0) {
                                ll_layout.visibility = View.GONE
                                iv_empty.visibility = View.VISIBLE
                            } else {
                                oriData = this
                                initView(oriData)
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        ll_layout.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                }, { e ->
                    Trace.e(e)
                    ll_layout.visibility = View.GONE
                    iv_empty.visibility = View.VISIBLE
                })
    }

    val checkList = ArrayList<() -> Boolean>()

    private fun initView(list: ArrayList<ManagerDetailBean>) {
        list.forEach {
            // control=0   一定有child
            when (it.control) {//0=无控件,1=中型输入框,2=大型输入框,4=单选框,10=文件输入框
                0 -> add0(it)
                1 -> add1(it)
                4 -> add4(it)
                10 -> add10(it)
            }
        }
    }

    private fun add0(bean: ManagerDetailBean) {
        var childView = layoutInflater.inflate(R.layout.item_manager_detail_row0, null)
        content.addView(childView)

        val tvLabel = childView.findViewById<TextView>(R.id.tv_label) as TextView
        tvLabel.text = bean.zhName

        val cell = childView.findViewById<LinearLayout>(R.id.cell) as LinearLayout
        bean.child?.forEach {
            when (it.control) {
                1 -> add1(it, cell)
                4 -> add4(it, cell)
                10 -> add10(it, cell)
            }
        }
    }

    private fun add1(bean: ManagerDetailBean, view: LinearLayout? = null) {
        var childView: LinearLayout
        if (view == null) {
            childView = layoutInflater.inflate(R.layout.item_manager_detail_row1, null) as LinearLayout
            content.addView(childView)
        } else {
            childView = layoutInflater.inflate(R.layout.item_inner_1, null) as LinearLayout
            view.addView(childView)
        }

        val tvLabel = childView.findViewById<TextView>(R.id.tv_label) as TextView
        tvLabel.text = bean.zhName

        val et_content = childView.findViewById<EditText>(R.id.et_content) as EditText
        et_content.onFocusChange { v, hasFocus ->
            if (hasFocus) {
                et_content.setSelection(et_content.text.length)
            } else {
                et_content.setText(et_content.text)
            }
        }
        if (bean.contents.isNullOrEmpty()) {
            et_content.setSelection(0)
        } else {
            et_content.setText(bean.contents)
            //et_content.setSelection(bean.contents!!.length)
        }

        val cell = childView.findViewById<LinearLayout>(R.id.cell) as LinearLayout
        if (bean.child == null || bean.child!!.size == 0) {
            cell.visibility = View.GONE
        } else {
            cell.visibility = View.VISIBLE
            bean.child?.forEach {
                when (it.control) {
                    1 -> add1(it, cell)
                    4 -> add4(it, cell)
                    10 -> add10(it, cell)
                }
            }
        }

        checkList.add {
            var str = et_content.text.toString()
            if (str.isNullOrEmpty()) {
                false
            } else {
                bean.contents = str
                true
            }
        }
    }

    private fun add4(bean: ManagerDetailBean, view: LinearLayout? = null) {
        var childView: LinearLayout
        var textColor = 0
        if (view == null) {
            childView = layoutInflater.inflate(R.layout.item_manager_detail_row4, null) as LinearLayout
            content.addView(childView)
            textColor = Color.parseColor("#282828")
        } else {
            childView = layoutInflater.inflate(R.layout.item_inner_4, null) as LinearLayout
            view.addView(childView)
            textColor = Color.parseColor("#a0a4aa")
        }

        val tvLabel = childView.findViewById<TextView>(R.id.tv_label) as TextView
        tvLabel.text = bean.zhName

        val rgCheck = childView.findViewById<RadioGroup>(R.id.rg_check) as RadioGroup
        var desc = bean.elsed
        if (!desc.isNullOrEmpty()) {
            var arrayDesc = desc?.split(",")
            rgCheck.removeAllViews()
            arrayDesc?.forEach {
                var rb = RadioButton(context)
                rb.layoutParams = LinearLayout.LayoutParams(Utils.dpToPx(context, 10), LinearLayout.LayoutParams.WRAP_CONTENT)
                rb.visibility = View.INVISIBLE
                rgCheck.addView(rb)

                var radioBtn = RadioButton(context)
                var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                //params.setMargins(Utils.dpToPx(context, 10), 0, 0, 0)
                params.gravity = Gravity.CENTER_VERTICAL
                radioBtn.layoutParams = params
                radioBtn.setButtonDrawable(null)
                radioBtn.background = null
                radioBtn.gravity = Gravity.CENTER
                var drawable = resources.getDrawable(R.drawable.ic_cs_radio)
                drawable.setBounds(0, 0, Utils.spToPx(context, 14), Utils.spToPx(context, 14))
                radioBtn.setCompoundDrawables(drawable, null, null, null)
                radioBtn.text = it
                radioBtn.textColor = textColor
                radioBtn.textSize = 14f
                rgCheck.addView(radioBtn)
            }
            rgCheck.setOnCheckedChangeListener { group, checkedId ->
                (0 until rgCheck.childCount)
                        .map { rgCheck.getChildAt(it) as RadioButton }
                        .forEach { it.isChecked = false }
                (rgCheck.findViewById<RadioButton>(rgCheck.checkedRadioButtonId) as RadioButton).isChecked = true
            }
            (0 until rgCheck.childCount)
                    .map { rgCheck.getChildAt(it) as RadioButton }
                    .forEach {
                        if (!bean.contents.isNullOrEmpty()) {
                            if (bean.contents!! == it.text) {
                                it.isChecked = true
                            }
                        }
                    }
        }
        checkList.add {
            val str = (0 until rgCheck.childCount)
                    .map { rgCheck.getChildAt(it) as RadioButton }
                    .firstOrNull { it.isChecked }
                    ?.let { it.text.toString() }
                    ?: ""
            if (str.isNullOrEmpty()) {
                false
            } else {
                bean.contents = str
                true
            }
        }
    }

    private fun add10(bean: ManagerDetailBean, view: LinearLayout? = null) {
        var childView: LinearLayout
        if (view == null) {
            childView = layoutInflater.inflate(R.layout.item_manager_detail_row10, null) as LinearLayout
            content.addView(childView)
        } else {
            childView = layoutInflater.inflate(R.layout.item_inner_10, null) as LinearLayout
            view.addView(childView)
        }

        val tvLabel = childView.findViewById<TextView>(R.id.tv_label) as TextView
        tvLabel.text = bean.zhName

        val ll_files = childView.findViewById<LinearLayout>(R.id.ll_files) as LinearLayout
        ll_files.removeAllViews()
        var filesView = ll_files
        var pathList = HashMap<String, String>()
        try {
            bean.files?.apply {
                pathList.putAll(this)
            }
        } catch (e: Exception) {
            pathList.clear()
        }

        viewMap.put(bean.id!!, filesView)
        pathMap.put(bean.id!!, pathList)
        replaceMap.put(bean.id!!, -1)

        refreshFiles(bean.id!!)

        val tvFile = childView.findViewById<FrameLayout>(R.id.tvFile) as FrameLayout
        tvFile.setOnClickFastListener {
            selectId = bean.id!!
            showProgress("正在读取内存文件")
            FileMainActivity.start(context, requestCode = REQ_SELECT_FILE, maxSize = 1)
        }
        checkList.add {
            var map = HashMap<String, String>()
            var pathList = pathMap[bean.id!!]!!
            val rawData = bean.files?.isEmpty() ?: true
            if ((pathList == null || pathList.size == 0) && (bean.files == null || rawData)) {
                false
            } else {
                map.putAll(pathList)
                bean.files = map
                true
            }
        }
    }

    //key就是bean.id
    var viewMap = HashMap<Int, LinearLayout>()
    var pathMap = HashMap<Int, HashMap<String, String>>()
    var replaceMap = HashMap<Int, Int>()

    private fun refreshFiles(id: Int) {
        var filesView = viewMap.get(id)!!
        var pathList = pathMap.get(id)!!
        var replaceFileStr = replaceMap.get(id)!!

        filesView.removeAllViews()
        for ((k, v) in pathList) {
            if (!v.isNullOrEmpty()) {
                val convertView = layoutInflater.inflate(R.layout.cs_item_file_manager, null)
                filesView.addView(convertView)

                val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                val tvReplace = convertView.findViewById<TextView>(R.id.btn_replace) as TextView
                val tvDel = convertView.findViewById<TextView>(R.id.btn_delete) as TextView

                tvName.text = k
                tvDel.setOnClickListener {
                    pathList.remove(k)
                    pathMap.put(id, pathList)
                    refreshFiles(id)
                }
                tvName.setOnClickListener {
                    if (!TextUtils.isEmpty(v)) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(v)
                        startActivity(intent)
                    }
                }
            }
        }
        viewMap.put(id, filesView)
    }

    var selectId = -1

    private fun uploadFile(filePath: String?) {
        if (!filePath.isNullOrEmpty()) {
            val file = File(filePath)
            SoguApi.getService(application, ProjectService::class.java)
                    .uploadFile(MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("*/*"), file))
                            .build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload?.payload?.apply {
                                showCustomToast(R.drawable.icon_toast_success, "上传成功")
                                var pathList = pathMap.get(selectId)!!

                                //pathList.add(filePath!!)  同名文件必须删除
                                var mContains = false
                                for (path in pathList) {
                                    if (path.equals(filePath)) {
                                        mContains = true
                                        break
                                    }
                                }
                                if (!mContains) {
                                    try {
                                        pathList.put(File(filePath).name, this.get(0))
                                    } catch (e: Exception) {
                                    }
                                }

                                pathMap.put(selectId, pathList)
                                refreshFiles(selectId)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        handler.postDelayed({
                            hideProgress()
                            showCustomToast(R.drawable.icon_toast_fail, "上传失败")
                        }, 500)
                    }, {
                        handler.postDelayed({
                            hideProgress()
                        }, 500)
                    }, {
                        showProgress("正在上传")
                    })
        }
    }

    val REQ_SELECT_FILE = 0xf0
    val REQ_CHANGE_FILE = 0xf1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SELECT_FILE && resultCode === Activity.RESULT_OK) {
            val paths = data?.getStringArrayListExtra(Extras.LIST)
            paths?.forEach {
                uploadFile(it)
            }
        } else if (requestCode == REQ_CHANGE_FILE && resultCode === Activity.RESULT_OK) {
            val paths = data?.getStringExtra(Extras.DATA)
            paths?.apply {
                uploadFile(this)
            }
        }
    }
}
