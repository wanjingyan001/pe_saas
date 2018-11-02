package com.sogukj.pe.module.register.presenter

import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.RegisterVerResult
import com.sogukj.pe.bean.UserBean

/**
 * Created by admin on 2018/10/29.
 */
interface LoginView {
    fun getCodeStart()
    fun getCodeSuccess(phone:String)
    fun verificationCodeSuccess(result: RegisterVerResult)
    fun verificationCodeFail()
    fun getUserBean(user:UserBean)
    fun getCompanyInfoSuccess(info: MechanismBasicInfo)
    fun getInfoFinish()
}