package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Intent
import com.netease.nim.uikit.business.session.actions.BaseAction

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudFileAction : BaseAction {

    constructor(iconResId : Int, titleId : Int):super(iconResId,titleId){

    }

    override fun onClick() {
        CloudDishActivity.invokeForResult(activity,1,REQ_SELETE_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQ_SELETE_FILE -> {

                }
            }
        }
    }

    companion object {
        val REQ_SELETE_FILE = 1002
    }

}