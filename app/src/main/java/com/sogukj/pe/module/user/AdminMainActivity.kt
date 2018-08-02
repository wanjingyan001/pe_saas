package com.sogukj.pe.module.user

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import kotlinx.android.synthetic.main.activity_admin_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class AdminMainActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)
        title = "管理员"
        setBack(true)
        addAdministrator.clickWithTrigger {
            startActivity<AddAdminActivity>()
        }
        transferAdministrator.clickWithTrigger {
            startActivityForResult<AdminTransferActivity>(Extras.REQUESTCODE)
        }
    }
}
