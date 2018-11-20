package com.sogukj.pe.module.register

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.main.ScanResultActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import com.xuexuan.zxing.android.activity.CaptureActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_invite_by_pc.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.toast

class InviteByPcActivity : ToolbarActivity() {
    private val CAMERA = Manifest.permission.CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_by_pc)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        title = "批量导入"

        copyLink.setOnLongClickListener {
            val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            manager.primaryClip = ClipData.newPlainText(null, "http://saas.pewinner.com")
            toast("已复制到剪贴板")
            true
        }
        loginByQRCode.clickWithTrigger {
            if (ActivityCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(CAMERA), Extras.REQUESTCODE)
            } else {
                val openCameraIntent = Intent(this, CaptureActivity::class.java)
                startActivityForResult(openCameraIntent, 0)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Extras.REQUESTCODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val openCameraIntent = Intent(this, CaptureActivity::class.java)
            startActivityForResult(openCameraIntent, 0)
        } else {
            showCustomToast(R.drawable.icon_toast_common, "该功能需要相机权限")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data!=null){
           val scanResult =  data.extras.getString("result")
            if (scanResult.contains("/api/qrlogin/notify")) {
                val index = scanResult.indexOf("/api/")
                info { scanResult.substring(0, index) }
                RetrofitUrlManager.getInstance().putDomain("QRCode", scanResult.substring(0, index))
                SoguApi.getService(application, OtherService::class.java)
                        .qrNotify_saas(scanResult.substring(index), 1, Store.store.getUser(application)?.phone!!)
                        .execute {
                            onNext { payload ->
                                if (payload.isOk) {
                                    ScanResultActivity.start(this@InviteByPcActivity, scanResult)
                                    overridePendingTransition(R.anim.activity_in, 0)
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }
                        }
            }
        }
    }
}
