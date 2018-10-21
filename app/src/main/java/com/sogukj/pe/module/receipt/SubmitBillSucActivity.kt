package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_submit_success.*
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/21.
 */
class SubmitBillSucActivity : ToolbarActivity() {
    private var title = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_success)
        Utils.setWindowStatusBarColor(this, R.color.white)
        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)
        setTitle(title)
        ll_his.clickWithTrigger {
            //开票历史
            startActivity<InvoiceHistoryActivity>(Extras.TYPE to 0,Extras.TITLE to "开票历史")
        }

    }
}