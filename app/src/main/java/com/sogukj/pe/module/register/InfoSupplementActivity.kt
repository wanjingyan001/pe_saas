package com.sogukj.pe.module.register

import android.content.Intent
import android.os.Bundle
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_register_info_supplement.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class InfoSupplementActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_info_supplement)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))

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
            startActivity<TakeCardActivity>()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            organScale.text = data.getStringExtra(Extras.DATA)
            scaleHint.setVisible(true)
        }
    }
}
