package com.sogukj.pe.module.fund

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ManagerDetailBean
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manager_detail.*
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onFocusChange

class FundAssociationDetailActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, projId: Int, baseDateId: Int, moduleId: Int, title: String) {
            val intent = Intent(ctx, FundAssociationDetailActivity::class.java)
            intent.putExtra("projId", projId)
            intent.putExtra("baseDateId", baseDateId)
            intent.putExtra("moduleId", moduleId)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }

    var projId = 0
    var baseDateId = 0
    var moduleId = 0
    private var editText:EditText? = null
    private var editInfos = HashMap<String,EditText>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_detail)
        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)

        projId = intent.getIntExtra("projId", 0)
        baseDateId = intent.getIntExtra("baseDateId", 0)
        moduleId = intent.getIntExtra("moduleId", 0)

        doRequest()

        btn_commit.setOnClickListener {
            var isCheckEmpty = false
            if (!isCheckEmpty){
                for ((k, v) in editInfos) {
//                    Log.e("TAG","  editInfos ---  key ==" + k + "   value ==" + v.text.toString())
                    if (k.contains("*")){
                        if (v.text.toString().isNullOrEmpty()){
                            isCheckEmpty = true
                        }
                    }
                }
            }
            if (isCheckEmpty){
                showCustomToast(R.drawable.icon_toast_fail, "带*号的必填")
            }else{
                if (null != oriData && oriData.size > 0){
                    var submitOriData = ArrayList<ManagerDetailBean>()
                    submitOriData.clear()
                    for (ori in oriData){
                        val child = ori.child
                        if (null != child && child.size > 0){
                            for (childOri in child){
                                if (null != childOri && null != childOri.zhName){
                                    val editText = editInfos[childOri.zhName!!]
                                    if (null == childOri.contents){
                                        childOri.contents = ""
                                    }
                                    if (null != editText && !(editText!!.text.toString().equals(childOri.contents))){
                                        childOri.contents = editText!!.text.toString()
                                    }

                                    val child1 = childOri.child
                                    if (null != child1 && child1.size > 0){
                                        for (child1Ori in child1){
                                            if (null != child1Ori && null != child1Ori.zhName){
                                                val editText = editInfos[child1Ori.zhName!!]
                                                if (null == child1Ori.contents){
                                                    child1Ori.contents = ""
                                                }
                                                if (null != editText && !(editText!!.text.toString().equals(child1Ori.contents))){
                                                    child1Ori.contents = editText!!.text.toString()
                                                }

                                                val child2 = child1Ori.child
                                                if (null != child2 && child2.size > 0){
                                                    for (child2Ori in child2){
                                                        if (null != child2Ori && null != child2Ori.zhName){
                                                            val editText = editInfos[child2Ori.zhName!!]
                                                            if (null == child2Ori.contents){
                                                                child2Ori.contents = ""
                                                            }
                                                            if (null != editText && !(editText!!.text.toString().equals(child2Ori.contents))){
                                                                child2Ori.contents = editText!!.text.toString()
                                                            }
                                                        }
                                                    }

                                                }
                                            }
                                        }


                                    }
                                }
                            }
                        }
                        if (null != ori.zhName){
                            val editText = editInfos[ori.zhName!!]
                            if (null == ori.contents){
                                ori.contents = ""
                            }
                            if (null != editText && !(editText!!.text.toString().equals(ori.contents))){
                                ori.contents = editText!!.text.toString()
                            }
                        }

                    }
                    submitOriData.addAll(oriData)
                    var map = HashMap<String, Any>()
                    map.put("projId", projId)
                    map.put("data", submitOriData)
                    for (i in submitOriData){
//                        Log.e("TAG","submitOriData -- name ==" + i.zhName)
                        if (null != i.child && i.child!!.size > 0){
                            for (j in i.child!!){
//                                Log.e("TAG","submitOriDataChild -- name ==" + j.zhName)
                            }
                        }
                    }
//                    Log.e("TAG","  projId ==" + projId + "  data ==" + Utils.objToJson(submitOriData))
                    SoguApi.getService(application,FundService::class.java)
                            .modifiModuleInfo(map)
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
                }else{
                    showCustomToast(R.drawable.icon_toast_fail, "数据异常，提交失败")
                }
            }
            //只要有一个为true就可以提交
//            var flag = false
//            for (check in checkList) {
//                flag = flag.or(check())
//            }
//            if (!flag) {
//                finish()
//            } else {
//
//            }
        }
    }

    lateinit var oriData: ArrayList<ManagerDetailBean>

    fun doRequest() {
        SoguApi.getService(application,FundService::class.java)
                .getModuleDetail(projId, baseDateId, moduleId)
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
            }
        }
        if (null != editText){
            Utils.showSoftInputFromWindow(this,editText)
        }
    }

    private fun add0(bean: ManagerDetailBean) {
        var childView = layoutInflater.inflate(R.layout.item_manager_detail_row0, null)
        content.addView(childView)

        val tvLabel = childView.find<TextView>(R.id.tv_label)
        tvLabel.text = bean.zhName

        val cell = childView.findViewById<LinearLayout>(R.id.cell) as LinearLayout
        bean.child?.forEach {
            when (it.control) {
                1 -> add1(it, cell)
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
//                    4 -> add4(it, cell)
//                    10 -> add10(it, cell)
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
//        Log.e("TAG","zhName ==" + bean.zhName+",")
        if (null == editText){
            editText = et_content
        }
        editInfos.put(bean.zhName!!,et_content)
    }

    override fun onPause() {
        super.onPause()
        if (null != editText){
            Utils.closeInput(this,editText)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
