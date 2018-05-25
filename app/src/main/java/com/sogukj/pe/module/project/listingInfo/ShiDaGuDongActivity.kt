package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.ListAdapter
import com.sogukj.pe.baselibrary.widgets.ListHolder
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ShareHolderBean
import com.sogukj.pe.bean.TimeGroupedShareHolderBean
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_share_holder.*
import org.jetbrains.anko.find

/**
 * Created by qinfei on 17/8/11.
 */
open class ShiDaGuDongActivity : ToolbarActivity() {

    open val type: Int
        get() = 1
    val adapter = ListAdapter { LocalHolder() }
    val adapterSelector = ListAdapter {
        object : ListHolder<TimeGroupedShareHolderBean> {
            lateinit var text: TextView
            override fun createView(inflater: LayoutInflater): View {
                text = inflater.inflate(R.layout.item_textview_selector, null) as TextView
                return text
            }

            override fun showData(convertView: View, position: Int, data: TimeGroupedShareHolderBean?) {
                text.text = data?.time
                if (position != selectedIndex)
                    text.setTextColor(resources.getColor(R.color.colorBlue))
                else
                    text.setTextColor(resources.getColor(R.color.text_2))
            }

        }
    }
    lateinit var project: ProjectBean
    var selectedIndex = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_share_holder)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "十大股东"


        list_view.adapter = adapter

        tv_previous.setOnClickListener {
            setGroup(selectedIndex - 1)
        }
        tv_next.setOnClickListener {
            setGroup(selectedIndex + 1)
        }
        tv_select.setOnClickListener { v ->
            showDropdown()
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    internal var popSelector: PopupWindow? = null
    fun showDropdown() {
        if (null == popSelector) {
            val popupView = View.inflate(this, R.layout.drop_down_selector, null)
            val lv_dropdown = popupView.find<ListView>(R.id.lv_dropdown)
            popSelector = PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, Utils.dpToPx(this,160), true)
            popSelector?.isTouchable = true
            popSelector?.isFocusable = true
            popSelector?.isOutsideTouchable = true
            popSelector?.setBackgroundDrawable(BitmapDrawable(resources, null as Bitmap?))

            lv_dropdown.adapter = adapterSelector
            lv_dropdown.setOnItemClickListener { parent, view, position, id ->
                val group = adapterSelector.dataList[position]
                group.apply { setGroup(position) }
                tv_select.isChecked = false
                popSelector?.dismiss()
            }
        }
        val location = IntArray(2)
        fl_content.getLocationInWindow(location)
        val x = location[0]
        val y = location[1]
        popSelector?.showAtLocation(fl_content, Gravity.NO_GRAVITY, location[0], location[1])
    }

    fun setGroup(index: Int = 0) {
        tv_previous.isEnabled = false
        tv_next.isEnabled = false
        val size = adapterSelector.dataList.size
        if (size <= 0) return
        if (index < 0) return
        val group = adapterSelector.dataList[index]
        selectedIndex = index
        if (size > 0 && selectedIndex < size - 1)
            tv_next.isEnabled = true
        if (selectedIndex > 0)
            tv_previous.isEnabled = true
        group.apply {
            isSelected = true
            tv_select.text = time
            tv_tenTotal.text = tenTotal
            tv_tenPercent.text = tenPercent
            tv_holdingChange.text = holdingChange
            adapterSelector.notifyDataSetChanged()
            data?.apply {
                adapter.dataList.clear()
                adapter.dataList.addAll(this)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun doRequest() {
        SoguApi.getService(application,ProjectService::class.java)
                .listTenShareHolders(project.company_id!!, shareholder_type = type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    adapterSelector.dataList.clear()
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapterSelector.dataList.clear()
                            adapterSelector.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    setGroup(0)
                    adapterSelector.notifyDataSetChanged()
                }, { e ->
                    Trace.e(e)
                })
    }


    class LocalHolder : ListHolder<ShareHolderBean> {

        lateinit var tvName: TextView
        lateinit var tvProportion: TextView
        lateinit var tvHoldingNum: TextView
        lateinit var tvShareType: TextView
        override fun createView(inflater: LayoutInflater): View {
            val view = inflater.inflate(R.layout.item_project_share_holder, null)

            tvName = view.findViewById<TextView>(R.id.tv_name) as TextView
            tvProportion = view.findViewById<TextView>(R.id.tv_proportion) as TextView
            tvHoldingNum = view.findViewById<TextView>(R.id.tv_holdingNum) as TextView
            tvShareType = view.findViewById<TextView>(R.id.tv_shareType) as TextView
            return view
        }

        override fun showData(convertView: View, position: Int, data: ShareHolderBean?) {
            tvName.text = data?.name
            tvProportion.text = data?.proportion
            tvHoldingNum.text = data?.holdingNum
            tvShareType.text = data?.shareType
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ShiDaGuDongActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
