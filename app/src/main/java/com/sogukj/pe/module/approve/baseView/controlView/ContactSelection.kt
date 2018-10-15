package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.util.AttributeSet
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.approve.ApproveInitiateActivity
import com.sogukj.pe.module.approve.SelectionActivity
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.register.MemberSelectActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_control_contact_selection.view.*
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

/**
 * 联系人选择
 * Created by admin on 2018/10/8.
 */
class ContactSelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private val alreadySelected = mutableListOf<ApproveValueBean>()
    override fun getContentResId(): Int = R.layout.layout_control_contact_selection

    override fun bindContentView() {
        hasInit.yes {
            controlBean.value?.let { values ->
                values.isNotEmpty().yes {
                    val beans = mutableListOf<ApproveValueBean>()
                    values.forEach { map ->
                        val treeMap = map as LinkedTreeMap<String, Any>
                        beans.add(ApproveValueBean(name = treeMap["name"] as String,
                                id = treeMap["id"] as String))
                    }
                    controlBean.value?.clear()
                    controlBean.value?.addAll(beans)
                    setSelectedContact(controlBean.value as MutableList<ApproveValueBean>)
                }.otherWise {
                    inflate.contactSelectTv.hint = "请选择"
                    inflate.contactSelectTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                }

                inflate.contactSelectTv.clickWithTrigger {
                    AvoidOnResult(activity)
                            .startForResult<SelectionActivity>(Extras.REQUESTCODE,
                                    Extras.LIST to alreadySelected,
                                    Extras.FLAG to controlBean.is_multiple,
                                    Extras.TYPE to controlBean.skip!![0].skip_site)
                            .filter { it.resultCode == Extras.RESULTCODE }
                            .flatMap {
                                val selected = it.data.getSerializableExtra(Extras.BEAN) as ArrayList<ApproveValueBean>
                                Observable.just(selected)
                            }.subscribe {
                        it.isNotEmpty().yes {
                            setSelectedContact(controlBean.value as MutableList<ApproveValueBean>)
                        }
                    }
                }
            }
        }
    }

    private fun setSelectedContact(users: MutableList<ApproveValueBean>) {
        alreadySelected.clear()
        alreadySelected.addAll(users)
        inflate.contactSelectTv.text = users.joinToString(",") { userBean -> userBean.name }
        inflate.contactSelectTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }
}