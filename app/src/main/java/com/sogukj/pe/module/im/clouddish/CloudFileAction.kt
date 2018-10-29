package com.sogukj.pe.module.im.clouddish

import android.content.Intent
import com.netease.nim.uikit.business.session.actions.BaseAction

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudFileAction : BaseAction {

    constructor(iconResId : Int, titleId : Int):super(iconResId,titleId){

    }

    override fun onClick() {
        CloudDishActivity.invoke(activity,1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

}