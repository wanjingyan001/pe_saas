package com.sogukj.pe.module.register

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_invite_by_phone.*
import org.jetbrains.anko.startActivity

class InviteByPhoneActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_by_phone)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)

        val inputList = ArrayList<Observable<CharSequence>>()
        inputList.add(RxTextView.textChanges(phoneEdt.getEditText()))
        inputList.add(RxTextView.textChanges(nameEdt.getEditText()))
        Observable.combineLatest(inputList) { str ->
            (str[0] as CharSequence).isEmpty() || (str[1] as CharSequence).isEmpty()
        }.subscribe { b ->
            inviteNowBtn.isEnabled = !b
        }

        inviteNowBtn.clickWithTrigger {
            if (Utils.isMobileExact(phoneEdt.getInput())) {
//                TODO("调用接口发送邀请短信")
            } else {
                showTopSnackBar("手机号格式有误")
            }
        }
    }
}
