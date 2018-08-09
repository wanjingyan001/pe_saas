package com.sogukj.pe.module.project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CompanySelectBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.register.MemberSelectActivity
import com.sogukj.pe.module.user.FormActivity
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.service.ProjectService
import com.sogukj.pe.widgets.PayView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_project.*
import org.jetbrains.anko.startActivityForResult
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectAddActivity : ToolbarActivity() {
    lateinit var mCompanyAdapter: RecyclerAdapter<CompanySelectBean>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
        setBack(true)

        var type = intent.getStringExtra(Extras.TYPE)
        if (type == "ADD") {
            var title = intent.getStringExtra(Extras.TITLE)
            if (title.contains("项目")) {
                setTitle("添加${title}")
            } else {
                setTitle("添加${title}项目")
            }
        } else if (type == "EDIT") {
            setTitle("编辑项目")
            var data = intent.getSerializableExtra(Extras.DATA) as ProjectBean
            SoguApi.getService(application, NewService::class.java)
                    .showProject(data.company_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                et_name.text = name
                                et_fuzeren.text = chargeName
                                chargerStr = chargeName ?: ""
                                chargerId = charge ?: 0
                                et_short_name.text = shortName
                                et_faren.text = legalPersonName
                                et_reg_address.text = regLocation
                                et_credit_code.text = creditCode
                                et_other.text = info
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }

                    }, { e ->
                        Trace.e(e)
                        ToastError(e)
                    })
        }
        btn_commit.setOnClickListener {
            if (type == "ADD") {
                doSave()
            } else if (type == "EDIT") {
                editSave()
            }
        }
        et_name.setOnClickListener {
            ll_search.visibility = View.VISIBLE
            Utils.showInput(context, search_view.et_search)

            search_view.et_search.setText(et_name.text.toString())
            search_view.et_search.setSelection(et_name.text.length)
            search_view.et_search.imeOptions = EditorInfo.IME_ACTION_DONE
            search_view.et_search.setSingleLine(true)
            search_view.et_search.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ll_search.visibility = View.GONE
                    et_name.text = search_view.et_search.text
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

        mCompanyAdapter = RecyclerAdapter<CompanySelectBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent) as View
            object : RecyclerHolder<CompanySelectBean>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                override fun setData(view: View, data: CompanySelectBean, position: Int) {
                    tv1.text = "${position + 1}." + data.name
                }
            }
        })
        mCompanyAdapter.onItemClick = { v, p ->
            val data = mCompanyAdapter.dataList.get(p)
            et_name.text = data.name

            search_view.search = ""
            mCompanyAdapter.dataList.clear()
            mCompanyAdapter.notifyDataSetChanged()
            Utils.closeInput(context, search_view.et_search)
            ll_search.visibility = View.GONE
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_result.layoutManager = layoutManager
        recycler_result.adapter = mCompanyAdapter

        et_short_name.setOnClickListener {
            FormActivity.start(context, "项目名称", et_short_name.text.toString(), SHORT_NAME)
        }
        et_faren.setOnClickListener {
            FormActivity.start(context, "法人代表", et_faren.text.toString(), FA_REN)
        }
        et_reg_address.setOnClickListener {
            FormActivity.start(context, "注册地点", et_reg_address.text.toString(), REG_ADDRESS)
        }
        et_credit_code.setOnClickListener {
            FormActivity.start(context, "统一信用代码", et_credit_code.text.toString(), CREDIT_CODE)
        }
        et_other.setOnClickListener {
            FormActivity.start(context, "其他信息", et_other.text.toString(), OTHER)
        }
        et_fuzeren.setOnClickListener {
            var bean = UserBean()
            bean.user_id = chargerId
            bean.uid = chargerId
            startActivityForResult<MemberSelectActivity>(FUZEREN, Extras.FLAG to true, Extras.TITLE to "请选择负责人", Extras.LIST to Arrays.asList(bean))
        }
    }

    var SHORT_NAME = 0x555
    var FA_REN = 0x556
    var REG_ADDRESS = 0x557
    var CREDIT_CODE = 0x558
    var OTHER = 0x559
    var FUZEREN = 0x560


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {
                SHORT_NAME -> et_short_name.text = data.getStringExtra(Extras.DATA)
                FA_REN -> et_faren.text = data.getStringExtra(Extras.DATA)
                REG_ADDRESS -> et_reg_address.text = data.getStringExtra(Extras.DATA)
                CREDIT_CODE -> et_credit_code.text = data.getStringExtra(Extras.DATA)
                OTHER -> {
                    et_other.text = data.getStringExtra(Extras.DATA)
                    et_other.viewTreeObserver.addOnGlobalLayoutListener {
                        if (et_other.layout.lineCount > 1) {
                            et_other.gravity = Gravity.LEFT
                        } else {
                            et_other.gravity = Gravity.RIGHT
                        }
                    }
                }
                FUZEREN -> {
                    var userList = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
                    chargerId = userList.get(0).uid ?: 0
                    chargerStr = userList.get(0).name ?: ""
                    et_fuzeren.text = chargerStr
                }
            }
        }
    }

    var chargerStr = ""
    var chargerId = 0

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

    private fun doSave() {
        val project = ProjectBean()
        project.name = et_name?.text?.trim()?.toString()
        if (project.name == null) return
        project.shortName = et_short_name?.text?.trim()?.toString()
        project.legalPersonName = et_faren?.text?.trim()?.toString()
        project.regLocation = et_reg_address?.text?.trim()?.toString()
        project.creditCode = et_credit_code?.text?.trim()?.toString()
        project.info = et_other?.text?.trim()?.toString()
        project.charge = chargerId
        project.chargeName = chargerStr
        SoguApi.getService(application, NewService::class.java)
                .addProject(name = project.name!!
                        , shortName = project.shortName!!
                        , creditCode = project.creditCode
                        , legalPersonName = project.legalPersonName
                        , regLocation = project.regLocation
                        , info = project.info
                        , charge = chargerId
                        , type = 6)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_success, "保存成功")
                        finish()
                    } else {
                        if (payload.message === "9527") {
                            val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                            if (permission != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 0x001)
                            } else {
                                SoguApi.getService(application, OtherService::class.java).getPayType()
                                        .execute {
                                            onNext { payload ->
                                                if (payload.isOk) {
                                                    payload.payload?.let {
                                                        val bean = it.find { it.mealName == "项目套餐" }
                                                        val pay = PayView(context, bean)
                                                        pay.show(2, bean?.tel)
                                                    }
                                                } else {
                                                    showErrorToast(payload.message)
                                                }
                                            }
                                        }
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }

                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "保存失败")
                })
    }

    private fun editSave() {
        var data = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        if (et_name?.text?.trim()?.toString().isNullOrEmpty()) return
        data.name = et_name?.text?.trim()?.toString()
        data.shortName = et_short_name?.text?.trim()?.toString()
        data.legalPersonName = et_faren?.text?.trim()?.toString()
        data.regLocation = et_reg_address?.text?.trim()?.toString()
        data.creditCode = et_credit_code?.text?.trim()?.toString()
        data.info = et_other?.text?.trim()?.toString()
        data.charge = chargerId
        data.chargeName = chargerStr
        SoguApi.getService(application, NewService::class.java)
                .addProject(company_id = data.company_id
                        , name = data.name!!
                        , shortName = data.shortName!!
                        , creditCode = data.creditCode
                        , legalPersonName = data.legalPersonName
                        , regLocation = data.regLocation
                        , info = data.info
                        , charge = chargerId
                        , type = 6)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_success, "保存成功")
                        finish()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }

                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "保存失败")
                })
    }

    companion object {
        fun startAdd(ctx: Activity?) {
            var intent = Intent(ctx, ProjectAddActivity::class.java)
            intent.putExtra(Extras.TYPE, "ADD")
            ctx?.startActivity(intent)
        }

        fun startEdit(ctx: Activity?, data: ProjectBean) {
            var intent = Intent(ctx, ProjectAddActivity::class.java)
            intent.putExtra(Extras.TYPE, "EDIT")
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }
}
