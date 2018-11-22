package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.RecordInfoBean
import com.sogukj.pe.module.approve.ListSelectorActivity
import com.sogukj.pe.service.ProjectService
import com.sogukj.pe.widgets.CalendarDingDing
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_record_detail.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RecordDetailActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    lateinit var item: RecordInfoBean.ListBean
    val gson = Gson()
    var type: String = ""
    var format = SimpleDateFormat("yyyy.MM.dd HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setContentView(R.layout.activity_record_detail)

        type = intent.getStringExtra(Extras.TYPE)
        if (type == "ADD") {
            title = "新增记录"

            var startT = Date(System.currentTimeMillis())
            var endT = Date(System.currentTimeMillis() + 3600 * 1000)

            tv_start_time.text = format.format(startT)
            tv_end_time.text = format.format(endT)

            var startDD = CalendarDingDing(context)
            tv_start_time.setOnClickListener {
                var time = format.parse(tv_start_time.text.toString())
                startDD.show(2, time) { date ->
                    if (date != null) {
                        tv_start_time.text = format.format(date)
                    }
                }
            }

            var deadDD = CalendarDingDing(context)
            tv_end_time.setOnClickListener {
                var time = format.parse(tv_end_time.text.toString())
                deadDD.show(2, time) { date ->
                    if (date != null) {
                        tv_end_time.text = format.format(date)
                    }
                }
            }
        } else if (type.equals("VIEW")) {
            setTitle("记录详情")

            tv_start_time.setOnClickListener(null)
            tv_end_time.setOnClickListener(null)
            company_name.setOnClickListener(null)
            company_name.setCompoundDrawables(null, null, null, null)
            tv_start_time.setCompoundDrawables(null, null, null, null)
            tv_end_time.setCompoundDrawables(null, null, null, null)
            et_visiter.isFocusable = false
            et_visiter.isEnabled = false
            et_des.isFocusable = false
            et_des.isEnabled = false
            cb_important.isEnabled = false

            item = intent.getSerializableExtra(Extras.DATA2) as RecordInfoBean.ListBean
            tv_start_time.text = DateUtils.timet("${item.start_time}")
            tv_end_time.text = DateUtils.timet("${item.end_time}")
            et_visiter.setText(if (item.visits.isNullOrEmpty()) "无" else item.visits)
            et_des.setText(if (item.des.isNullOrEmpty()) "无" else item.des)
            cb_important.isChecked = item.important != 0

            et_visiter.gravity = Gravity.RIGHT
            et_des.viewTreeObserver.addOnGlobalLayoutListener {
                if (et_des.layout.lineCount > 1) {
                    et_des.gravity = Gravity.LEFT
                } else {
                    et_des.gravity = Gravity.RIGHT
                }
            }
        }

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        if (project.name.isNullOrEmpty()) {
            company_name.text = ""
            company_name.hint = "请选择"
            company_name.setOnClickListener {
                var map = CustomSealBean.ValueBean()
                map.type = 2
                var bean = CustomSealBean()
                bean.name = "公司名称"
                bean.value_map = map
                var data: Int? = null
                data = try {
                    intent.getIntExtra(Extras.DATA2, -1)
                } catch (e: Exception) {
                    null
                }
                ListSelectorActivity.start(this, bean, type = data)
            }
        } else {
            company_name.text = project.name
        }

        setBack(true)

        view_des.setOnClickListener {
            if (null != et_des){
                Utils.showSoftInputFromWindow(this,et_des)
            }
        }
        if ("ADD".equals(type)){
            toolbar_menu.text = "提交"
            toolbar_menu.visibility = View.VISIBLE
        }else if ("VIEW".equals(type)){
            toolbar_menu.visibility = View.INVISIBLE
        }

        toolbar_menu.clickWithTrigger {
            if (project.company_id == null) {
                showCustomToast(R.drawable.icon_toast_common, "请选择公司")
            } else {
                upload(project.company_id!!)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ListSelectorActivity.REQ_LIST_SELECTOR && resultCode === Activity.RESULT_OK) {
            val valueBean = data!!.getSerializableExtra(Extras.DATA2) as CustomSealBean.ValueBean
            company_name.text = valueBean.name
            project.company_id = valueBean.id
        }
    }

//    override val menuId: Int
//        get() = R.menu.menu_mark
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val flag = super.onCreateOptionsMenu(menu)
//        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
//        if (type == "ADD") {
//            menuMark.title = "提交"
//        } else if (type == "VIEW") {
//            menuMark.title = ""
//        }
//        return flag
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.action_mark -> {
//                if (type == "ADD") {
//                    if (project.company_id == null) {
//                        showCustomToast(R.drawable.icon_toast_common, "请选择公司")
//                    } else {
//                        upload(project.company_id!!)
//                    }
//                }
//            }
//        }
//        return false
//    }

    var paramsMap = HashMap<String, Any>()

    fun upload(it: Int) {
        var isImportant = 0
        if (cb_important.isChecked) {
            isImportant = 1
        }

        var start_Date = format.parse(tv_start_time.text.toString())
        start_Date.time /= 1000
        var end_Date = format.parse(tv_end_time.text.toString())
        end_Date.time /= 1000
        if (end_Date.time < start_Date.time) {
            showCustomToast(R.drawable.icon_toast_common, "日期错误")
            return
        }
        if (et_des.text.toString().trim() == "") {
            showCustomToast(R.drawable.icon_toast_common, "跟踪情况描述不能为空")
            return
        }

        var paramsObj = HashMap<String, Any>()
        paramsObj.put("company_id", it)
        paramsObj.put("start_time", start_Date.time)
        paramsObj.put("end_time", end_Date.time)
        paramsObj.put("visits", et_visiter.text.toString())
        paramsObj.put("des", et_des.text.toString())
        paramsObj.put("important", isImportant)
        paramsMap.put("ae", paramsObj)

        SoguApi.getService(application,ProjectService::class.java)
                .addRecord(paramsMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    companion object {
        fun startAdd(ctx: Activity?, project: ProjectBean, type: Int? = null) {
            val intent = Intent(ctx, RecordDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, "ADD")
            intent.putExtra(Extras.DATA2, type)
            ctx?.startActivityForResult(intent, 0x001)
        }

        fun startView(ctx: Activity?, project: ProjectBean, item: RecordInfoBean.ListBean) {
            val intent = Intent(ctx, RecordDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.DATA2, item)
            intent.putExtra(Extras.TYPE, "VIEW")
            ctx?.startActivityForResult(intent, 0x002)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (type == "VIEW") {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
