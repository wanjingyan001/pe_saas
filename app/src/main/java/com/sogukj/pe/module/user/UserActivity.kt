package com.sogukj.pe.module.user

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.module.other.PayExpansionActivity
import org.jetbrains.anko.startActivity

class UserActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user2)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, UserFragment.newInstance())
                .commitAllowingStateLoss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Extras.REQUESTCODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity<PayExpansionActivity>()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        fun start(ctx: Context) {
            val intent = Intent(ctx, UserActivity::class.java)
            ctx.startActivity(intent)
        }
    }
}
