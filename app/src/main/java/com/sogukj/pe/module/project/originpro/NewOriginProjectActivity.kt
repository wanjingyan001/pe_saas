package com.sogukj.pe.module.project.originpro

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CompanySelectBean
import com.sogukj.pe.bean.NewProjectInfo
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.project.originpro.callback.NewOriginProCallBack
import com.sogukj.pe.module.project.originpro.presenter.NewOriginProPresenter
import com.sogukj.pe.module.register.MemberSelectActivity
import com.sogukj.pe.module.user.FormActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ProjectService
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_project.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import kotlinx.android.synthetic.main.layout_pro_top.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivityForResult
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by CH-ZH on 2018/9/17.
 * 新建项目
 */
class NewOriginProjectActivity : ToolbarActivity(), NewOriginProCallBack {
    lateinit var mCompanyAdapter: RecyclerAdapter<CompanySelectBean>
    lateinit var mRelateAdapter: RecyclerAdapter<NewProjectInfo.RelateInfo>
    private val SIMPLE_NAME = 0x111
    private val CREDIT_CODE = 0x110
    private var project: ProjectBean? = null
    private var presenter: NewOriginProPresenter? = null
    private var chargerPmId = 0
    private var chargerPlId = 0
    private var FUZEREN_PM = 0x100
    private var FUZEREN_PL = 0x101
    private var editAble = false
    private var company_id = -1
    private var relateInfos = ArrayList<NewProjectInfo.RelateInfo>()
    private var nameMap = HashMap<Int, EditText>()
    private var jobMap = HashMap<Int, TextView>()
    private var phoneMap = HashMap<Int, EditText>()
    private var idCardMap = HashMap<Int, EditText>()
    private var emailMap = HashMap<Int, EditText>()
    private var user: UserBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_project)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setTitle("项目基本信息")
        setBack(true)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean?
        presenter = NewOriginProPresenter(this, this)
        editAble = false
        user = Store.store.getUser(this)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        ll_create.visibility = View.GONE
    }

    private fun initData() {
        initLocalData()
        mCompanyAdapter = RecyclerAdapter<CompanySelectBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent) as View
            object : RecyclerHolder<CompanySelectBean>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                override fun setData(view: View, data: CompanySelectBean, position: Int) {
                    tv1.text = "${position + 1}." + data.name
                }
            }
        })

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_result.layoutManager = layoutManager
        recycler_result.adapter = mCompanyAdapter


        mRelateAdapter = RecyclerAdapter<NewProjectInfo.RelateInfo>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.layout_add_contact, parent)
            object : RecyclerHolder<NewProjectInfo.RelateInfo>(convertView) {
                val et_contact_name = convertView.findViewById<EditText>(R.id.et_contact_name)
                val tv_contact_name = convertView.findViewById<TextView>(R.id.tv_contact_name)
                val rl_job = convertView.findViewById<RelativeLayout>(R.id.rl_job)
                val tv_job_name = convertView.findViewById<TextView>(R.id.tv_job_name)
                val et_phone_name = convertView.findViewById<EditText>(R.id.et_phone_name)
                val tv_phone_name = convertView.findViewById<TextView>(R.id.tv_phone_name)
                val et_card_name = convertView.findViewById<EditText>(R.id.et_card_name)
                val tv_card_name = convertView.findViewById<TextView>(R.id.tv_card_name)
                val et_email_name = convertView.findViewById<EditText>(R.id.et_email_name)
                val tv_email_name = convertView.findViewById<TextView>(R.id.tv_email_name)
                override fun setData(view: View, data: NewProjectInfo.RelateInfo, position: Int) {
                    if (editAble) {
                        setModuleEnable(true)
                        rl_job.setOnClickListener {
                            //选择职位
                            Utils.forceCloseInput(this@NewOriginProjectActivity,et_contact_name)
                            val experience = resources.getStringArray(R.array.job).toList()
                            val position = experience.indices.firstOrNull { experience[it].contains(tv_job_name.text) }
                                    ?: 0
                            val pvOptions = OptionsPickerBuilder(this@NewOriginProjectActivity,
                                    OnOptionsSelectListener { options1, option2, options3, v ->
                                        tv_job_name.text = experience[options1]
                                    }).setDecorView(window.decorView.find(android.R.id.content)).build<String>()
                            pvOptions.setPicker(experience)
                            pvOptions.setSelectOptions(position)
                            pvOptions.show()
                        }
                    } else {
                        setModuleEnable(false)
                    }
                    if (null != data) {
                        fitData(data)
                    }
                    nameMap.put(position, et_contact_name)
                    jobMap.put(position, tv_job_name)
                    phoneMap.put(position, et_phone_name)
                    idCardMap.put(position, et_card_name)
                    emailMap.put(position, et_email_name)
                }

                private fun fitData(data: NewProjectInfo.RelateInfo) {
                    if (null != data.name) {
                        et_contact_name.setText(data.name)
                        tv_contact_name.text = data.name
                    }
                    if (null != data.position) {
                        tv_job_name.text = data.position
                    }
                    if (null != data.phone) {
                        et_phone_name.setText(data.phone)
                        tv_phone_name.text = data.phone
                    }
                    if (null != data.idCard) {
                        et_card_name.setText(data.idCard)
                        tv_card_name.text = data.idCard
                    }
                    if (null != data.email) {
                        et_email_name.setText(data.email)
                        tv_email_name.text = data.email
                    }
                }

                private fun setModuleEnable(enable: Boolean) {
                    if (enable) {
                        et_contact_name.visibility = View.VISIBLE
                        et_phone_name.visibility = View.VISIBLE
                        et_card_name.visibility = View.VISIBLE
                        et_email_name.visibility = View.VISIBLE

                        tv_contact_name.visibility = View.INVISIBLE
                        tv_phone_name.visibility = View.INVISIBLE
                        tv_card_name.visibility = View.INVISIBLE
                        tv_email_name.visibility = View.INVISIBLE
                    } else {
                        et_contact_name.visibility = View.INVISIBLE
                        et_phone_name.visibility = View.INVISIBLE
                        et_card_name.visibility = View.INVISIBLE
                        et_email_name.visibility = View.INVISIBLE

                        tv_contact_name.visibility = View.VISIBLE
                        tv_phone_name.visibility = View.VISIBLE
                        tv_card_name.visibility = View.VISIBLE
                        tv_email_name.visibility = View.VISIBLE
                    }

                }
            }
        })

        recycler_view.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                Utils.dip2px(this, 8f), Color.parseColor("#f7f9fc")))
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = mRelateAdapter

    }

    private fun setLoadding() {
        view_recover.visibility = View.VISIBLE
    }

    override fun goneLoadding() {
        view_recover.visibility = View.INVISIBLE
    }

    private fun initLocalData() {
        if (null != project) {
            if (null != project!!.name) {
                tv_name_all.text = project!!.name
            }

            if (null != project!!.shortName) {
                tv_name_simple.text = project!!.shortName
            }

            if (null != project!!.creditCode){
                tv_credit_code.text = project!!.creditCode
            }
            if (null != presenter && null != project!!.company_id) {
                setLoadding()
                company_id = project!!.company_id!!
                presenter!!.getOriginProRequest(project!!.company_id!!)
            }
        }
    }

    private fun bindListener() {
        rl_name_all.setOnClickListener {
            if (!editAble) return@setOnClickListener
            //公司名称
            ll_search.visibility = View.VISIBLE
            Utils.showInput(context, search_view.et_search)

            search_view.et_search.setText(tv_name_all.text.toString())
            if (tv_name_all.text.length <= 10) {
                search_view.et_search.setSelection(tv_name_all.text.length)
            }
            search_view.et_search.imeOptions = EditorInfo.IME_ACTION_DONE
            search_view.et_search.setSingleLine(true)
            search_view.et_search.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ll_search.visibility = View.GONE
                    tv_name_all.text = search_view.et_search.text
                    true
                }
                false
            }

            search_view.onTextChange = { text ->
                if (!TextUtils.isEmpty(text)) {
                    handler.postDelayed({ doSearch(text) }, 500)
                } else {
                    mCompanyAdapter.dataList.clear()
                    mCompanyAdapter.notifyDataSetChanged()
                }
            }

            search_view.tv_cancel.setOnClickListener {
                search_view.search = ""
                mCompanyAdapter.dataList.clear()
                mCompanyAdapter.notifyDataSetChanged()
                Utils.closeInput(context, search_view.et_search)
                ll_search.visibility = View.GONE
            }
        }

        rl_name_simple.setOnClickListener {
            //公司简介
            if (editAble) {
                FormActivity.start(context, "公司简介", tv_name_simple.text.toString(), SIMPLE_NAME)
            }
        }

        rl_pm.setOnClickListener {
            if (!editAble) return@setOnClickListener
            //项目经理
            var bean = UserBean()
            bean.user_id = chargerPmId
            bean.uid = chargerPmId
            startActivityForResult<MemberSelectActivity>(FUZEREN_PM, Extras.FLAG to true, Extras.TITLE to "请选择项目经理", Extras.LIST to Arrays.asList(bean))
        }

        rl_pl.setOnClickListener {
            if (!editAble) return@setOnClickListener
            //项目负责人
            var bean = UserBean()
            bean.user_id = chargerPlId
            bean.uid = chargerPlId
            startActivityForResult<MemberSelectActivity>(FUZEREN_PL, Extras.FLAG to true, Extras.TITLE to "请选择项目负责人", Extras.LIST to Arrays.asList(bean))
        }

        tv_add_contact.setOnClickListener {
            if (!editAble) return@setOnClickListener
            //添加联系人
            addContactView()
        }

        tv_edit.clickWithTrigger {
            //编辑
            editAble = true
            et_business_brief.visibility = View.VISIBLE
            tv_business_brief.visibility = View.INVISIBLE
            mRelateAdapter.notifyDataSetChanged()

            tv_edit.visibility = View.INVISIBLE
            ll_create.visibility = View.VISIBLE
            tv_add_contact.visibility = View.VISIBLE
        }

        mCompanyAdapter.onItemClick = { v, p ->
            val data = mCompanyAdapter.dataList.get(p)
            tv_name_all.text = data.name

            search_view.search = ""
            mCompanyAdapter.dataList.clear()
            mCompanyAdapter.notifyDataSetChanged()
            Utils.closeInput(context, search_view.et_search)
            ll_search.visibility = View.GONE
        }

        tv_credit_code.setOnClickListener {
            //统一信用代码
            if (editAble) {
                FormActivity.start(context, "统一信用代码", tv_credit_code.text.toString(), CREDIT_CODE)
            }
        }

        ll_create.clickWithTrigger {
            createProjectBuild()
        }
    }

    private fun createProjectBuild() {
        if (tv_name_all.textStr.isNullOrEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "公司名称不能为空")
            return
        } else if (tv_pm_name.textStr.isNullOrEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "项目经理不能为空")
            return
        } else if (tv_pl_name.textStr.isNullOrEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "项目负责人不能为空")
            return
        }
        val relateInfos = ArrayList<NewProjectInfo.RelateInfo>()
        val dataList = mRelateAdapter.dataList
        for (i in dataList.indices) {
            val info = NewProjectInfo.RelateInfo()
//            info.position_id = dataList[i].position_id
            info.id = dataList[i].id
            for ((k, v) in nameMap) {
                if (k == i) {
                    info.name = v.textStr
                }
            }
            for ((k, v) in jobMap) {
                if (k == i) {
                    if (!"请选择".equals(v.textStr)){
                        info.position = v.textStr
                    }
                }
            }

            for ((k, v) in phoneMap) {
                if (k == i) {
                    info.phone = v.textStr
                }
                if (!v.textStr.isNullOrEmpty() && !Utils.isMobileExact(v.textStr)) {
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的手机号")
                    return
                }
            }

            for ((k, v) in idCardMap) {
                if (k == i) {
                    info.idCard = v.textStr
                }
                if (!v.textStr.isNullOrEmpty() && !Utils.isIDCard18(v.textStr)) {
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的身份证号")
                    return
                }
            }

            for ((k, v) in emailMap) {
                if (k == i) {
                    info.email = v.textStr
                }
                if (!v.textStr.isNullOrEmpty() && !Utils.isEmail(v.textStr)) {
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的邮箱")
                    return
                }

            }
            if (!info.name.isNullOrEmpty() || !info.position.isNullOrEmpty() || !info.phone.isNullOrEmpty()
                    || !info.idCard.isNullOrEmpty() || !info.email.isNullOrEmpty()) {
                relateInfos.add(info)
            }
        }
        val code = tv_credit_code.textStr
        if (!code.isNullOrEmpty() && !Utils.isCreditCode(code)){
            showCustomToast(R.drawable.icon_toast_common, "请填写正确的信用代码")
            return
        }
        val map = HashMap<String, Any>()
        map.put("cName", tv_name_all.textStr)
        map.put("shortName", tv_name_simple.textStr)
        map.put("info", et_business_brief.textStr)
        map.put("principal", chargerPmId)
        map.put("leader", chargerPlId)
        map.put("type", 2)
        map.put("creditCode", code)
        map.put("company_id", company_id)
        map.put("relate", relateInfos)
        if (null != presenter) {
            presenter!!.createProjectBuild(map)
        }
    }

    private fun addContactView() {
        val dataList = mRelateAdapter.dataList
        dataList.add(NewProjectInfo.RelateInfo())
        mRelateAdapter.dataList = dataList
        mRelateAdapter.notifyDataSetChanged()
    }

    fun doSearch(text: String) {
        var map = HashMap<String, String>()
        map.put("name", text)
        recycler_result.visibility = View.VISIBLE
        iv_empty.visibility = View.GONE
        SoguApi.getService(application, ProjectService::class.java)
                .searchCompany(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        mCompanyAdapter.dataList.clear()

                        var bean = CompanySelectBean()
                        bean.name = search_view.search
                        mCompanyAdapter.dataList.add(bean)

                        payload?.payload?.apply {
                            mCompanyAdapter.dataList.addAll(this)
                        }
                        mCompanyAdapter.notifyDataSetChanged()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                    if (mCompanyAdapter.dataList.size == 0) {
                        recycler_result.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                }, { e ->
                    Trace.e(e)
                    if (mCompanyAdapter.dataList.size == 0) {
                        recycler_result.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data) {
            when (requestCode) {
                SIMPLE_NAME -> tv_name_simple.text = data.getStringExtra(Extras.DATA)
                CREDIT_CODE -> tv_credit_code.text = data.getStringExtra(Extras.DATA)
                FUZEREN_PM -> {
                    //项目经理
                    var userList = data.getSerializableExtra(Extras.DATA) as java.util.ArrayList<UserBean>
                    if (!userList.isNullOrEmpty()) {
                        chargerPmId = userList[0].uid!!
                        tv_pm_name.text = userList[0].name
                    }
                }

                FUZEREN_PL -> {
                    //项目负责人
                    var userList = data.getSerializableExtra(Extras.DATA) as java.util.ArrayList<UserBean>
                    if (!userList.isNullOrEmpty()) {
                        chargerPlId = userList[0].uid!!
                        tv_pl_name.text = userList[0].name
                    }
                }
            }
        }
    }

    override fun setProjectOriginData(data: NewProjectInfo) {

        tv_name_all.text = data.name
        tv_name_simple.text = data.shortName
        if (!data.creditCode.isNullOrEmpty()){
            tv_credit_code.text = data.creditCode
        }
        if (null != data.duty) {
            tv_pm_name.text = data.duty!!.name
            if (null != data.duty!!.principal) {
                chargerPmId = data.duty!!.principal!!
            }
        }

        if (null != data.lead) {
            tv_pl_name.text = data.lead!!.name
            if (null != data.lead!!.leader) {
                chargerPlId = data.lead!!.leader!!
            }
        }
        if (null != user && null != user!!.uid) {
            if (user!!.uid == chargerPmId || user!!.uid == chargerPlId) {
                tv_edit.visibility = View.VISIBLE
                tv_edit.text = "编辑"
            } else {
                tv_edit.visibility = View.GONE
            }
        }
        if (null != data.info) {
            et_business_brief.setText(data.info)
            tv_business_brief.text = data.info
        }

        if (null != data.relate && data.relate!!.size > 0) {
            relateInfos.clear()
            relateInfos = (data.relate as ArrayList<NewProjectInfo.RelateInfo>?)!!
            mRelateAdapter.dataList.clear()
            mRelateAdapter.dataList.addAll(relateInfos)
            mRelateAdapter.notifyDataSetChanged()
        } else {
            mRelateAdapter.dataList.clear()
            mRelateAdapter.dataList.add(NewProjectInfo.RelateInfo())
            mRelateAdapter.notifyDataSetChanged()
        }
    }


    override fun setCreateOriginSuccess() {
        showSuccessToast("创建成功")
        finish()
    }

}