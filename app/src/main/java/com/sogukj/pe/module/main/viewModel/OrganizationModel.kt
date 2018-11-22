/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: OrganizationModel
 * Author: admin
 * Date: 2018/11/22 上午11:38
 * Description: 组织架构viewModel
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.module.main.viewModel;

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.amap.api.mapcore.util.it
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.utils.ToastUtils
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi

/**
 * @ClassName: OrganizationModel
 * @Description: 组织架构viewModel
 * @Author: admin
 * @Date: 2018/11/22 上午11:38
 */
class OrganizationModel : ViewModel() {
    var datas = MutableLiveData<List<DepartmentBean>>()

    fun getUserDepart(context: Context) {
        SoguApi.getService(context, UserService::class.java)
                .userDepart()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                datas.value = it
                            }
                        }.otherWise {
                            payload.message?.let {
                                ToastUtils.showErrorToast(it, context)
                            }
                        }
                    }
                    onError {
                        ToastUtils.showErrorToast("公司组织架构人员获取失败", context)
                    }
                }

    }
}