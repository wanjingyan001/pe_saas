package com.sogukj.pe.widgets

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.R
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/9/21.
 */
class BuildProjectDialog {

    companion object {
        private var mAct : Activity ? = null
        fun showAgreeBuildProDialog(context: Activity){
            mAct = context
            val build = MaterialDialog.Builder(context)
                    .theme(Theme.DARK)
                    .customView(R.layout.layout_agree_build, false)
                    .canceledOnTouchOutside(false)
                    .build()
            build.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val iv_close = build.find<ImageView>(R.id.iv_close)
            val ed_agree = build.find<EditText>(R.id.ed_agree)
            val rv = build.find<RecyclerView>(R.id.rv)
            val tv_add_file = build.find<TextView>(R.id.tv_add_file)
            val tv_cancel = build.find<TextView>(R.id.tv_cancel)
            val tv_agree = build.find<TextView>(R.id.tv_agree)

            iv_close.setOnClickListener {
                if (build.isShowing){
                    build.dismiss()
                }
            }

            tv_add_file.setOnClickListener {
                //添加文件
            }

            tv_cancel.setOnClickListener {
                if (build.isShowing){
                    build.dismiss()
                }
            }

            tv_agree.setOnClickListener {
                if (build.isShowing){
                    build.dismiss()
                }

                //同意通过
            }
            build.show()
        }

        fun showAgreeBuildLxh(context:Activity){
            mAct = context
            val build = MaterialDialog.Builder(context)
                    .theme(Theme.DARK)
                    .customView(R.layout.layout_agree_build_lxh, false)
                    .canceledOnTouchOutside(false)
                    .build()
            build.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val iv_close = build.find<ImageView>(R.id.iv_close)
            val ed_agree = build.find<EditText>(R.id.ed_agree)
            val rv = build.find<RecyclerView>(R.id.rv)
            val tv_add_file = build.find<TextView>(R.id.tv_add_file)
            val tv_cancel = build.find<TextView>(R.id.tv_cancel)
            val tv_agree = build.find<TextView>(R.id.tv_agree)
            val tv_time = build.find<TextView>(R.id.tv_time)
            iv_close.setOnClickListener {
                if (build.isShowing){
                    build.dismiss()
                }
            }

            tv_add_file.setOnClickListener {
                //添加文件
            }

            tv_cancel.setOnClickListener {
                if (build.isShowing){
                    build.dismiss()
                }
            }

            tv_agree.setOnClickListener {
                if (build.isShowing){
                    build.dismiss()
                }

                //同意通过
            }
            build.show()
        }
    }
}