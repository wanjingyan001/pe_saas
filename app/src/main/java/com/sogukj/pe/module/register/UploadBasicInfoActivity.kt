package com.sogukj.pe.module.register

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.EasyPhotos
import com.jakewharton.rxbinding2.widget.RxTextView
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import kotlinx.android.synthetic.main.activity_upload_basic_info.*
import org.jetbrains.anko.startActivity

class UploadBasicInfoActivity : ToolbarActivity() {
    private var cardPath: String = ""
    private var mechanismName: String = ""
    override val menuId: Int
        get() = R.menu.next_step

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_basic_info)
        title = "基础资料上传"
        setBack(true)
        RxTextView.textChanges(mechanismNameEdt).subscribe { str ->
            mechanismName = str.toString()
        }

        logoLayout.clickWithTrigger {
            EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                    .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                    .setPuzzleMenu(false)
                    .start(Extras.REQUESTCODE)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_next_step -> {
                if (cardPath.isNotEmpty() && mechanismName.isNotEmpty()) {
                    startActivity<CreateDepartmentActivity>()
                } else {
                    showTopSnackBar("请完善资料")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == RESULT_OK && data != null) {
            val resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
            cardPath = resultPaths[0]
            Glide.with(this)
                    .load(cardPath)
                    .into(mCompanyLogo)
        }
    }
}
