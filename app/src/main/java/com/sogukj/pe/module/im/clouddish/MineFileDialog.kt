package com.sogukj.pe.module.im.clouddish

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/26.
 */
class MineFileDialog {
    companion object {
        fun showUploadFile(context:Context,callBack: UploadCallBack){
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_show_upload)
            val lay = dialog.getWindow()!!.getAttributes()
            lay.height = WindowManager.LayoutParams.WRAP_CONTENT
            lay.width = WindowManager.LayoutParams.MATCH_PARENT
            lay.gravity = Gravity.BOTTOM
            dialog.getWindow()!!.setAttributes(lay)
            dialog.show()

            val ll_upload = dialog.find<LinearLayout>(R.id.ll_upload)
            val ll_newdir = dialog.find<LinearLayout>(R.id.ll_newdir)
            val tv_cancel = dialog.find<TextView>(R.id.tv_cancel)

            ll_upload.clickWithTrigger {
                //上传文件
                if (null != callBack){
                    callBack.uploadFile()
                    if (dialog.isShowing){
                        dialog.dismiss()
                    }
                }
            }

            ll_newdir.clickWithTrigger {
                //新建文件夹
                if (null != callBack){
                    callBack.newDir()
                    if (dialog.isShowing){
                        dialog.dismiss()
                    }
                }
            }

            tv_cancel.clickWithTrigger {
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
        }
    }
}