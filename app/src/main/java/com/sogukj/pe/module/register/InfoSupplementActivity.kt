package com.sogukj.pe.module.register

import android.content.Intent
import android.os.Bundle
import androidx.core.content.edit
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.TeamInfoSupplementReq
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_register_info_supplement.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import kotlin.properties.Delegates

class InfoSupplementActivity : ToolbarActivity() {

    private lateinit var phone: String
    private var index: Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_info_supplement)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        phone = intent.getStringExtra(Extras.DATA)
        val inputList = ArrayList<Observable<CharSequence>>()
        inputList.add(RxTextView.textChanges(organNameEdt.getEditText()))
        inputList.add(RxTextView.textChanges(organScale))
        inputList.add(RxTextView.textChanges(mNameEdt.getEditText()))
        inputList.add(RxTextView.textChanges(mPositionEdt.getEditText()))
        Observable.combineLatest(inputList) { str ->
            (str[0] as CharSequence).isEmpty()
                    || (str[1] as CharSequence).isEmpty()
                    || (str[2] as CharSequence).isEmpty()
                    || (str[3] as CharSequence).isEmpty()
        }.subscribe { b ->
            infoNextStep.isEnabled = !b
        }
        organScale.clickWithTrigger {
            startActivityForResult(intentFor<ScaleSelectionActivity>(Extras.DATA to organScale.text.toString()), Extras.REQUESTCODE)
        }
        infoNextStep.clickWithTrigger {
            teamInfoSupplement()
        }
    }

    private fun teamInfoSupplement() {
        if (index == 0) {
            showTopSnackBar("请选择规模")
            return
        }
        val teamInfo = TeamInfoSupplementReq(mPositionEdt.getInput(), mNameEdt.getInput(), index, organNameEdt.getInput(), 2, phone, null)
        SoguApi.getService(application, RegisterService::class.java).teamInfoSupplement(teamInfo)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                sp.edit { putString(Extras.CompanyKey, it.key) }
                                startActivity<TakeCardActivity>(Extras.DATA to it)
                            }
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            organScale.text = data.getStringExtra(Extras.DATA)
            index = data.getIntExtra(Extras.INDEX, 0)
            scaleHint.setVisible(true)
        }
    }
}
