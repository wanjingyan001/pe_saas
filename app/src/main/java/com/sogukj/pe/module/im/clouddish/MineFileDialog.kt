package com.sogukj.pe.module.im.clouddish

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.peUtils.FileTypeUtils
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

        fun showFillterDialog(context: Context,dir:String){
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
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "文档")
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            ll_zip.clickWithTrigger {
                //压缩包
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "压缩包")
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            ll_pic.clickWithTrigger {
                //图片
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "图片")
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            ll_video.clickWithTrigger {
                //视频
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "视频")
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            ll_audio.clickWithTrigger {
                //音频
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "音频")
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            ll_other.clickWithTrigger {
                //其他
                context.startActivity<CloudFileSortActivity>(Extras.TITLE to dir,Extras.SORT to "其他文件")
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

        fun showFileItemDialog(context: Context,isdir : Boolean,data: CloudFileBean){
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
            val tv_delete = dialog.find<TextView>(R.id.tv_delete) //删除
            val tv_cancel = dialog.find<TextView>(R.id.tv_cancel)
            if (null != data){
                tv_summary.text = data.file_name
                tv_time.text = data.create_time
                if (data.file_type.equals("Folder")) {
                    file_icon.setImageResource(R.drawable.folder_zip)
                } else {
                    file_icon.imageResource =  FileTypeUtils.getFileType(data.file_name).icon
                }
            }
            if (isdir){
                tv_forward.setVisible(false)
                view_line1.setVisible(false)

                tv_copy.setVisible(false)
                view_line2.setVisible(false)
            }else{
                tv_forward.setVisible(true)
                view_line1.setVisible(true)

                tv_copy.setVisible(true)
                view_line2.setVisible(true)
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
            }

            tv_rename.clickWithTrigger {
                //重命名
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            tv_remove.clickWithTrigger {
                //移动到
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            tv_delete.clickWithTrigger {
                //删除
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
        }
    }
}