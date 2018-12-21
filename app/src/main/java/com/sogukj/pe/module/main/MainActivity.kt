package com.sogukj.pe.module.main

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.*
import com.sogukj.pe.bean.MainBottomBar
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.database.FunctionViewModel
import com.sogukj.pe.database.Injection
import com.sogukj.pe.module.fund.FundMainFragment
import com.sogukj.pe.module.news.NewsDetailActivity
import com.sogukj.pe.module.project.MainProjectFragment
import com.sogukj.pe.module.register.LoginActivity
import com.sogukj.pe.peExtended.RootPath
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.FileUtil
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.FileFindService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.service.socket.DzhClientService
import com.sogukj.pe.widgets.MyProgressBar
import com.sogukj.pe.widgets.PhoneNotifDialog
import com.sogukj.service.SoguApi
import com.tencent.bugly.crashreport.CrashReport
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qinfei on 17/7/18.
 */
class MainActivity : BaseActivity() {
    val fragments = Stack<Fragment>()
    private val mainMsg: MainMsgFragment by lazy { MainMsgFragment.newInstance() }
    private val teamSelect: TeamSelectFragment by lazy { TeamSelectFragment.newInstance() }
    private val mainHome: MainHomeFragment by lazy { MainHomeFragment.newInstance() }
    private val project: MainProjectFragment by lazy { MainProjectFragment.newInstance() }
    private val mainFund: FundMainFragment by lazy { FundMainFragment.newInstance() }
    private val defaultIndex = when (getEnvironment()) {
        "ht" -> 2
        else -> 0
    }

    //    "消息", "通讯录", "基金"
    private val modules = arrayListOf(MainBottomBar(1, "享聊"),
            MainBottomBar(2, "通讯录"),
            MainBottomBar(3, "工作台"),
            MainBottomBar(4, "项目"),
            MainBottomBar(5, "基金"))
    private var items = ArrayList<BottomNavigationItem>()

