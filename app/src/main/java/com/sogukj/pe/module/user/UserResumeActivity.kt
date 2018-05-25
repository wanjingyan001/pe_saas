package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bigkoo.pickerview.OptionsPickerView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_resume.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find

class UserResumeActivity : BaseActivity(), View.OnClickListener {
    lateinit var user: UserBean
    var city: CityArea.City? = null
    lateinit var educationAdapter: RecyclerAdapter<EducationBean>
    lateinit var workAdapter: RecyclerAdapter<WorkEducationBean>
    var isSelf: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_resume)
        Utils.setWindowStatusBarColor(this, R.color.white)
        user = intent.getSerializableExtra(Extras.DATA) as UserBean
        if (user.uid == Store.store.getUser(this)?.uid) {
            isSelf = true
            toolbar_title.text = "个人简历"
            addTv.text = "保存"
        } else {
            isSelf = false
            toolbar_title.text = "${user.name}简历"
            addTv.visibility = View.GONE
        }

        initAdapter()

        back.setOnClickListener { finish() }
        tv_add_work_expericence.setOnClickListener {
            WorkExpericenceAddActivity.start(this)
        }
        tv_add_education.setOnClickListener {
            EducationActivity.start(this)
        }
        addTv.setOnClickListener(this)
        tr_job.setOnClickListener(this)
        tr_sex.setOnClickListener(this)
        tr_language.setOnClickListener(this)
        tr_woek_experience.setOnClickListener(this)
        tr_city.setOnClickListener(this)
        tr_education.setOnClickListener(this)
        educationLayout.setOnClickListener(this)
        workLayout.setOnClickListener(this)
        if (isSelf) {
            user.uid?.let { doRequest(it) }
        } else {
            user.user_id?.let { doRequest(it) }
        }
        registeredRxbus()
    }


    private fun registeredRxbus() {
        val subscribe = RxBus.getIntanceBus().getObservable(EducationBean::class.java).subscribe({ educationBean ->
            val bean = educationAdapter.dataList.find {
                it.id == educationBean.id
            }
            educationAdapter.dataList.remove(bean)
            educationAdapter.notifyDataSetChanged()
        }
        )
        val subscribe1 = RxBus.getIntanceBus().getObservable(WorkEducationBean::class.java).subscribe({ workEducationBean ->
            val bean = workAdapter.dataList.find { it.id == workEducationBean.id }
            workAdapter.dataList.remove(bean)
            workAdapter.notifyDataSetChanged()
        })
        RxBus.getIntanceBus().addSubscription(EducationBean, subscribe)
        RxBus.getIntanceBus().addSubscription(WorkEducationBean, subscribe1)
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getIntanceBus().unSubscribe(EducationBean)
        RxBus.getIntanceBus().unSubscribe(WorkEducationBean)
    }


    fun setData(baseInfo: Resume.BaseInfoBean) {
        baseInfo.let {
            tv_name.setText(it.name)
            tv_name.isEnabled = false
            tv_job.setText(it.position)
            when (it.sex) {
                1 -> tv_sex.text = "男"
                2 -> tv_sex.text = "女"
            }
            tv_language.text = it.language
            tv_woek_experience.text = if (it.workYear == "0" || it.workYear == null) "" else "${it.workYear}年"
            tv_city.text = it.cityName
            tv_education.text = it.educationLevel
            tv_mail.text = it.email
            tv_phone.setText(it.phone)
            tv_phone.isEnabled = false
            if (!isSelf) {
                tv_job.isEnabled = false
                tv_sex.isEnabled = false
                tv_language.isEnabled = false
                tv_woek_experience.isEnabled = false
                tv_city.isEnabled = false
                tv_education.isEnabled = false
                tv_mail.isEnabled = false
            }
        }
    }

    private fun setEducationListData(listData: List<Resume.EductionBean>) {
        educationAdapter.dataList.clear()
        listData.forEach {
            val education = EducationBean()
            education.toSchoolDate = it.toSchoolDate!!
            education.graduationDate = it.graduationDate!!
            education.school = it.school!!
            education.education = it.education!!
            education.major = it.major!!
            education.majorInfo = it.majorInfo
            education.id = it.id
            educationAdapter.dataList.add(education)
        }
        educationAdapter.notifyDataSetChanged()
        if (!isSelf) {
            educationList.isEnabled = false
            tv_add_education.isEnabled = false
        }
    }

    private fun setWorkListData(listData: List<Resume.WorkBean>) {
        workAdapter.dataList.clear()
        listData.forEach {
            val workeducation = WorkEducationBean()
            workeducation.employDate = it.employDate
            workeducation.leaveDate = it.leaveDate
            workeducation.company = it.company
            workeducation.responsibility = it.responsibility
            workeducation.jobInfo = it.jobInfo
            workeducation.trade = it.trade
            workeducation.department = it.department
            workeducation.companyScale = it.companyScale
            workeducation.companyProperty = it.companyProperty
            workeducation.trade_name = it.trade_name
            workeducation.id = it.id
            workAdapter.dataList.add(workeducation)
        }
        workAdapter.notifyDataSetChanged()
        if (!isSelf) {
            workList.isEnabled = false
            tv_add_work_expericence.isEnabled = false
        }
    }

    lateinit var resume: Resume
    fun doRequest(uId: Int) {
        SoguApi.getService(application, UserService::class.java)
                .getPersonalResume(user_id = uId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            resume = this
                            this.baseInfo?.apply {
                                setData(this)
                            }
                            this.eduction?.apply {
                                setEducationListData(this)
                            }
                            this.work?.apply {
                                setWorkListData(this)
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    private fun editResumeBaseInfo(reqBean: UserResumeReqBean) {
        val userReq = UserReq()
        userReq.ae = reqBean
        SoguApi.getService(application, UserService::class.java)
                .editResumeBaseInfo(userReq)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_success, "修改成功")
                        changeUserInfo()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    hideProgress()
                }, {
                    hideProgress()
                }, {
                    showProgress("正在提交,请稍等")
                })
    }


    private fun changeUserInfo() {
        val user = Store.store.getUser(context)
        if (null != user?.uid) {
            SoguApi.getService(application, UserService::class.java)
                    .userInfo(user.uid!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                Store.store.setUser(context, this)
                                finish()
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                    })
        }
    }


    private fun initAdapter() {
        educationAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_education_list, parent)
            object : RecyclerHolder<EducationBean>(convertView) {
                val SETime = convertView.find<TextView>(R.id.start_end_time)
                val schoolName = convertView.find<TextView>(R.id.schoolName)
                val education = convertView.find<TextView>(R.id.education)
                override fun setData(view: View, data: EducationBean, position: Int) {
                    SETime.text = "${data.toSchoolDate}-${data.graduationDate}"
                    schoolName.text = data.school
                    education.text = "${data.education} | ${data.major}"
                }
            }
        })
        educationAdapter.onItemClick = { v, position ->
            EducationActivity.start(this, educationAdapter.dataList[position])
        }
        educationList.layoutManager = LinearLayoutManager(this)
        educationList.adapter = educationAdapter

        workAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_education_list, parent)
            object : RecyclerHolder<WorkEducationBean>(convertView) {
                val SETime = convertView.find<TextView>(R.id.start_end_time)
                val companyName = convertView.find<TextView>(R.id.schoolName)
                val info = convertView.find<TextView>(R.id.education)
                override fun setData(view: View, data: WorkEducationBean, position: Int) {
                    SETime.text = "${data.employDate}-${data.leaveDate}"
                    companyName.text = data.company
                    info.text = "${data.responsibility}/${data.department}"
                }
            }
        })
        workAdapter.onItemClick = { v, position ->
            val bean = workAdapter.dataList[position]
            WorkExpericenceAddActivity.start(this, bean, bean.trade_name)
        }
        workList.layoutManager = LinearLayoutManager(this)
        workList.adapter = workAdapter

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addTv -> {
                val reqBean = UserResumeReqBean()
                reqBean.position = tv_job.text.toString()
                when {
                    tv_sex.text.toString() == "男" -> reqBean.sex = 1
                    tv_sex.text.toString() == "女" -> reqBean.sex = 2
                    else -> reqBean.sex = 3
                }
                reqBean.language = tv_language.text.toString()
                try {
                    reqBean.workYear = tv_woek_experience.text.toString().replace("年", "").toInt()
                } catch (e: Exception) {
                }
                reqBean.city = city?.id
                reqBean.educationLevel = tv_education.text.toString()
                reqBean.email = tv_mail.text.toString()
                editResumeBaseInfo(reqBean)
            }
            R.id.tr_sex -> {
                val position: Int = when {
                    tv_sex.text == "男" -> 0
                    tv_sex.text == "女" -> 1
                    else -> 0
                }
                val dataList = arrayListOf("男", "女")
                val pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                    tv_sex.text = dataList[options1]
                }).build()
                pvOptions.setPicker(dataList, null, null)
                pvOptions.setSelectOptions(position)
                pvOptions.show()

            }
            R.id.tr_language -> {
                val position = arrayListOf<Int>()
                val languageList = resources.getStringArray(R.array.Language).toList()
                languageList.forEachIndexed { index, s ->
                    val list = tv_language.text.split(",")
                    if (list.contains(s)) {
                        position.add(index)
                    }
                }
                MaterialDialog.Builder(this)
                        .title("选择语言")
                        .theme(Theme.LIGHT)
                        .items(languageList)
                        .itemsCallbackMultiChoice(position.toTypedArray()) { dialog, which, text ->
                            val build = StringBuilder()
                            text?.forEach {
                                build.append(it)
                                build.append(",")
                            }
                            val str = build.toString()
                            tv_language.text = str.substring(0, str.length - 1)
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
            R.id.tr_woek_experience -> {
                val experience = ArrayList<String>()
                (1..30).mapTo(experience) { "${it}年" }
                val position = experience.indices.firstOrNull { experience[it].contains(tv_woek_experience.text) } ?: 0
                val pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                    tv_woek_experience.text = experience[options1]
                }).build()
                pvOptions.setPicker(experience)
                pvOptions.setSelectOptions(position)
                pvOptions.show()
            }
            R.id.tr_city -> {
                CityAreaActivity.start(this)
            }
            R.id.tr_education -> {
                val dataList = resources.getStringArray(R.array.Education).toList()
                val position = dataList.indices.firstOrNull { dataList[it].contains(tv_education.text) } ?: 0
                val pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                    tv_education.text = dataList.get(options1)
                }).build()
                pvOptions.setPicker(dataList)
                pvOptions.setSelectOptions(position)
                pvOptions.show()
            }
            R.id.educationLayout -> {
                val dataList = educationAdapter.dataList as ArrayList<EducationBean>
                ResumeEditorActivity.start(this, dataList)
            }
            R.id.workLayout -> {
                val dataList = workAdapter.dataList as ArrayList<WorkEducationBean>
                ResumeEditorActivity.start2(this, dataList)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Extras.RESULTCODE3 -> {
                    city = data.getSerializableExtra(Extras.DATA) as CityArea.City?
                    city?.let { tv_city.text = it.name }
                }
                Extras.RESULTCODE -> {
                    var needChange = false
                    val list = data.getParcelableExtra<WorkEducationBean>(Extras.LIST)
                    workAdapter.dataList.forEach {
                        if (it.id == list.id) {
                            it.employDate = list.employDate
                            it.leaveDate = list.leaveDate
                            it.company = list.company
                            it.responsibility = list.responsibility
                            it.jobInfo = list.jobInfo
                            it.department = list.department
                            it.companyScale = list.companyScale
                            it.companyProperty = list.companyProperty
                            it.trade = list.trade
                            it.trade_name = data.type
                            it.pid = list.pid
                            needChange = true
                        }
                    }
                    if (!needChange) {
                        workAdapter.dataList.add(list)
                    }
                    workAdapter.notifyDataSetChanged()
                }
                Extras.RESULTCODE2 -> {
                    var needChange = false
                    val list = data.getParcelableExtra<EducationBean>(Extras.LIST2)
                    educationAdapter.dataList.forEach {
                        if (it.id == list.id) {
                            it.toSchoolDate = list.toSchoolDate
                            it.graduationDate = list.graduationDate
                            it.school = list.school
                            it.education = list.education
                            it.major = list.major
                            it.majorInfo = list.majorInfo
                            needChange = true
                        }
                    }
                    if (!needChange) {
                        educationAdapter.dataList.add(list)
                    }
                    educationAdapter.notifyDataSetChanged()
                }
            }
        }
    }


    companion object {
        fun start(ctx: Activity?, userBean: UserBean) {
            val intent = Intent(ctx, UserResumeActivity::class.java)
            intent.putExtra(Extras.DATA, userBean)
            ctx?.startActivity(intent)
        }
    }
}
