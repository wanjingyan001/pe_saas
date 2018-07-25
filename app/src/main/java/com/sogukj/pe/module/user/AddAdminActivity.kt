package com.sogukj.pe.module.user

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

class AddAdminActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_admin)
        title = "添加管理员"
        setBack(true)
    }
}
