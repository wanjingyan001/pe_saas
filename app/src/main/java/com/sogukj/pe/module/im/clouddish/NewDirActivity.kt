package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import kotlinx.android.synthetic.main.activity_new_dir.*
import kotlinx.android.synthetic.main.white_normal_toolbar.*


/**
 * Created by CH-ZH on 2018/10/26.
 * 新建文件夹
 */
class NewDirActivity : ToolbarActivity(), TextWatcher {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_dir)
        setBack(true)
        setTitle("新建文件夹")
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
                val dataIntent = Intent()
                dataIntent.putExtra(Extras.DATA,et_input.textStr)
                setResult(Activity.RESULT_OK,dataIntent)
                finish()
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
        fun invokeForResult(context:Activity,requestCode:Int){
            val intent = Intent(context,NewDirActivity::class.java)
            context.startActivityForResult(intent,requestCode)
        }
    }
}