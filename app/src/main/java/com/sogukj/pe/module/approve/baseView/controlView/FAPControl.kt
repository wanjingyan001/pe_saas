package com.sogukj.pe.module.approve.baseView.controlView

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.module.approve.SelectionActivity
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_control_department_selection.view.*
import kotlinx.android.synthetic.main.layout_control_fund_and_project.view.*

/**
 * 基金项目关联
 * Created by admin on 2018/10/9.
 */
class FAPControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    var block: ((fundId: Int?, projectId: Int?) -> Unit)? = null
    val refresh by lazy { controlBean.is_fresh == true }

    override fun getContentResId(): Int = R.layout.layout_control_fund_and_project

    override fun bindContentView() {
        hasInit.yes {
            inflate.star1.setVisible(controlBean.is_must_fund != false)
            inflate.star2.setVisible(controlBean.is_must_pro == true)

            inflate.fundSelectionTitle.text = controlBean.name1
            controlBean.value?.let { values ->
                values.isNotEmpty().yes {
                    val beans = mutableListOf<ApproveValueBean>()
                    values.forEach { map ->
                        val treeMap = map as LinkedTreeMap<*, *>
                        beans.add(ApproveValueBean(name = treeMap["name"] as String, id = (treeMap["id"] as? Number)?.toInt()))
                    }
                    controlBean.value?.clear()
                    controlBean.value?.addAll(beans)
                }
            }
            controlBean.value.isNullOrEmpty().no {
                inflate.fundTv.text = (controlBean.value!![0] as ApproveValueBean).name
                inflate.fundTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }.otherWise {
                inflate.fundTv.text = controlBean.placeholder
                inflate.fundTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
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
                            controlBean.value?.let { values ->
                                values.isNotEmpty().yes {
                                    values[0] = it
                                }.otherWise {
                                    values.add(it)
                                }
                                if (values.size > 1) {
                                    values.removeAt(1)
                                }
                                if (inflate.projectTv.text.isNotEmpty()){
                                    inflate.projectTv.text = ""
                                }
                            }
                            inflate.fundTv.text = it.name
                            if (block != null && refresh) {
                                block?.invoke(it.id, null)
                            }
                        }
            }

            inflate.projectSelectionTitle.text = controlBean.name2
            (controlBean.value != null && controlBean.value!!.size > 1).yes {
                inflate.projectTv.text = (controlBean.value!![1] as ApproveValueBean).name
                inflate.projectTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }.otherWise {
                inflate.projectTv.text = controlBean.placeholder
                inflate.projectTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
            }
            inflate.projectTv.clickWithTrigger {
                if (controlBean.value?.isEmpty() == true) {
                    (activity as BaseActivity).showErrorToast("请先选择基金")
                } else {
                    val fundId = (controlBean.value!![0] as ApproveValueBean).id
                    AvoidOnResult(activity)
                            .startForResult<SelectionActivity>(Extras.REQUESTCODE,
                                    Extras.TYPE to controlBean.skip!![1].skip_site,
                                    Extras.ID to fundId)
                            .filter { it.resultCode == Activity.RESULT_OK }
                            .flatMap {
                                val extra = it.data.getSerializableExtra(Extras.BEAN)
                                Observable.just(extra as ApproveValueBean)
                            }.subscribe {
                                controlBean.value?.let { values ->
                                    (values.size > 1).yes {
                                        values[1] = it
                                    }.otherWise {
                                        values.add(it)
                                    }
                                }
                                inflate.projectTv.text = it.name
                                if (block != null && refresh) {
                                    block?.invoke(fundId, it.id)
                                }
                            }
                }
            }
        }
    }
}