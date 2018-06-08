package com.sogukj.pe.module.register

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.EasyPhotos
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.interf.ReviewStatus
import kotlinx.android.synthetic.main.activity_register_take_card.*
import kotlin.properties.Delegates

class TakeCardActivity : ToolbarActivity() {
    private var cardPath: String by Delegates.observable("") { _, _, newValue ->
        nextStep.isEnabled = newValue.isNotEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_take_card)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)

        takeCard.clickWithTrigger {
            EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                    .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                    .setPuzzleMenu(false)
                    .start(Extras.REQUESTCODE)
        }
        nextStep.clickWithTrigger {
            ReviewActivity.start(this,ReviewStatus.FAILURE_REVIEW)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == RESULT_OK && data != null) {
            val resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
            cardPath = resultPaths[0]
            Glide.with(this)
                    .load(cardPath)
                    .into(takeCard)
        }
    }
}