    lateinit var manager: FragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DzhClientService.startService(this)
        val clazz = intent.getSerializableExtra("uPush.target") as Class<Activity>?
        clazz?.apply {
            val news = intent.getSerializableExtra(Extras.DATA) as NewsBean?
            if (null != news) NewsDetailActivity.start(this@MainActivity, news)
        }
        //现在公司logo
        val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(company)
        detail?.let {
            if (!it.homeBottomButton.isNullOrEmpty()) {
                modules.clear()
                modules.addAll(it.homeBottomButton!!)
            }
            val defaultLogo = when (getEnvironment()) {
                "zgh" -> R.mipmap.ic_launcher_zgh_circle
                else -> R.mipmap.ic_launcher_28_round

            }
            Glide.with(this@MainActivity)
                    .load(it.logo)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(defaultLogo)
                            .error(defaultLogo))
                    .thumbnail(0.1F)
                    .into(mainLogo)
            initBottomNavBar()
            changeFragment(defaultIndex)
        }
        verifyPermissions(this)
        updateVersion()
        ViewCompat.setElevation(mainLogo, 50f)
        val factory = Injection.provideViewModelFactory(ctx)
        val model = ViewModelProviders.of(this, factory).get(FunctionViewModel::class.java)
        model.generateData(application)
        setCrashReportData()
        ActivityHelper.finishAllWithoutTop()

        showPhoneNotifiDialog()

        saveCityAreaJson()
        saveConfigApprove()
        copyAssets("ic_launcher_pe.png")

    }

    private fun saveConfigApprove() {
        SoguApi.getService(application, OtherService::class.java)
                .configApprove()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val jsonObject = payload.payload
                            val hidden = jsonObject!!.get("hidden").asInt
                            Store.store.saveApproveConfig(this@MainActivity, hidden)
                        }
                    }

                    onError {
                        it.printStackTrace()
                    }
                }

        SoguApi.getService(application, OtherService::class.java)
                .saveApprove()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val jsonObject = payload.payload
                            val jsonElement = jsonObject!!.get("is_save_draft")
                            if (null != jsonElement) {
                                Store.store.saveTemplateConfig(this@MainActivity, jsonElement.asInt)
                            }
                        }
                    }

                    onError {
                        it.printStackTrace()
                    }
                }
    }

    private fun copyAssets(filename: String) {
        val assetManager = assets
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = assetManager.open(filename)

            val outFile = File(Environment.getExternalStorageDirectory(), filename)
            out = FileOutputStream(outFile)
            copyFile(`in`, out)
            `in`!!.close()
            out.flush()
            out.close()
        } catch (e: IOException) {
            Trace.e("tag", "Failed to copy asset file: " + filename, e)
        }

    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (true) {
            read = `in`.read(buffer)
            if (read == -1) break
            out.write(buffer, 0, read)
        }
    }

    private fun saveCityAreaJson() {
        var datas = XmlDb.open(this).get(Extras.NEW_CITY_JSON, "")
        if (null == datas || datas.equals("")) {
            val cityJson = Utils.getJson(this, "city.json")
            XmlDb.open(this).set(Extras.NEW_CITY_JSON, cityJson)
        }
    }

    private fun showPhoneNotifiDialog() {
        val brand = Utils.getDeviceBrand()
        if ("Honor".equals(brand) && !XmlDb.open(this).get("Honor")) {
            PhoneNotifDialog(this).showLoadding()
            XmlDb.open(this).set("Honor", true)
        }
    }

    @Suppress("PLUGIN_WARNING")
    private fun initBottomNavBar() {
        val mainItem0 = BottomNavigationItem(R.drawable.ic_qb_sel12, "工作台").setInactiveIconResource(R.drawable.ic_qb_nor2).initNavTextColor()
        val msgItem = BottomNavigationItem(R.drawable.ic_qb_sel11, "享聊").setInactiveIconResource(R.drawable.ic_qb_nor).initNavTextColor()
        val contactItem = BottomNavigationItem(R.drawable.ic_qb_sel15, "通讯录").setInactiveIconResource(R.drawable.ic_qb_nor1).initNavTextColor()
        val mainItem = BottomNavigationItem(R.drawable.ic_qb_selnull, "工作台").setInactiveIconResource(R.drawable.ic_qb_nor2).initNavTextColor1()
        val projectItem = BottomNavigationItem(R.drawable.ic_tab_main_proj_11, "项目").setInactiveIconResource(R.drawable.ic_tab_main_proj_0).initNavTextColor()
        val fundItem = BottomNavigationItem(R.drawable.ic_main_fund22, "基金").setInactiveIconResource(R.drawable.ic_main_fund).initNavTextColor()
        modules.forEach {
            when (it.name) {
                "工作台", "首页" -> {
                    if (modules.size.rem(2) == 0) {
                        items.add(mainItem0)
                    } else {
                        items.add(mainItem)
                    }
                    fragments.add(mainHome)
                }
                "享聊", "消息" -> {
                    items.add(msgItem)
                    fragments.add(mainMsg)
                }
                "通讯录" -> {
                    items.add(contactItem)
                    fragments.add(teamSelect)
                }
                "项目" -> {
                    items.add(projectItem)
                    fragments.add(project)
                }
                "基金" -> {
                    items.add(fundItem)
                    fragments.add(mainFund)
                }
            }
        }
        if (modules.size.rem(2) == 0) {
            mainItem.isEnable(false)
            mainItem.setActiveColorResource(R.color.transparent)
                    .setInActiveColorResource(R.color.transparent)
            items.add(modules.size.div(2), mainItem)
        }
        items.forEach {
            bottomBar.addItem(it)
        }
        bottomBar.setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setBarBackgroundColor(R.color.white)
                .setFirstSelectedPosition(defaultIndex)
                .initialise()
        bottomBar.setTabSelectedListener(object : BottomNavigationBar.SimpleOnTabSelectedListener() {
            override fun onTabSelected(position: Int) {
                if (fragments.size.rem(2) != 0) {
                    if (position == fragments.size.div(2)) {
                        mainLogo.setVisible(true)
                        val scalex = PropertyValuesHolder.ofFloat("scaleX", 0.7f, 1f)
                        val scaley = PropertyValuesHolder.ofFloat("scaleY", 0.7f, 1f)
                        val animator = ObjectAnimator.ofPropertyValuesHolder(mainLogo, scalex, scaley).setDuration(300)
                        animator.interpolator = DecelerateInterpolator()
                        animator.start()
                    } else {
                        mainLogo.setVisible(false)
                    }
                    changeFragment(position)
                } else {
                    if (position != items.size.div(2)) {
                        val index = if (position > items.size.div(2)) position - 1 else position
                        changeFragment(index)
                    } else {
                        return
                    }
                }

            }
        })
        val rem = fragments.size.rem(2) == 0
        val b = (fragments.size.rem(2) != 0).and(fragments.size.div(2) == defaultIndex)
        mainLogo.setVisible(rem or b)
        mainLogo.isEnabled = fragments.size.rem(2) != 0

        manager = supportFragmentManager
        fragments.forEach {
            manager.beginTransaction().add(R.id.container, it, it::class.java.simpleName).commit()
        }
    }

    /**
     * 切换Tab，切换到对应的Fragment
     */
    private fun changeFragment(position: Int) {
        when (fragments[position]::class.java.simpleName) {
            TeamSelectFragment::class.java.simpleName -> {
                StatusBarUtil.setColor(this, resources.getColor(R.color.color_blue_0888ff), 0)
                StatusBarUtil.setDarkMode(this)
            }
            MainHomeFragment::class.java.simpleName -> {
                StatusBarUtil.setColor(this, resources.getColor(R.color.white), 0)
                StatusBarUtil.setLightMode(this)
            }
            else -> {
                StatusBarUtil.setColor(this, resources.getColor(R.color.colorPrimary), 0)
                StatusBarUtil.setDarkMode(this)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        fragments.forEach { ft.hide(it) }
        ft.show(fragments[position])
        ft.commit()
    }


    val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun verifyPermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    1)
        }
    }


    private fun setCrashReportData() {
        CrashReport.putUserData(this, "userId", Store.store.getUser(this)?.uid.toString())
        CrashReport.putUserData(this, "companyKey", sp.getString(Extras.CompanyKey, ""))
        CrashReport.putUserData(this, "httpUrl", sp.getString(Extras.HTTPURL, ""))
    }

    override fun onStart() {
        super.onStart()
        if (!Store.store.checkLogin(this)) {
            startActivity<LoginActivity>()
        }
    }

    private fun updateVersion() {
        val mDialog = MaterialDialog.Builder(this)
                .customView(R.layout.dialog_updated, false)
                .theme(Theme.LIGHT)
                .cancelable(false)
                .build()
        mDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val icon = mDialog.find<ImageView>(R.id.updated_icon)
        val bar = mDialog.find<MyProgressBar>(R.id.update_progress)
        val title = mDialog.find<TextView>(R.id.update_title)
        val update_info = mDialog.find<TextView>(R.id.update_info)
        val scrollView = mDialog.find<ScrollView>(R.id.update_message_layout)
        val message = mDialog.find<TextView>(R.id.update_message)
        val update = mDialog.find<Button>(R.id.update)
        val prompt = mDialog.find<TextView>(R.id.update_prompt)
        //mDialog.show()
        title.text = "正在检测更新"
        update_info.visibility = View.GONE
        scrollView.visibility = View.GONE
        update.visibility = View.GONE
        prompt.visibility = View.VISIBLE
        if (isWifi()) {
            prompt.text = "当前处于Wi-Fi网络，请放心下载"
        } else {
            var hint = SpannableString("当前处于移动网络，下载将消耗流量")
            hint.setSpan(ForegroundColorSpan(Color.parseColor("#FF6174")), 9, hint.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            prompt.text = hint
        }
        RetrofitUrlManager.getInstance().putDomain("upgrade", RootPath)
        SoguApi.getService(application, OtherService::class.java)
                .getVersion()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var newVersion = payload.payload?.version
                        var info = payload.payload?.info
                        var url = payload.payload?.app_url
                        var force = payload.payload?.force//1=>更新，0=>不更新，2---强制更新

                        if (!url.isNullOrEmpty()) {
                            if (force == 0) {
                                title.text = "不用更新"
                                Thread.sleep(500)
                                if (XmlDb.open(context).get("is_read", "").equals("no")) {
                                    mDialog.show()
                                    title.text = "新版功能介绍"
                                    icon.imageResource = R.drawable.update_icon1
                                    message.text = info.toString()
                                    update_info.visibility = View.GONE
                                    scrollView.visibility = View.VISIBLE
                                    update.visibility = View.VISIBLE
                                    prompt.visibility = View.GONE
                                    update.text = "我知道了"
                                    update.setOnClickListener {
                                        mDialog.dismiss()
                                    }
                                    XmlDb.open(context).set("is_read", "yes")
                                }
                                //mDialog.dismiss()
                                //enterNext()
                            } else {//1=>更新，2---强制更新
                                mDialog.show()
                                if (force == 1) {
                                    mDialog.setCanceledOnTouchOutside(true)
                                } else {
                                    mDialog.setCanceledOnTouchOutside(false)
                                }
                                update_info.visibility = View.VISIBLE
                                update.visibility = View.VISIBLE
                                title.text = "当前版本过低(" + Utils.getVersionName(context) + ")"
                                update_info.text = "请立即升级到最新版(" + newVersion + ")"
                                scrollView.visibility = View.GONE
                                message.text = info.toString()
                                update.setOnClickListener {
                                    XmlDb.open(context).set("is_read", "no")
                                    icon.imageResource = R.drawable.update_icon1
                                    update.visibility = View.GONE
                                    title.text = "新版功能介绍"
                                    update_info.visibility = View.GONE
                                    scrollView.visibility = View.VISIBLE
                                    prompt.visibility = View.GONE
                                    update(url!!, bar, update, mDialog, prompt, force!!)
                                }
                            }
                        } else {
                            //enterNext()
                            title.text = "更新失败"
                            //mDialog.dismiss()
                        }
                    } else {
                        //showToast(payload.message)
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        //enterNext()
                        title.text = "更新失败"
                        //mDialog.dismiss()
                    }
                }, { e ->
                    Trace.e(e)
                    title.text = "更新失败"
                    //showToast("更新失败，进入首页")
                    //enterNext()
                    mDialog.dismiss()
                })
    }

    private var bundle: Bundle? = null
    private var pathByUri: String? = null
    private var accid: String? = null
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (manager != null) {
            bottomBar.selectTab(0)
        }
        pathByUri = intent!!.getStringExtra("pathByUri")
        accid = intent.getStringExtra("accid")
        bundle = intent.getBundleExtra("bundle")
        if (null != bundle) {
            if (bundle!!.getBoolean("formPersonalInfo")) {
                if (!pathByUri.isNullOrEmpty()) {
                    NimUIKit.startP2PSession(this, accid, pathByUri)
                } else {
                    NimUIKit.startP2PSession(this, accid)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        App.INSTANCE.resetPush(true)
        val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(company)
        detail?.let {
            Glide.with(this@MainActivity)
                    .load(it.logo)
                    .apply(RequestOptions.circleCropTransform()
                            .error(R.mipmap.ic_launch_pe_round))
                    .into(mainLogo)
        }
    }

    fun update(url: String, bar: MyProgressBar, update: Button, dialog: MaterialDialog, prompt: TextView, force: Int) {
        bar.visibility = View.VISIBLE
        //title.text = "开始下载"
        val fileName = "Saas_${Utils.getVersionName(context)}_${Utils.getYMD(Date(System.currentTimeMillis()))}.apk"
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                //title.text = "下载完成"
                //dialog.dismiss()
                if (force == 1) {
                    dialog.setCanceledOnTouchOutside(true)
                } else {
                    dialog.setCanceledOnTouchOutside(false)
                }
                bar.visibility = View.GONE
                update.visibility = View.VISIBLE
                prompt.visibility = View.VISIBLE
                update.text = "立刻安装"
                if (isWifi()) {
                    prompt.text = "已在Wi-Fi网络下完成下载"
                } else {
                    prompt.text = "已在移动网络下完成下载"
                }
                update.setOnClickListener {
                    dialog.dismiss()
                    var intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.action = Intent.ACTION_VIEW
                    //val uri = Uri.fromFile(File(path))
                    path?.let {
                        if (force == 2) {
                            RetrofitUrlManager.getInstance().removeGlobalDomain()
                            sp.edit { putString(Extras.HTTPURL, "") }
                            Store.store.clearUser(ctx)
                            IMLogout()
                        }
                        val uri = transform(path, intent)
                        intent.setDataAndType(uri, "application/vnd.android.package-archive")
                        startActivity(intent)
                    }
                }
            }

            override fun onDownloading(progress: Int) {
                //title.text = "已下载" + progress + "%"
                //bar.progress = progress
                bar.setProgress(progress)
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
            }

            override fun onDownloadFailed() {
                //showToast("下载失败")
                //title.text = "下载失败"
            }
        })
    }

    private fun transform(param: String, intent: Intent): Uri {
        val uri: Uri
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, FileUtil.getFileProvider(this), File(param))
            //uri = MyFileProvider.getUriForFile(new File(param));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(File(param))
        }
        return uri
    }

    private var mExitTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (System.currentTimeMillis() - mExitTime > 1000) {
//                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                    ToastUtils.showWarnToast("再按一次退出程序", this)
                    mExitTime = System.currentTimeMillis()
                } else {
                    finish()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        DzhClientService.stopService(this)
        stopService<FileFindService>()
    }

    /**
     * 网易云信IM注销
     */
    private fun IMLogout() {
        val xmlDb = XmlDb.open(this)
        xmlDb.set(Extras.NIMACCOUNT, "")
        xmlDb.set(Extras.NIMTOKEN, "")
        NimUIKit.logout()
        NIMClient.getService(AuthService::class.java).logout()
    }

    companion object {
        fun invoke(context: Context, bundle: Bundle, pathByUri: String, accid: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("bundle", bundle)
            intent.putExtra("pathByUri", pathByUri)
            intent.putExtra("accid", accid)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
