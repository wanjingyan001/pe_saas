package com.sogukj.pe.module.fund

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.service.FundService
import com.sogukj.pe.widgets.CalendarDingDing
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_edit.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FundEditActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<UserBean>

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        menuMark.title = "保存"
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {

                var map = HashMap<String, Any?>()
                map.put("id", data.id)
                //map.put("administrator", administrator.text.toString())
                map.put("regTime", regTime.text.toString())

                try {
                    var size1 = contributeSize.text.toString().toDouble()
                    map.put("contributeSize", size1)
                } catch (e: Exception) {
                    map.put("contributeSize", 0)
                }

                try {
                    var size2 = actualSize.text.toString().toDouble()
                    map.put("actualSize", size2)
                } catch (e: Exception) {
                    map.put("actualSize", 0)
                }

                map.put("duration", duration.text.toString())
                map.put("partners", partners.text.toString())
                map.put("mode", mode.text.toString())
                map.put("commission", commission.text.toString())

                try {
                    var size3 = manageFees.text.toString().toDouble()
                    map.put("manageFees", size3)
                } catch (e: Exception) {
                    map.put("manageFees", 0)
                }

                map.put("carry", carry.text.toString())

                var id_list = ArrayList<Int>()
                var list = ArrayList<UserBean>(adapter.dataList)
                list.removeAt(list.size - 1)
                list.mapTo(id_list) { it.uid!! }

                map.put("list", id_list)

                var builder = HashMap<String, Any?>()
                builder.put("ae", map)

                SoguApi.getService(application,FundService::class.java)
                        .editFundInfo(builder)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showCustomToast(R.drawable.icon_toast_success, "保存成功")
                                handler.postDelayed({
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }, 2000)
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
                            showCustomToast(R.drawable.icon_toast_fail, "保存失败")
                        })
            }
        }
        return false
    }

    lateinit var data: FundSmallBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_edit)
        data = intent.getSerializableExtra(Extras.DATA) as FundSmallBean
        setBack(true)
        title = data.fundName
        run {
            adapter = RecyclerAdapter(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_fund_detail_name_list, parent)
                object : RecyclerHolder<UserBean>(convertView) {
                    val headImg = convertView.find<CircleImageView>(R.id.commissionHeadImg)
                    val commissionName = convertView.find<TextView>(R.id.commissionName)
                    override fun setData(view: View, data: UserBean, position: Int) {
                        if (data.uid == null) {
                            commissionName.text = "添加"
                            headImg.setImageResource(R.drawable.send_add)
                        } else {
                            commissionName.text = data.name
//                            if (data.url.isNullOrEmpty()) {
//                                headImg.setImageResource(R.drawable.nim_avatar_default)
//                            } else {
//                                Glide.with(this@FundEditActivity)
//                                        .load(data.url)
//                                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
//                                        .into(headImg)
//                            }
                            if (data.url.isNullOrEmpty()) {
                                val ch = data.name.first()
                                headImg.setChar(ch)
                            } else {
                                Glide.with(context)
                                        .load(MyGlideUrl(data.url))
                                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                                        .into(headImg)
                            }
                        }
                    }
                }
            })
            val manager = LinearLayoutManager(this)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            commissionList.layoutManager = manager
            commissionList.adapter = adapter
        }

        adapter.onItemClick = { v, p ->
            var bean = adapter.dataList.get(p)
            if (bean.uid == null) {
                var list = ArrayList<UserBean>(adapter.dataList)
                list.removeAt(adapter.dataList.size - 1)
                ContactsActivity.start(this, list, true, false, SELECT)
            } else {
                adapter.dataList.remove(bean)
                adapter.notifyDataSetChanged()
            }
        }

        getFundDetail(data.id)

        mCalendar = CalendarDingDing(context)
        regTime.setOnClickListener {
            var date: Date? = null
            date = try {
                SimpleDateFormat("yyyy-MM-dd").parse(regTime.text.toString())
            } catch (e: Exception) {
                Date()
            }
            if (date == null) {
                date = Date()
            }
            mCalendar.show(1, date) { date ->
                if (date == null) {

                } else {
                    regTime.text = SimpleDateFormat("yyyy-MM-dd").format(date)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT && resultCode == Extras.RESULTCODE) {
            var beanObj = data?.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            adapter.dataList.clear()
            adapter.dataList.addAll(beanObj)

            var bean = UserBean()
            bean.uid = null
            adapter.dataList.add(bean)

            adapter.notifyDataSetChanged()
        }
    }

    private lateinit var mCalendar: CalendarDingDing

    var SELECT = 0x008

    private fun isZero(input: String? = null): String {
        val retStr: String
        retStr = if (input == null) {
            ""
        } else {
            try {
                val inputStr = input.toDouble().toString()
                if (inputStr == "0" || inputStr == "0.0" || inputStr == "0.00") {
                    ""
                } else {
                    inputStr
                }
            } catch (e: Exception) {
                ""
            }
        }
        return retStr
    }

    private fun getFundDetail(fundId: Int) {
        SoguApi.getService(application,FundService::class.java)
                .getFundDetail(fundId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            Log.d(FundDetailActivity.TAG, Gson().toJson(this))
                            find<TextView>(R.id.administrator).text = administrator
                            find<TextView>(R.id.regTime).text = regTime
                            find<EditText>(R.id.contributeSize).setText(isZero(contributeSize))
                            find<EditText>(R.id.actualSize).setText(isZero(actualSize))
                            find<EditText>(R.id.duration).setText(duration)
                            find<EditText>(R.id.partners).setText(partners)
                            find<EditText>(R.id.mode).setText(mode)
                            find<EditText>(R.id.commission).setText(commission)
                            find<EditText>(R.id.manageFees).setText(isZero(manageFees))
                            find<EditText>(R.id.carry).setText(isZero(carry))

//                            if (contributeSize.isNullOrEmpty()) {
//                                find<EditText>(R.id.contributeSize).setSelection(0)
//                            } else {
//                                find<EditText>(R.id.contributeSize).setSelection(contributeSize!!.length)
//                            }
                            list?.let {
                                adapter.dataList.addAll(it)

                                var bean = UserBean()
                                bean.uid = null
                                adapter.dataList.add(bean)

                                adapter.notifyDataSetChanged()
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }
}
