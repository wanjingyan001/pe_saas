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
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.JoinTeamResult
import com.sogukj.pe.interf.ReviewStatus
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_register_take_card.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.ctx
import java.io.File
import kotlin.properties.Delegates

class TakeCardActivity : ToolbarActivity() {
    private var cardPath: String by Delegates.observable("") { _, _, newValue ->
        nextStep.isEnabled = newValue.isNotEmpty()
    }

    private lateinit var result: JoinTeamResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_take_card)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        result = intent.getSerializableExtra(Extras.DATA) as JoinTeamResult

        takeCard.clickWithTrigger {
            EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                    .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                    .setPuzzleMenu(false)
                    .start(Extras.REQUESTCODE)
        }
        nextStep.clickWithTrigger {
            ReviewActivity.start(this, ReviewStatus.SUCCESSFUL_REVIEW)
        }
    }

    private fun uploadCard() {
        val file = File(cardPath)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("card", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("user_id", result.user_id.toString()).build()
        SoguApi.getService(application, RegisterService::class.java).uploadCard(body)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                Glide.with(ctx)
                                        .load(it)
                                        .into(takeCard)
                            }
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == RESULT_OK && data != null) {
            val resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
            cardPath = resultPaths[0]
            uploadCard()
        }
    }
}
