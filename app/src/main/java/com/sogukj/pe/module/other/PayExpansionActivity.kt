package com.sogukj.pe.module.other

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import kotlinx.android.synthetic.main.activity_pay_expansion.*

class PayExpansionActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_expansion)
        toolbar_title.text = "扩容套餐购买"

    }
}
