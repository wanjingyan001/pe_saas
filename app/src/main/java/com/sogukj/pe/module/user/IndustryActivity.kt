package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.Industry
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_industry.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find

class IndustryActivity : BaseActivity(), View.OnClickListener {


    companion object {
        val TAG = IndustryActivity::class.java.simpleName
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, IndustryActivity::class.java)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    var selectPosition: Int? = null
    lateinit var parentAdapter: RecyclerAdapter<Industry>
    lateinit var childrenAdapter: RecyclerAdapter<Industry.Children?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_industry)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "行业列表"
        addTv.text = "完成"
        addTv.setOnClickListener(this)
        back.setOnClickListener(this)

        parentAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_city_area_list, parent)
            object : RecyclerHolder<Industry>(convertView) {
                val layout = convertView.find<LinearLayout>(R.id.item_layout)
                val tv = convertView.find<TextView>(R.id.city_name)
                override fun setData(view: View, data: Industry, position: Int) {
                    tv.text = data.name
                    layout.isSelected = data.seclected
                }
            }
        })
        parentAdapter.onItemClick = { v, position ->
            val data = parentAdapter.dataList[position]
            parentAdapter.dataList.forEachIndexed { index, industry ->
                if (index == position) {
                    parentAdapter.dataList[position].seclected = true
                } else {
                    parentAdapter.dataList[index].seclected = false
                }
            }
            parentAdapter.notifyDataSetChanged()
            childrenAdapter.dataList.clear()
            data.children?.let {
                childrenAdapter.dataList.addAll(it)
                childrenAdapter.notifyDataSetChanged()
            }

        }
        parentList.layoutManager = LinearLayoutManager(this)
        parentList.adapter = parentAdapter

        childrenAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_city_area_list2, parent)
            object : RecyclerHolder<Industry.Children?>(convertView) {
                val tv = convertView.find<TextView>(R.id.city_name)
                override fun setData(view: View, data: Industry.Children?, position: Int) {
                    data?.let {
                        tv.text = data.name
                        tv.isSelected = data.seclected
                    }
                }
            }
        })
        childrenAdapter.onItemClick = { v, position ->
            selectPosition = position
            childrenAdapter.dataList.forEachIndexed { index, children ->
                if (index == position) {
                    childrenAdapter.dataList[position]?.seclected = true
                } else {
                    childrenAdapter.dataList[index]?.seclected = false
                }
            }
            childrenAdapter.notifyDataSetChanged()
        }
        childrenList.layoutManager = LinearLayoutManager(this)
        childrenList.adapter = childrenAdapter

        doRequest()
    }


    private fun doRequest() {
        SoguApi.getService(application,UserService::class.java)
                .industryCategory()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    Log.d(TAG, Gson().toJson(payload))
                    if (payload.isOk) {
                        payload.payload?.let {
                            parentAdapter.dataList.addAll(it)
                            parentAdapter.notifyDataSetChanged()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addTv -> {
                if (selectPosition != null) {
                    val industry = childrenAdapter.dataList[selectPosition!!]
                    val intent = Intent()
                    intent.putExtra(Extras.DATA, industry)
                    setResult(Extras.RESULTCODE, intent)
                    finish()
                }
            }
            R.id.back -> {
                onBackPressed()
            }
        }
    }
}
