package com.sogukj.pe.module.register

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_phone_input.*
import org.jetbrains.anko.startActivity

/**
 * 注册--手机号输入界面
 */
class PhoneInputActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_input)
        Utils.setWindowStatusBarColor(this, R.color.white)
        RxTextView.textChanges(phoneEdt.getEditText())
                .subscribe({ t ->
                    nextStep.isEnabled = !t.isNullOrEmpty()
                })
        nextStep.clickWithTrigger {
            if (Utils.isMobileExact(phoneEdt.getInput())) {
                startActivity<VerCodeInputActivity>()
            }else{
                showTopSnackBar("手机号格式有误")
            }
        }
    }
}
