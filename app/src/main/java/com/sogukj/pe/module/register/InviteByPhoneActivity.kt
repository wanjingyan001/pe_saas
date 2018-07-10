package com.sogukj.pe.module.register

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amap.api.mapcore.util.it
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_invite_by_phone.*
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

class InviteByPhoneActivity : ToolbarActivity() {

    private var inviteCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_by_phone)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)

        inviteCode = intent.getStringExtra(Extras.DATA)
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
                sendInviteMessage(phoneEdt.getInput(), nameEdt.getInput())
            } else {
                showTopSnackBar("手机号格式有误")
            }
        }
    }


    private fun sendInviteMessage(phone: String, name: String) {
        inviteCode?.let {
            SoguApi.getService(application, RegisterService::class.java).inviteByPhone(phone, it, name)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                showSuccessToast("邀请成功")
                                phoneEdt.setText("")
                                nameEdt.setText("")
                                phoneEdt.isFocusableInTouchMode = true
                                phoneEdt.requestFocus()
                            } else {
                                showTopSnackBar(payload.message)
                            }
                        }
                    }
        }
    }
}
