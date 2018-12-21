package com.sogukj.pe.module.register.presenter

import android.app.Application
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.sogukj.pe.Extras
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import me.jessyan.retrofiturlmanager.RetrofitUrlManager

/**
 *
 * Created by admin on 2018/10/29.
 */
class LoginPresenter constructor(val application: Application) {
    var loginView: LoginView? = null

    fun sendPhoneInput(phone: String) {
        SoguApi.getService(application, RegisterService::class.java).sendVerCode(phone)
                .execute {
                    onSubscribe {
                        loginView?.getCodeStart()
                    }
                    onNext { payload ->
                        if (payload.isOk) {
                            loginView?.getCodeSuccess(phone)
                        } else {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, application)
                        }
                    }
                }
    }

    fun verificationCode(phone: String, verCode: String) {
        SoguApi.getService(application, RegisterService::class.java).verifyCode(phone, verCode)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                loginView?.verificationCodeSuccess(it)
                            }
                        }.otherWise {
                            RetrofitUrlManager.getInstance().clearAllDomain()
                            loginView?.verificationCodeFail()
                        }
                    }
                    onError {
                        RetrofitUrlManager.getInstance().clearAllDomain()
                        loginView?.verificationCodeFail()
                    }
                }
    }

    fun verificationCompanyCode(phone: String, verCode: String){
        SoguApi.getService(application, RegisterService::class.java).verifyCompanyCode(phone, verCode)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                loginView?.verificationCompanyCodeSuccess(it)
                            }
                        }.otherWise {
                            RetrofitUrlManager.getInstance().clearAllDomain()
                            loginView?.verificationCodeFail()
                        }
                    }
                    onError {
                        RetrofitUrlManager.getInstance().clearAllDomain()
                        loginView?.verificationCodeFail()
                    }
                }
    }

    fun getUserBean(phone: String, userId: Int) {
        val sp = PreferenceManager.getDefaultSharedPreferences(application)
        var source: String? = null
        var unique: String? = null
        sp.getString(Extras.THIRDLOGIN, "").apply {
            isNotEmpty().yes {
                source = split("_")[0]
                unique = split("_")[1]
            }
        }
        SoguApi.getService(application, RegisterService::class.java).getUserBean(phone, userId, source, unique)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                loginView?.getUserBean(it)
                            }
                        }.otherWise {
                            RetrofitUrlManager.getInstance().clearAllDomain()
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, application)
                        }
                    }
                    onError {
                        RetrofitUrlManager.getInstance().clearAllDomain()
                    }
                    onComplete {
                        sp.edit { putString(Extras.THIRDLOGIN, "") }
                    }
                }
    }

    fun getCompanyInfo(key: String) {
        SoguApi.getService(application, RegisterService::class.java)
                .getBasicInfo(key)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                loginView?.getCompanyInfoSuccess(it)
                            }
                        }.otherWise {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, application)
                        }
                    }
                    onError {
                        loginView?.getInfoFinish()
                    }
                    onComplete {
                        loginView?.getInfoFinish()
                    }
                }
    }
}