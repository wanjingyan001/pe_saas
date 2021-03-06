package com.sogukj.pe.module.creditCollection

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.service.CreditService
import com.sogukj.pe.widgets.keyboard.KeyBoardUtil
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_hundred_search.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * 百融征信查询界面
 */
class HundredSearchActivity : ToolbarActivity() {
    private val name: String? by extraDelegate(Extras.NAME, null)
    private val phone: String? by extraDelegate(Extras.DATA, null)
    private val idCard: String? by extraDelegate(Extras.DATA2, null)
    private val pid: Int? by extraDelegate(Extras.ID, null)
    private var keyboardUtli : KeyBoardUtil ? = null
    override val menuId: Int
        get() = if (pid != null) {
            R.menu.arrange_edit_delete
        } else {
            super.menuId
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hundred_search)
        Utils.setWindowStatusBarColor(this, R.color.colorPrimary)
        toolbar?.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        setBack(true)
        title = "个人资质查询"
        val inputList = ArrayList<Observable<CharSequence>>()
        keyboardUtli = KeyBoardUtil(this)
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

        idCardEdt.setOnTouchListener { v, event ->
            keyboardUtli?.attachTo(idCardEdt)
            false
        }
    }


    private fun doSearch() {
        SoguApi.getService(application, CreditService::class.java)
                .HundredCredit(nameEdt.textStr, idCardEdt.textStr, phoneEdt.textStr,pid)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                startActivity<NewCreditDetailActivity>(Extras.BEAN to it)
                                finish()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.arrange_delete) {
            val mDialog = MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .canceledOnTouchOutside(true)
                    .customView(R.layout.dialog_yongyin, false).build()
            mDialog.show()
            val content = mDialog.find<TextView>(R.id.content)
            val cancel = mDialog.find<Button>(R.id.cancel)
            val yes = mDialog.find<Button>(R.id.yes)
            content.text = "是否需要删除该征信记录"
            cancel.text = "否"
            yes.text = "是"
            cancel.clickWithTrigger {
                mDialog.dismiss()
            }
            yes.clickWithTrigger {
                delete()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun delete() {
        pid?.let {
            SoguApi.getService(this, CreditService::class.java)
                    .deleteHundredCredit(it)
                    .execute {
                        onNext { payload ->
                            payload.isOk.yes {
                                showSuccessToast("删除成功")
                                finish()
                            }.otherWise {
                                showErrorToast(payload.message)
                            }
                        }
                    }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            var b = keyboardUtli?.hideSoftKeyboard() ?: false
            return if (b) true else super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }
}
