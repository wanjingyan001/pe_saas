package com.sogukj.pe.module.approve.baseView.controlView

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.module.approve.SelectionActivity
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_control_fund_selection.view.*

/**
 * 基金选择
 * Created by admin on 2018/10/9.
 */
class FundSelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    var block: ((value: ApproveValueBean) -> Unit)? = null
    val refresh by lazy { controlBean.is_fresh == true }

    override fun getContentResId(): Int = R.layout.layout_control_fund_selection

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.fundSelectionTitle.text = controlBean.name
            controlBean.value?.let { values ->
                values.isNotEmpty().yes {
                    val beans = mutableListOf<ApproveValueBean>()
                    values.forEach { map ->
                        val treeMap = map as LinkedTreeMap<*, *>
                        beans.add(ApproveValueBean(name = treeMap["name"] as String, id = (treeMap["id"] as? Number)?.toInt()))
                    }
                    controlBean.value?.clear()
                    controlBean.value?.addAll(beans)
                    inflate.fundTv.text = beans.joinToString(",") { it.name }
                    inflate.fundTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }.otherWise {
                    inflate.fundTv.hint = controlBean.placeholder
                    inflate.fundTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                }
            }
            inflate.fundTv.clickWithTrigger {
                AvoidOnResult(activity)
                        .startForResult<SelectionActivity>(Extras.REQUESTCODE,
                                Extras.TYPE to controlBean.skip!![0].skip_site)
                        .filter { it.resultCode == Activity.RESULT_OK }
                        .flatMap {
                            val extra = it.data.getSerializableExtra(Extras.BEAN)
                            Observable.just(extra as ApproveValueBean)
                        }.subscribe {
                            controlBean.value?.clear()
                            controlBean.value?.add(it)
                            inflate.fundTv.text = it.name
                            if (block != null && refresh) {
                                block?.invoke(it)
                            }
                        }
            }
        }
    }
}