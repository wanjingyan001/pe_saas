package com.sogukj.pe.module.approve.baseView.controlView

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.module.approve.SelectionActivity
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_control_document_associate.view.*

/**
 * 关联审批单
 * Created by admin on 2018/10/10.
 */
class DocumentAssociate @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    override fun getContentResId(): Int = R.layout.layout_control_document_associate

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.associateTitle.text = controlBean.name
            controlBean.value?.let { values ->
                values.isNotEmpty().yes {
                    val beans = mutableListOf<ApproveValueBean>()
                    values.forEach { map ->
                        val treeMap = map as LinkedTreeMap<*, *>
                        beans.add(ApproveValueBean(name = "",
                                title = treeMap["title"] as String,
                                number = treeMap["number"] as String,
                                add_time = treeMap["add_time"] as String,
                                id = (treeMap["id"] as? Number)?.toInt()))
                    }
                    controlBean.value?.clear()
                    controlBean.value?.addAll(beans)
                    inflate.associateTv.text = beans.joinToString(",") { "${it.title}${it.number}" }
                    inflate.associateTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }.otherWise {
                    inflate.associateTv.hint = controlBean.placeholder
                    inflate.associateTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                }
            }
            inflate.associateTv.clickWithTrigger {
                AvoidOnResult(activity)
                        .startForResult<SelectionActivity>(Extras.REQUESTCODE, Extras.TYPE to controlBean.skip!![0].skip_site)
                        .filter { it.resultCode == Activity.RESULT_OK }
                        .flatMap {
                            val extra = it.data.getSerializableExtra(Extras.BEAN)
                            Observable.just(extra as ApproveValueBean)
                        }.subscribe {
                    controlBean.value?.clear()
                    controlBean.value?.add(it)
                    inflate.associateTv.text = "${it.title}${it.number}"
                }
            }
        }
    }
}