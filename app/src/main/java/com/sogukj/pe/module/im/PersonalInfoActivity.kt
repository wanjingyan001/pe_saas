package com.sogukj.pe.module.im

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.ClipboardManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.netease.nim.uikit.api.NimUIKit
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.service.ImService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_personal_info.*
import org.jetbrains.anko.toast

class PersonalInfoActivity : BaseActivity(), View.OnClickListener, TextWatcher, View.OnLongClickListener {

    private var pathByUri: String? = null
    lateinit var db: XmlDb
    var user: UserBean? = null
    val CALL_PHONE_PERMISSION = arrayOf(Manifest.permission.CALL_PHONE)

    companion object {
        fun start(ctx: Context, user: UserBean, pathByUri: String?) {
            val intent = Intent(ctx, PersonalInfoActivity::class.java)
            intent.putExtra(Extras.DATA, user)
            intent.putExtra(Extras.DATA2, pathByUri)
            ctx.startActivity(intent)
        }

        fun start(ctx: Context?, uid: Int) {
            val intent = Intent(ctx, PersonalInfoActivity::class.java)
            intent.putExtra(Extras.ID, uid)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        team_toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        team_toolbar.setNavigationOnClickListener { finish() }
        db = XmlDb.open(this)
        user = intent.getSerializableExtra(Extras.DATA) as UserBean?
        pathByUri = intent.getStringExtra(Extras.DATA2)
        if (user != null) {
            queryUserInfo(user!!.uid!!)
        } else {
            val uid = intent.getIntExtra(Extras.ID, -1)
            queryUserInfo(uid)
        }
        sendMsg.setOnClickListener(this)
        call_phone.setOnClickListener(this)

        company.setOnLongClickListener(this)
        name_tv.setOnLongClickListener(this)
        phone_tv.setOnLongClickListener(this)
        email_tv.setOnLongClickListener(this)
        department_tv.setOnLongClickListener(this)
        position_tv.setOnLongClickListener(this)

    }

    private fun queryUserInfo(uid: Int) {
        SoguApi.getService(application, ImService::class.java)
                .showIMUserInfo(uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            user = it
                            name.text = it.name
                            position.text = it.position
                            name_tv.text = it.name
                            phone_tv.text = it.phone
                            company.text = it.company
                            email_tv.text = it.email
                            department_tv.text = it.depart_name
                            position_tv.text = it.position
                            Glide.with(this)
                                    .load(it.headImage())
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            return false
                                        }

                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            avatar.setChar(it.name.first())
                                            return false
                                        }

                                    })
                                    .into(avatar)
                        }
                    }
                }, { e ->
                })
    }

    override fun onLongClick(v: View?): Boolean {
        val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (v is TextView) {
            manager.text = v.text
            toast("已复制到剪贴板")
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sendMsg -> {
                if (NimUIKit.getAccount().isNotEmpty() && user?.accid != null) {
                    if (!pathByUri.isNullOrEmpty()) {
                        NimUIKit.startP2PSession(this, user?.accid, pathByUri)
                    } else {
                        NimUIKit.startP2PSession(this, user?.accid)
                    }
                } else {
                    toast("信息有误")
                }
            }
            R.id.call_phone -> {
                val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, CALL_PHONE_PERMISSION, 1)
                } else {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${user?.phone}"))
                    startActivity(intent)
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        s?.let {
            if (it.toString().trim().isNotEmpty() && !user?.accid.isNullOrEmpty()) {
                db.set(user?.accid!!, it.toString())
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${user?.phone}"))
                startActivity(intent)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
