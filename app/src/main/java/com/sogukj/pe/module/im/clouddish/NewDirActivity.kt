package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_new_dir.*
import kotlinx.android.synthetic.main.white_normal_toolbar.*


/**
 * Created by CH-ZH on 2018/10/26.
 * 新建文件夹
 */
class NewDirActivity : ToolbarActivity(), TextWatcher {
    private var dir = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_dir)
        setBack(true)
        setTitle("新建文件夹")
        dir = intent.getStringExtra("dir")
        toolbar_menu.visibility = View.VISIBLE
        toolbar_menu.text = "完成"
        toolbar_menu.setTextColor(resources.getColor(R.color.gray_f1))

        et_input.addTextChangedListener(this)
        iv_delete.clickWithTrigger {
            et_input.setText("")
            iv_delete.visibility = View.INVISIBLE
        }

        toolbar_menu.clickWithTrigger {
            if (!et_input.textStr.isNullOrEmpty()){
                //新建文件夹
                createNewDir(et_input.textStr)
            }else{
                showCommonToast("文件夹名称不能为空")
            }
        }
    }

    private fun createNewDir(content: String) {
        SoguApi.getService(application,OtherService::class.java)
                .createNewDir(dir,content, Store.store.getUser(this)!!.phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            showSuccessToast("新建文件夹成功")
                            setResult(Activity.RESULT_OK)
                            finish()
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("新建文件夹失败")
                    }
                }
    }

    override fun afterTextChanged(s: Editable?) {
       if (et_input.textStr.length > 0){
           iv_delete.visibility = View.VISIBLE
           toolbar_menu.setTextColor(resources.getColor(R.color.white))
       }else{
           iv_delete.visibility = View.INVISIBLE
           toolbar_menu.setTextColor(resources.getColor(R.color.gray_f1))
       }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    companion object {
        fun invokeForResult(context:Activity,dir:String,requestCode:Int){
            val intent = Intent(context,NewDirActivity::class.java)
            intent.putExtra("dir",dir)
            context.startActivityForResult(intent,requestCode)
        }

        fun invokeForResult(context:Fragment,dir:String,requestCode:Int){
            val intent = Intent(context.activity,NewDirActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("dir",dir)
            context.startActivityForResult(intent,requestCode)
        }

        fun invoke(context: Context,dir:String){
            val intent = Intent(context,NewDirActivity::class.java)
            intent.putExtra("dir",dir)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}