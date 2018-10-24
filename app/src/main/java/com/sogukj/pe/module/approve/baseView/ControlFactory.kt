package com.sogukj.pe.module.approve.baseView

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.amap.api.mapcore.util.it
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean

@Suppress("UNCHECKED_CAST")
/**
 * 通过反射获取BaseControl子类的实例
 * Created by admin on 2018/9/27.
 */
class ControlFactory constructor(val activity: FragmentActivity)  : FactoryInterface {
    override fun <T : BaseControl> createControl(clazz: Class<T>, controlBean: ControlBean): BaseControl {
        val control = Class.forName(clazz.name).getDeclaredConstructor(Context::class.java).newInstance(activity) as BaseControl
        control.controlBean = controlBean
        control.activity = activity
        control.init()
        return control as T
    }
}