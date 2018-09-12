package com.sogukj.pe.module.creditCollection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

class NewCreditDetailActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_credit_detail)
        title = "个人资质"
        setBack(true)
    }
}
