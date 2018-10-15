package com.sogukj.pe.module.approve.baseView

import android.support.v4.app.FragmentActivity
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean

/**
 * Created by admin on 2018/9/27.
 */
interface FactoryInterface {
    fun <T : BaseControl> createControl(clazz: Class<T>,controlBean: ControlBean): BaseControl
}