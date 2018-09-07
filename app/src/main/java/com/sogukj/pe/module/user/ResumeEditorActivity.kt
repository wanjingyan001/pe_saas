package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.mcxtzhang.swipemenulib.SwipeMenuLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.EducationBean
import com.sogukj.pe.bean.WorkEducationBean
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_resume_editor.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find
import kotlin.properties.Delegates

class ResumeEditorActivity : BaseActivity(), View.OnClickListener {


    lateinit var eduadapter: RecyclerAdapter<EducationBean>
    lateinit var workAdapter: RecyclerAdapter<WorkEducationBean>
    private var intExtra: Int by Delegates.notNull()

    companion object {
        val EDU = 1
        val WORK = 2

        fun start2(ctx: Activity?, dataList: ArrayList<WorkEducationBean>) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, WORK)
            intent.flags = WORK
            intent.putParcelableArrayListExtra(Extras.LIST, dataList)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, dataList: ArrayList<EducationBean>) {
            val intent = Intent(ctx, ResumeEditorActivity::class.java)
            intent.putExtra(Extras.TYPE, EDU)
            intent.flags = EDU
            intent.putParcelableArrayListExtra(Extras.LIST, dataList)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_editor)
        Utils.setWindowStatusBarColor(this, R.color.white)
        addTv.text = "编辑"
        back.setOnClickListener(this)
        addTv.setOnClickListener(this)
