package com.sogukj.pe.module.approve.baseView

import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean

/**
 * 工厂接口
 * Created by admin on 2018/9/27.
 */
interface FactoryInterface {
    fun <T : BaseControl> createControl(clazz: Class<T>,controlBean: ControlBean): BaseControl
}