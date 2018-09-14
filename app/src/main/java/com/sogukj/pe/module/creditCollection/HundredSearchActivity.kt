package com.sogukj.pe.module.creditCollection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_hundred_search.*
import org.jetbrains.anko.startActivity

/**
 * 百融征信查询界面
 */
class HundredSearchActivity : ToolbarActivity() {
    private val name: String? by extraDelegate(Extras.NAME, null)
    private val phone:String? by extraDelegate( Extras.DATA,null)
    private val idCard:String? by extraDelegate(Extras.DATA2 ,null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hundred_search)
        Utils.setWindowStatusBarColor(this, R.color.colorPrimary)
        toolbar?.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        setBack(true)
        title = "个人资质查询"
        val inputList = ArrayList<Observable<CharSequence>>()
        inputList.add(RxTextView.textChanges(nameEdt))
        inputList.add(RxTextView.textChanges(phoneEdt))
        inputList.add(RxTextView.textChanges(idCardEdt))
        Observable.combineLatest(inputList) { str ->
            (str[0] as CharSequence).isEmpty()
                    || (str[1] as CharSequence).isEmpty()
                    || (str[2] as CharSequence).isEmpty()
        }.subscribe { b ->
            searchCredit.isEnabled = !b
        }
        nameEdt.setText(name)
        phoneEdt.setText(phone)
        idCardEdt.setText(idCard)

        searchCredit.clickWithTrigger {
            Utils.isMobileExact(phoneEdt.textStr).yes {
                Utils.isIDCard(idCardEdt.textStr).yes {
                    doSearch()
                }.otherWise {
                    showErrorToast("请输入正确的身份证号")
                }
            }.otherWise {
                showErrorToast("请输入正确的手机号")
            }
        }
    }


    private fun doSearch() {
        SoguApi.getService(application, CreditService::class.java)
                .HundredCredit(nameEdt.textStr, idCardEdt.textStr, phoneEdt.textStr)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                startActivity<NewCreditDetailActivity>(Extras.BEAN to it)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onError {
                        showErrorToast("查询失败")
                    }
                }
    }
}
