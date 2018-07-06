package com.sogukj.pe.module.register

import android.content.Intent
import android.os.AsyncTask.execute
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
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.RegisterVerResult
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_upload_basic_info.*
import kotlinx.android.synthetic.main.item_arrange_edit.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity
import java.io.File

class UploadBasicInfoActivity : ToolbarActivity() {
    private var cardPath: String = ""
    private var mechanismName: String = ""
    private lateinit var key:String
    private lateinit var result: RegisterVerResult

    override val menuId: Int
        get() = R.menu.next_step

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_basic_info)
        title = "基础资料上传"
        setBack(true)
        result = intent.getParcelableExtra(Extras.BEAN)
        key = result.key
        mechanismNameEdt.text = result.mechanism_name
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

    private fun uploadInfo() {
        if (key.isNotEmpty()) {
            SoguApi.getService(application, RegisterService::class.java)
                    .uploadBasicInfo(key, emailEdt.textStr, mAddressEdt.textStr, webEdt.textStr, mPhoneEdt.textStr)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                showSuccessToast("提交成功")
                                startActivity<CreateDepartmentActivity>(Extras.BEAN to result,
                                        Extras.DATA to cardPath)
                            } else {
                                showTopSnackBar(payload.message)
                            }
                        }
                    }
        }
    }

    private fun uploadLogo() {
        val file = File(cardPath)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                .addFormDataPart("key", key).build()
        SoguApi.getService(application, RegisterService::class.java).uploadLogo(body)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                Glide.with(ctx)
                                        .load(it)
                                        .into(mCompanyLogo)
                            }
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_next_step -> {
                if (cardPath.isNotEmpty() && mechanismName.isNotEmpty()) {
                    uploadInfo()
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
            uploadLogo()
        }
    }
}
