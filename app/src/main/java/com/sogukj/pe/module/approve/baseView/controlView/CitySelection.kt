package com.sogukj.pe.module.approve.baseView.controlView

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.CityArea
import com.sogukj.pe.module.approve.NewDstCityActivity
import com.sogukj.pe.module.approve.SelectionActivity
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import com.sogukj.pe.module.approve.baseView.viewBean.AttachmentBean
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_control_city_selection.view.*

/**
 * 城市选择
 * Created by admin on 2018/10/9.
 */
class CitySelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    override fun getContentResId(): Int = R.layout.layout_control_city_selection

    @SuppressLint("SetTextI18n")
    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.citySelectionTitle.text = controlBean.name
            controlBean.value?.let { values ->
                values.isNotEmpty().yes {
                    val beans = mutableListOf<ApproveValueBean>()
                    values.forEach { map ->
                        val treeMap = map as LinkedTreeMap<*, *>
                        beans.add(ApproveValueBean(name = treeMap["name"] as String,
                                id = (treeMap["id"] as? Number)?.toInt()))
                    }
                    controlBean.value?.clear()
                    controlBean.value?.addAll(beans)
                    if (beans.size == 1) {
                        inflate.cityTv.text = beans[0].name
                    }else{
                        inflate.cityTv.text = "${beans.size}个"
                        inflate.cities.setVisible(true)
                        beans.forEachIndexed { index, valueBean ->
                            if (index < 3) {
                                val textView = inflate.cities.getChildAt(index) as TextView
                                textView.setVisible(true)
                                textView.text = valueBean.name
                            }
                        }
                    }
                }
            }
            inflate.citySelectionLayout.clickWithTrigger {
                AvoidOnResult(activity)
                        .startForResult<SelectionActivity>(Extras.REQUESTCODE,
                                Extras.TYPE to controlBean.skip!![0].skip_site,
                                Extras.FLAG to controlBean.is_multiple,
                                Extras.LIST to controlBean.value)
                        .filter { it.resultCode == Activity.RESULT_OK}
                        .flatMap {
                            val list = it.data.getSerializableExtra(Extras.BEAN) as ArrayList<ApproveValueBean>
                            Observable.just(list)
                        }.subscribe { values ->
                    values.isNotEmpty().yes {
                        controlBean.value?.clear()
                        controlBean.value?.addAll(values)
                        controlBean.is_multiple?.yes {
                            inflate.cityTv.text = "${values.size}个"
                            (0 until inflate.cities.childCount).forEach {
                                val textView = inflate.cities.getChildAt(it) as TextView
                                if (it < values.size) {
                                    inflate.cities.setVisible(true)
                                    textView.setVisible(true)
                                    textView.text = values[it].name
                                } else {
                                    textView.setVisible(false)
                                }
                            }
                        }?.otherWise {
                            inflate.cityTv.text = values[0].name
                        }
                    }
                }
            }
        }
    }
}