//        intExtra = intent.getIntExtra(Extras.TYPE, -1)
        intExtra = intent.flags
        when (intExtra) {
            EDU -> {
                toolbar_title.text = "教育经历"
                tv_add_work_expericence.visibility = View.GONE
                val list = intent.getParcelableArrayListExtra<EducationBean>(Extras.LIST)
                eduadapter = RecyclerAdapter(this, { _adapter, parent, position ->
                    val convertView = _adapter.getView(R.layout.item_resume_editlist, parent)
                    object : RecyclerHolder<EducationBean>(convertView) {
                        val name = convertView.find<TextView>(R.id.name)
                        val time = convertView.find<TextView>(R.id.timeTv)
                        val right = convertView.find<ImageView>(R.id.ic_right)
                        val delete = convertView.find<TextView>(R.id.delete)
                        val deleteImg = convertView.find<ImageView>(R.id.deleteImg)
                        val content = convertView.find<RelativeLayout>(R.id.contentLayout)
                        val educationLayout = convertView.find<SwipeMenuLayout>(R.id.educationLayout)
                        override fun setData(view: View, data: EducationBean, position: Int) {
                            name.text = data.school
                            time.text = "${data.toSchoolDate}-${data.graduationDate}"
                            delete.setOnClickListener {
                                data.id?.let {
                                    deleteExperience(it, 1, position)
                                }
                            }
                            deleteImg.visibility = if (data.isShow) View.VISIBLE else View.GONE
                            deleteImg.setOnClickListener {
                                educationLayout.smoothExpand()
                            }
                            if (data.isShow) {
                                right.visibility = View.GONE
                                content.setOnClickListener(null)
                            } else {
                                right.visibility = View.VISIBLE
                                content.setOnClickListener {
                                    EducationActivity.start(this@ResumeEditorActivity, data)
                                }
                            }
                        }
                    }
                })
                eduadapter.dataList.addAll(list)
                resumeList.layoutManager = LinearLayoutManager(this)
                resumeList.adapter = eduadapter
                tv_add_education.setOnClickListener(this)
            }
            WORK -> {
                toolbar_title.text = "工作经历"
                tv_add_education.visibility = View.GONE
                val list = intent.getParcelableArrayListExtra<WorkEducationBean>(Extras.LIST)
                workAdapter = RecyclerAdapter(this, { _adapter, parent, position ->
                    val convertView = _adapter.getView(R.layout.item_resume_editlist, parent)
                    object : RecyclerHolder<WorkEducationBean>(convertView) {
                        val name = convertView.find<TextView>(R.id.name)
                        val time = convertView.find<TextView>(R.id.timeTv)
                        val right = convertView.find<ImageView>(R.id.ic_right)
                        val delete = convertView.find<TextView>(R.id.delete)
                        val deleteImg = convertView.find<ImageView>(R.id.deleteImg)
                        val content = convertView.find<RelativeLayout>(R.id.contentLayout)
                        val educationLayout = convertView.find<SwipeMenuLayout>(R.id.educationLayout)
                        override fun setData(view: View, data: WorkEducationBean, position: Int) {
                            name.text = data.company
                            time.text = "${data.employDate}-${data.leaveDate}"
                            delete.setOnClickListener {
                                data.id.let {
                                    deleteExperience(it, 2, position)
                                }
                            }
                            deleteImg.visibility = if (data.isShow) View.VISIBLE else View.GONE
                            deleteImg.setOnClickListener {
                                educationLayout.smoothExpand()
                            }
                            if (data.isShow) {
                                right.visibility = View.GONE
                                content.setOnClickListener(null)
                            } else {
                                right.visibility = View.VISIBLE
                                content.setOnClickListener {
                                    WorkExpericenceAddActivity.start(this@ResumeEditorActivity, data, data.trade_name)
                                }
                            }
                        }
                    }
                })
                workAdapter.dataList.addAll(list)
                resumeList.layoutManager = LinearLayoutManager(this)
                resumeList.adapter = workAdapter

                tv_add_work_expericence.setOnClickListener(this)
            }
        }
    }


    fun deleteExperience(id: Int, type: Int, position: Int) {
        SoguApi.getService(application,UserService::class.java)
                .deleteExperience(id, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_success, "删除成功")
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    when (intExtra) {
                        EDU -> {
                            RxBus.getIntanceBus().post(eduadapter.dataList[position])
                            eduadapter.dataList.removeAt(position)
                            eduadapter.notifyItemRemoved(position)
                            eduadapter.notifyDataSetChanged()
                        }
                        WORK -> {
                            RxBus.getIntanceBus().post(workAdapter.dataList[position])
                            workAdapter.dataList.removeAt(position)
                            workAdapter.notifyItemRemoved(position)
                            workAdapter.notifyDataSetChanged()
                        }
                    }
                    hideProgress()
                }, {
                    showProgress("正在提交删除")
                })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_add_education -> {
                EducationActivity.start(this)
            }
            R.id.tv_add_work_expericence -> {
                WorkExpericenceAddActivity.start(this)
            }
            R.id.back -> {
                onBackPressed()
            }
            R.id.addTv -> {
                addTv.isSelected = !addTv.isSelected
                addTv.text = if (addTv.isSelected) "完成" else "编辑"
                when (intExtra) {
                    EDU -> {
                        eduadapter.dataList.forEach {
                            it.isShow = addTv.isSelected
                        }
                        eduadapter.notifyDataSetChanged()
                    }
                    WORK -> {
                        workAdapter.dataList.forEach {
                            it.isShow = addTv.isSelected
                        }
                        workAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Extras.RESULTCODE -> {
                    val list = data.getParcelableExtra<WorkEducationBean>(Extras.LIST)
                    val bean = workAdapter.dataList.find { it.id == list.id }
                    if (bean == null) {
                        workAdapter.dataList.add(list)
                    } else {
                        bean.employDate = list.employDate
                        bean.leaveDate = list.leaveDate
                        bean.company = list.company
                        bean.responsibility = list.responsibility
                        bean.jobInfo = list.jobInfo
                        bean.department = list.department
                        bean.companyScale = list.companyScale
                        bean.companyProperty = list.companyProperty
                        bean.trade = list.trade
                        bean.trade_name = data.type
                        bean.pid = list.pid
                    }
                    workAdapter.notifyDataSetChanged()
                }
                Extras.RESULTCODE2 -> {
                    val list = data.getParcelableExtra<EducationBean>(Extras.LIST2)
                    val bean = eduadapter.dataList.find { it.id == list.id }
                    if (bean == null){
                        eduadapter.dataList.add(list)
                    }else{
                        bean.toSchoolDate = list.toSchoolDate
                        bean.graduationDate = list.graduationDate
                        bean.school = list.school
                        bean.education = list.education
                        bean.major = list.major
                        bean.majorInfo = list.majorInfo
                    }
                    eduadapter.notifyDataSetChanged()
                }
            }
        }
    }
}
