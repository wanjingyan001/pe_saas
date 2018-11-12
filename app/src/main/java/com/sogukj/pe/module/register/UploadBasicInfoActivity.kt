package com.sogukj.pe.module.register

import android.content.Intent
import android.os.AsyncTask.execute
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.huantansheng.easyphotos.EasyPhotos
import com.jakewharton.rxbinding2.widget.RxTextView
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.RegisterVerResult
import com.sogukj.pe.peExtended.getEnvironment
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
    private  var mechanismName: String?=null
    private lateinit var key: String
    private lateinit var phone: String
    private val flag: Boolean by extraDelegate(Extras.FLAG, false)

    override val menuId: Int
        get() = R.menu.next_step

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_basic_info)
        title = "基础资料上传"
        setBack(true)
        key = sp.getString(Extras.CompanyKey, "")
        mechanismName = intent.getStringExtra(Extras.NAME)
        phone = intent.getStringExtra(Extras.CODE)
        supportInvalidateOptionsMenu()
        mechanismNameEdt.text = mechanismName?:"无"
        logoLayout.clickWithTrigger {
            EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                    .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                    .setPuzzleMenu(false)
                    .start(Extras.REQUESTCODE)
        }
        getBasicInfo()
    }

    private fun uploadInfo() {
        if (key.isNotEmpty()) {
            SoguApi.getService(application, RegisterService::class.java)
                    .uploadBasicInfo(key, emailEdt.textStr, mAddressEdt.textStr, webEdt.textStr, mPhoneEdt.textStr)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                showSuccessToast("提交成功")
                                if (flag) {
                                    finish()
                                } else {
                                    startActivity<CreateDepartmentActivity>(Extras.NAME to mechanismName,
                                            Extras.CODE to phone,
                                            Extras.DATA to cardPath)
                                }
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
                            Gson().fromJson<MechanismBasicInfo?>(sp.getString(Extras.SAAS_BASIC_DATA, ""))?.let {
                                it.logo = payload.payload
                                sp.edit { putString(Extras.SAAS_BASIC_DATA, it.jsonStr) }
                            }
                        } else {
                            cardPath = ""
                            showErrorToast("${payload.message},只接受jpg,png,jpeg,gif类型的图片")
                            Glide.with(ctx)
                                    .load(cardPath)
                                    .into(mCompanyLogo)
                        }
                    }
                }
    }

    private fun getBasicInfo() {
        if (key.isNotEmpty() && flag) {
            SoguApi.getService(application, RegisterService::class.java).getBasicInfo(key)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                payload.payload?.let {
                                    cardPath = it.logo ?: ""
                                    mechanismName = it.mechanism_name ?: ""
                                    val defaultLogo = when (getEnvironment()) {
                                        "zgh" -> R.mipmap.ic_launcher_zgh
                                        else -> R.mipmap.ic_launcher_pe

                                    }
                                    Glide.with(ctx)
                                            .load(it.logo)
                                            .apply(RequestOptions().placeholder(defaultLogo).error(defaultLogo))
                                            .into(mCompanyLogo)
                                    mechanismNameEdt.text = it.mechanism_name
                                    mPhoneEdt.setText(it.telephone)
                                    mAddressEdt.setText(it.address)
                                    emailEdt.setText(it.email)
                                    webEdt.setText(it.website)
                                }
                            } else {
                                showTopSnackBar(payload.message)
                            }
                        }
                    }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!flag) {
            menuInflater.inflate(menuId, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        if (flag) {
            menuInflater.inflate(R.menu.user_edit, menu)
        } else {
            menuInflater.inflate(R.menu.next_step, menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_next_step, R.id.action_save -> {
                if (cardPath.isNotEmpty() && mechanismName?.isNotEmpty() == true) {
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
            Glide.with(ctx)
                    .load(cardPath)
                    .thumbnail(0.1f)
                    .into(mCompanyLogo)
        }
    }
}
