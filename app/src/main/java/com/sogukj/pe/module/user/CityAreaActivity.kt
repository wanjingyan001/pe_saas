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
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_city_area.*
import kotlinx.android.synthetic.main.layout_shareholder_toolbar.*
import org.jetbrains.anko.find

class CityAreaActivity : BaseActivity(), View.OnClickListener {

    companion object {
        val TAG = CityAreaActivity::class.java.simpleName
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, CityAreaActivity::class.java)
            ctx?.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    var selectPosition: Int? = null
    lateinit var areaAdpater: RecyclerAdapter<CityArea>
    lateinit var cityAdapter: RecyclerAdapter<CityArea.City?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_area)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "城市列表"
        addTv.text = "完成"
        addTv.setOnClickListener(this)
        back.setOnClickListener(this)
        areaAdpater = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_city_area_list, parent)
            object : RecyclerHolder<CityArea>(convertView) {
                val layout = convertView.find<LinearLayout>(R.id.item_layout)
                val tv = convertView.find<TextView>(R.id.city_name)
                override fun setData(view: View, data: CityArea, position: Int) {
                    tv.text = data.name
                    layout.isSelected = data.seclected
                }
            }
        })
        areaAdpater.onItemClick = { v, position ->
            val data = areaAdpater.dataList[position]
            areaAdpater.dataList.forEachIndexed { index, cityArea ->
                if (index == position) {
                    areaAdpater.dataList[position].seclected = true
                } else {
                    areaAdpater.dataList[index].seclected = false
                }
            }
            areaAdpater.notifyDataSetChanged()
            cityAdapter.dataList.clear()
            data.city?.let {
                cityAdapter.dataList.addAll(it)
                cityAdapter.notifyDataSetChanged()
            }

        }
        areaList.layoutManager = LinearLayoutManager(this)
        areaList.adapter = areaAdpater

        cityAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_city_area_list2, parent)
            object : RecyclerHolder<CityArea.City?>(convertView) {
                val tv = convertView.find<TextView>(R.id.city_name)
                override fun setData(view: View, data: CityArea.City?, position: Int) {
                    data?.let {
                        tv.text = data.name
                        tv.isSelected = data.seclected
                    }
                }
            }
        })
        cityAdapter.onItemClick = { v, position ->
            selectPosition = position
            cityAdapter.dataList.forEachIndexed { index, city ->
                if (index == position) {
                    cityAdapter.dataList[position]?.seclected = true
                } else {
                    cityAdapter.dataList[index]?.seclected = false
                }
            }

            cityAdapter.notifyDataSetChanged()
        }
        cityList.layoutManager = LinearLayoutManager(this)
        cityList.adapter = cityAdapter
        doRequest()
    }


    fun doRequest() {
        SoguApi.getService(application,UserService::class.java)
                .getCityArea()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    Log.d(TAG, Gson().toJson(payload))
                    if (payload.isOk) {
                        payload.payload?.let {
                            areaAdpater.dataList.addAll(it)
                            areaAdpater.notifyDataSetChanged()
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
                    val city = cityAdapter.dataList[selectPosition!!]
                    val intent = Intent()
                    intent.putExtra(Extras.DATA, city)
                    setResult(Extras.RESULTCODE3, intent)
                    finish()
                } else {
                    finish()
                }
            }
            R.id.back -> {
                onBackPressed()
            }
        }
    }
}
