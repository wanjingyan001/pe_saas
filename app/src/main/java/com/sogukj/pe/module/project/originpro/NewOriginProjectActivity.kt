package com.sogukj.pe.module.project.originpro

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bigkoo.pickerview.OptionsPickerView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CompanySelectBean
import com.sogukj.pe.module.user.FormActivity
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_project.*
import kotlinx.android.synthetic.main.layout_pro_contact.*
import kotlinx.android.synthetic.main.layout_pro_top.*
import org.jetbrains.anko.startActivity
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * Created by CH-ZH on 2018/9/17.
 * 新建项目
 */
class NewOriginProjectActivity : ToolbarActivity() {
    lateinit var mCompanyAdapter: RecyclerAdapter<CompanySelectBean>
    private val SIMPLE_NAME = 0x111
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
    }

    private fun initData() {
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
            tv_name_all.text = data.name

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

    }

    private fun bindListener() {
        rl_name_all.setOnClickListener {
            //公司名称
            ll_search.visibility = View.VISIBLE
            Utils.showInput(context, search_view.et_search)

            search_view.et_search.setText(tv_name_all.text.toString())
            search_view.et_search.setSelection(tv_name_all.text.length)
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
            FormActivity.start(context, "公司简介", tv_name_simple.text.toString(), SIMPLE_NAME)
        }

        rl_pm.setOnClickListener {
            //项目经理
        }

        rl_pl.setOnClickListener {
            //项目负责人
        }

        rl_job.setOnClickListener {
            //选择职位
            val experience = ArrayList<String>()
            for (i in 0 .. 10){
                experience.add("职位" + i)
            }
            val position = experience.indices.firstOrNull { experience[it].contains(tv_job_name.text) } ?: 0
            val pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                tv_job_name.text = experience[options1]
            }).build()
            pvOptions.setPicker(experience)
            pvOptions.setSelectOptions(position)
            pvOptions.show()
        }
        tv_add_contact.setOnClickListener {
            //添加联系人
            addContactView()
        }

        ll_create.setOnClickListener {
            startActivity<ProjectApprovalActivity>()
        }
    }

    private fun addContactView() {
        val contactView = View.inflate(this,R.layout.layout_add_contact,null)
        val et_contact_name = contactView.findViewById<EditText>(R.id.et_contact_name)
        val rl_job = contactView.findViewById<RelativeLayout>(R.id.rl_job)
        val tv_job_name = contactView.findViewById<TextView>(R.id.tv_job_name)
        val et_phone_name = contactView.findViewById<EditText>(R.id.et_phone_name)
        val et_card_name = contactView.findViewById<EditText>(R.id.et_card_name)
        val et_email_name = contactView.findViewById<EditText>(R.id.et_email_name)

        rl_job.setOnClickListener {
            //选择职位
            val experience = ArrayList<String>()
            for (i in 0 .. 10){
                experience.add("职位" + i)
            }
            val position = experience.indices.firstOrNull { experience[it].contains(tv_job_name.text) } ?: 0
            val pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                tv_job_name.text = experience[options1]
            }).build()
            pvOptions.setPicker(experience)
            pvOptions.setSelectOptions(position)
            pvOptions.show()
        }
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = Utils.dip2px(this,15f)
        contactView.layoutParams = params
        ll_add_contact.addView(contactView)
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
        if (null != data){
            when(requestCode){
                SIMPLE_NAME -> tv_name_simple.text = data.getStringExtra(Extras.DATA)
            }
        }
    }

}