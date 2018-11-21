package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.netease.nim.uikit.common.util.file.FileUtil
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.bean.BatchRemoveBean
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.service.SoguApi
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity

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

        fun showFillterDialog(context: Context,dir:String,filePath:String){
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_show_fillter)
            val lay = dialog.getWindow()!!.getAttributes()
            lay.height = WindowManager.LayoutParams.WRAP_CONTENT
            lay.width = WindowManager.LayoutParams.MATCH_PARENT
            lay.gravity = Gravity.BOTTOM
            dialog.getWindow()!!.setAttributes(lay)
            dialog.show()

            val ll_doc = dialog.find<LinearLayout>(R.id.ll_doc)
            val ll_zip = dialog.find<LinearLayout>(R.id.ll_zip)
            val ll_pic = dialog.find<LinearLayout>(R.id.ll_pic)
            val ll_video = dialog.find<LinearLayout>(R.id.ll_video)
            val ll_audio = dialog.find<LinearLayout>(R.id.ll_audio)
            val ll_other = dialog.find<LinearLayout>(R.id.ll_other)
            val tv_cancel = dialog.find<TextView>(R.id.tv_cancel)

            ll_doc.clickWithTrigger {
                //文档
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "文档",
                        Extras.TYPE to "application",Extras.PATH to filePath)
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            ll_zip.clickWithTrigger {
                //压缩包
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "压缩包",
                        Extras.TYPE to "application",Extras.PATH to filePath)
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            ll_pic.clickWithTrigger {
                //图片
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "图片",
                        Extras.TYPE to "image",Extras.PATH to filePath)
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            ll_video.clickWithTrigger {
                //视频
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "视频",
                        Extras.TYPE to "audio",Extras.PATH to filePath)
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            ll_audio.clickWithTrigger {
                //音频
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "音频",
                        Extras.TYPE to "video",Extras.PATH to filePath)
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            ll_other.clickWithTrigger {
                //其他
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "其他文件",
                        Extras.TYPE to "other",Extras.PATH to filePath)
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            tv_cancel.clickWithTrigger {
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
        }

        fun showFileItemDialog(context: Activity, isdir : Boolean, data: CloudFileBean,requestCode : Int,
                               previousPath:String,callBack: UploadCallBack,isBusinessFile:Boolean){
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_file_item)
            val lay = dialog.getWindow()!!.getAttributes()
            lay.height = WindowManager.LayoutParams.WRAP_CONTENT
            lay.width = WindowManager.LayoutParams.MATCH_PARENT
            lay.gravity = Gravity.BOTTOM
            dialog.getWindow()!!.setAttributes(lay)
            dialog.show()

            val file_icon = dialog.find<ImageView>(R.id.file_icon)
            val tv_summary = dialog.find<TextView>(R.id.tv_summary)
            val tv_fileSize = dialog.find<TextView>(R.id.tv_fileSize)
            val tv_name = dialog.find<TextView>(R.id.tv_name)
            val tv_time = dialog.find<TextView>(R.id.tv_time)

            val tv_forward = dialog.find<TextView>(R.id.tv_forward) //转发副本
            val view_line1 = dialog.find<View>(R.id.view_line1)

            val tv_copy = dialog.find<TextView>(R.id.tv_copy) //复制到
            val view_line2 = dialog.find<View>(R.id.view_line2)

            val tv_rename = dialog.find<TextView>(R.id.tv_rename) //重命名
            val tv_remove = dialog.find<TextView>(R.id.tv_remove) //移动到
            val view_line4 = dialog.find<View>(R.id.view_line4)
            val tv_delete = dialog.find<TextView>(R.id.tv_delete) //删除
            val tv_cancel = dialog.find<TextView>(R.id.tv_cancel)
            if (null != data){
                tv_summary.text = data.file_name
                tv_time.text = data.create_time
                if (data.file_type.equals("Folder")) {
                    file_icon.setImageResource(R.drawable.folder_zip)
                } else {
                    file_icon.imageResource =  FileTypeUtils.getFileType(data.file_name).icon
                    if (data.used_bytes.isNullOrEmpty()){
                        tv_fileSize.setVisible(false)
                    }else{
                        tv_fileSize.setVisible(true)
                        tv_fileSize.text = FileUtil.formatFileSize(data.used_bytes.toLong(), FileUtil.SizeUnit.Auto)
                    }
                }
            }
            if (isdir){
                tv_copy.setVisible(false)
                view_line2.setVisible(false)
            }else{
                tv_copy.setVisible(true)
                view_line2.setVisible(true)
            }
            if (isBusinessFile){
                tv_remove.setVisible(false)
                view_line4.setVisible(false)
            }else{
                tv_remove.setVisible(true)
                view_line4.setVisible(true)
            }
            tv_cancel.clickWithTrigger {
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            tv_forward.clickWithTrigger {
                //转发副本
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            tv_copy.clickWithTrigger {
                //复制到
                if (dialog.isShowing){
                    dialog.dismiss()
                }
                CloudDishActivity.invoke(context,2,"",true,data.file_name,previousPath)
            }

            tv_rename.clickWithTrigger {
                //重命名
                if (dialog.isShowing){
                    dialog.dismiss()
                }
                if (isdir){
                    NewDirActivity.invokeForResult(context, previousPath, requestCode,1,data.file_name)
                }else{
                    NewDirActivity.invokeForResult(context, previousPath, requestCode,2,data.file_name)
                }

            }

            tv_remove.clickWithTrigger {
                //移动到
                if (dialog.isShowing){
                    dialog.dismiss()
                }
                context.startActivity<FileDirDetailActivity>(Extras.TITLE to "",Extras.TYPE to 2,
                        Extras.TYPE1 to "",Extras.TYPE2 to "/我的文件","isSave" to false,"isCopy" to false,"isRemove" to true,
                        "fileName" to data.file_name,"previousPath" to previousPath,"batchPath" to  BatchRemoveBean())
            }

            tv_delete.clickWithTrigger {
                //删除
                if (dialog.isShowing){
                    dialog.dismiss()
                }
                showDeleteFileDialog(context,isdir,previousPath,data.file_name,callBack)
            }
        }

        fun showDeleteFileDialog(context:Context,isdir : Boolean,previousPath:String,fileName : String,callBack: UploadCallBack){
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_delete_file)
            val lay = dialog.getWindow()!!.getAttributes()
            lay.height = WindowManager.LayoutParams.WRAP_CONTENT
            lay.width = WindowManager.LayoutParams.MATCH_PARENT
            lay.gravity = Gravity.CENTER
            dialog.getWindow()!!.setAttributes(lay)
            dialog.show()

            val tv_title = dialog.find<TextView>(R.id.tv_title)
            val tv_content = dialog.find<TextView>(R.id.tv_content)
            val tv_sure = dialog.find<TextView>(R.id.tv_sure)
            val tv_cancel = dialog.find<TextView>(R.id.tv_cancel)
            if (isdir){
                tv_title.setVisible(true)
                tv_title.text = "删除此文件夹？"
                tv_content.text = "文件夹及文件夹下的所有文件将被彻底删除"
            }else{
                tv_title.setVisible(false)
                tv_content.text = "请确认要彻底删除该文件吗？"
            }

            tv_sure.clickWithTrigger {
                //删除文件、文件夹
                if (dialog.isShowing){
                    dialog.dismiss()
                }
                (context as BaseActivity).showProgress("正在删除...")
                SoguApi.getStaticHttp(App.INSTANCE)
                        .deleteCloudFile(previousPath+"/${fileName}", Store.store.getUser(context)!!.phone)
                        .execute {
                            onNext { payload ->
                                if (payload.isOk){
                                    if (null != callBack){
                                        callBack.deleteFile()
                                    }
                                    ToastUtil.showSuccessToast("删除成功",context)
                                }else{
                                    ToastUtil.showErrorToast(payload.message,context)
                                }
                                (context as BaseActivity).hideProgress()
                            }

                            onError {
                                it.printStackTrace()
                                ToastUtil.showErrorToast("删除失败",context)
                                (context as BaseActivity).hideProgress()
                            }
                        }
            }

            tv_cancel.clickWithTrigger {
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
        }

        /**
         * 批量删除
         */
        fun showDeleteBatchFileDialog(context:Context,isdir : Boolean,batchPath:String,callBack: UploadCallBack){
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_delete_file)
            val lay = dialog.getWindow()!!.getAttributes()
            lay.height = WindowManager.LayoutParams.WRAP_CONTENT
            lay.width = WindowManager.LayoutParams.MATCH_PARENT
            lay.gravity = Gravity.CENTER
            dialog.getWindow()!!.setAttributes(lay)
            dialog.show()

            val tv_title = dialog.find<TextView>(R.id.tv_title)
            val tv_content = dialog.find<TextView>(R.id.tv_content)
            val tv_sure = dialog.find<TextView>(R.id.tv_sure)
            val tv_cancel = dialog.find<TextView>(R.id.tv_cancel)
            if (isdir){
                tv_title.setVisible(true)
                tv_title.text = "删除此文件夹？"
                tv_content.text = "文件夹及文件夹下的所有文件将被彻底删除"
            }else{
                tv_title.setVisible(false)
                tv_content.text = "请确认要彻底删除该文件吗？"
            }

            tv_sure.clickWithTrigger {
                //删除文件、文件夹
                if (dialog.isShowing){
                    dialog.dismiss()
                }
                (context as BaseActivity).showProgress("正在删除...")
                SoguApi.getStaticHttp(App.INSTANCE)
                        .deleteBatchCloudFile(batchPath,Store.store.getUser(context)!!.phone)
                        .execute {
                            onNext { payload ->
                                if (payload.isOk){
                                    ToastUtil.showSuccessToast("删除成功",context)
                                    if (null != callBack){
                                        callBack.batchDeleteFile()
                                    }
                                }else{
                                    ToastUtil.showErrorToast(payload.message,context)
                                }
                                (context as BaseActivity).hideProgress()
                            }

                            onError {
                                it.printStackTrace()
                                ToastUtil.showErrorToast("删除失败",context)
                                (context as BaseActivity).hideProgress()
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