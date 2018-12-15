package com.sogukj.pe.module.user

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.*
import android.support.v4.app.ActivityCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.system.text.ShortMessage
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QQClientNotExistException
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import cn.sharesdk.wechat.utils.WechatClientNotExistException
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.other.PayExpansionActivity
import com.sogukj.pe.module.project.ProjectFocusActivity
import com.sogukj.pe.module.project.ProjectListFragment
import com.sogukj.pe.module.receipt.MineWalletActivity
import com.sogukj.pe.module.register.CreateDepartmentActivity
import com.sogukj.pe.module.register.NewCreateDepartActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.MobLogin
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.ShareUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult
import java.io.File
import java.util.HashMap
import kotlin.collections.ArrayList

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
/**
 * Created by qinfei on 17/7/18.
 */
class UserFragment : ToolbarFragment(), PlatformActionListener {
    override val containerViewId: Int
        get() = R.layout.activity_user

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_title.text = "个人中心"
        toolbar_back.setOnClickListener {
            baseActivity!!.finish()
        }
        SoguApi.getService(baseActivity!!.application, UserService::class.java)
                .userDepart()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            departList.clear()
                            payload.payload?.forEach {
                                departList.add(it)
                            }
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }

        ll_user.clickWithTrigger {
            UserEditActivity.start(activity, departList)
        }
        documentManagement.clickWithTrigger {
            baseActivity?.showProgress("正在读取内存文件")
            startActivity<FileMainActivity>(Extras.DATA to 9,
                    Extras.FLAG to false, Extras.TYPE to false)
        }
        payPackageLayout.clickWithTrigger {
            val permission = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(baseActivity!!,
                        arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_CONTACTS),
                        Extras.REQUESTCODE)
            } else {
                startActivity<PayExpansionActivity>()
            }
        }
        tv_pay_manager.clickWithTrigger {
            //账户付费管理
            startActivity<PayManagerActivity>()
        }
        setting.clickWithTrigger {
            SettingActivity.start(context)
        }
        focus_layout.clickWithTrigger {
            ProjectFocusActivity.start(activity, ProjectListFragment.TYPE_GZ, "关注")
        }
        toolbar_menu.clickWithTrigger {
            //切换用户没有正确显示
            Store.store.getUser(ctx)?.let {
                CardActivity.start(activity, it)
            }
        }
        tv_bind.clickWithTrigger {
            bindingStatus?.let {
                if (it.is_sync == 0) {
                    MobLogin.WeChatLogin(activity!!,{
                        updateBindingStatus(it)
                    }, { showCommonToast("已取消微信绑定") }, {
                        showErrorToast("微信绑定失败")
                    })
                } else {
                    getWxUrl()
                }
            }
        }
        share.clickWithTrigger {
            SoguApi.getService(baseActivity!!.application, UserService::class.java)
                    .getWebConfig()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload?.payload?.apply {
                                var shareBean = CusShareBean()
                                shareBean.shareTitle = this.company!!
                                shareBean.shareContent = "搜股XPE您的专属投资大管家"
                                shareBean.shareUrl = this.web_url!!
                                ShareUtils.share(shareBean, activity!!)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "获取分享链接失败")
                    })
        }

        tv_wallet.clickWithTrigger {
            startActivity<MineWalletActivity>()
        }

        val user = Store.store.getUser(ctx)
        createDep.setVisible((user?.is_admin == 1) or (user?.is_admin == 2))
        createDep.clickWithTrigger {
            user?.let {
                val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
                val detail = Gson().fromJson<MechanismBasicInfo?>(company)
                detail?.let {
                    val name = it.mechanism_name ?: ""
                    val logo = it.logo ?: ""
                    startActivity<CreateDepartmentActivity>(Extras.NAME to name,
                            Extras.CODE to user.phone,
                            Extras.DATA to logo,
                            Extras.FLAG to true,
                            Extras.FLAG2 to false)
                }
            }
        }
        createDep2.clickWithTrigger {
            user?.let {
                val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
                val detail = Gson().fromJson<MechanismBasicInfo?>(company)
                detail?.let {
                    val name = it.mechanism_name ?: ""
                    val logo = it.logo ?: ""
                    startActivity<NewCreateDepartActivity>(Extras.NAME to name,
                            Extras.CODE to user.phone,
                            Extras.DATA to logo,
                            Extras.FLAG to true,
                            Extras.FLAG2 to false)
                }
            }
        }

        adminSetting.setVisible((user?.is_admin == 1) or (user?.is_admin == 2))
        adminSetting.clickWithTrigger {
            startActivityForResult<AdminMainActivity>(Extras.REQUESTCODE)
        }
    }

    fun share(bean: WebConfigBean) {
        val dialog = Dialog(context, R.style.AppTheme_Dialog)
        dialog.setContentView(R.layout.dialog_share)
        val lay = dialog.getWindow()!!.getAttributes()
        lay.height = WindowManager.LayoutParams.WRAP_CONTENT
        lay.width = WindowManager.LayoutParams.MATCH_PARENT
        lay.gravity = Gravity.BOTTOM
        dialog.getWindow()!!.setAttributes(lay)
        dialog.show()

        val tvHead = dialog.findViewById<TextView>(R.id.head) as TextView
        tvHead.text = "分享网址到电脑端即可访问"
        val tvWexin = dialog.findViewById<TextView>(R.id.tv_wexin) as TextView
        val tvQq = dialog.findViewById<TextView>(R.id.tv_qq) as TextView
        val tvCopy = dialog.findViewById<TextView>(R.id.tv_copy) as TextView
        val shareUrl = bean.web_url//news!!.shareUrl
        val shareTitle = bean.company//news!!.shareTitle
        val shareSummry = ""//news!!.title
        val shareImgUrl: String
        when (getEnvironment()) {
            "civc" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_zd.png").toString()
            }
            "ht" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_ht.png").toString()
            }
            "kk" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_kk.png").toString()
            }
            "yge" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_yge.png").toString()
            }
            "sr" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_sr.png").toString()
            }
            else -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "img_logo.png").toString()
            }
        }
        tvCopy.setOnClickListener {
            dialog.dismiss()
            Utils.copy(context, shareUrl)
            showCustomToast(R.drawable.icon_toast_success, "已复制")
        }
        tvQq.setOnClickListener {
            dialog.dismiss()
            val sp = Platform.ShareParams()
            sp.setTitle(shareTitle)
            sp.setText(shareSummry)
            sp.imagePath = shareImgUrl
            //sp.setImageUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1528281900948&di=edeb19905f4920430f816d917c7b24fe&imgtype=0&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F010f87596f13e6a8012193a363df45.jpg%401280w_1l_2o_100sh.jpg")//网络图片rul
            sp.setTitleUrl(shareUrl)
            val qq = ShareSDK.getPlatform(QQ.NAME)
            qq.platformActionListener = this
            qq.share(sp)
        }
        tvWexin.setOnClickListener {
            dialog.dismiss()
            val sp = cn.sharesdk.framework.Platform.ShareParams()
            sp.setShareType(cn.sharesdk.framework.Platform.SHARE_WEBPAGE)//非常重要：一定\要设置分享属性
            sp.setTitle(shareTitle)  //分享标题
            sp.setText(shareSummry)   //分享文本
//            if (null != news!!.imgUrl) {
//                sp.imageUrl = shareImgUrl//网络图片rul
//            } else {
//                sp.imagePath = shareImgUrl//
//            }
            sp.imagePath = shareImgUrl
            sp.setUrl(shareUrl)
            val wechat = ShareSDK.getPlatform(Wechat.NAME)
            wechat.platformActionListener = this
            wechat.share(sp)
        }

    }


    override fun onComplete(platform: Platform, i: Int, hashMap: HashMap<String, Any>) {
        if (platform.name == WechatMoments.NAME) {// 判断成功的平台是不是朋友圈
            mHandler.sendEmptyMessage(1)
        } else if (platform.name == Wechat.NAME) {
            mHandler.sendEmptyMessage(2)
        } else if (platform.name == SinaWeibo.NAME) {
            mHandler.sendEmptyMessage(3)
        } else if (platform.name == QQ.NAME) {
            mHandler.sendEmptyMessage(4)
        } else if (platform.name == QZone.NAME) {
            mHandler.sendEmptyMessage(5)
        } else if (platform.name == ShortMessage.NAME) {
            mHandler.sendEmptyMessage(6)
        }
    }

    override fun onError(platform: Platform, i: Int, throwable: Throwable) {
        throwable.printStackTrace()

        if (throwable is WechatClientNotExistException) {
            mHandler.sendEmptyMessage(8)
        } else if (throwable is PackageManager.NameNotFoundException) {
            mHandler.sendEmptyMessage(9)
        } else if (throwable is QQClientNotExistException || throwable is cn.sharesdk.tencent.qq.QQClientNotExistException) {
            mHandler.sendEmptyMessage(10)
        } else {
            mHandler.sendEmptyMessage(11)
        }

    }

    override fun onCancel(platform: Platform, i: Int) {
        mHandler.sendEmptyMessage(7)
    }

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> showCustomToast(R.drawable.icon_toast_success, "朋友圈分享成功")
                2 -> showCustomToast(R.drawable.icon_toast_success, "微信分享成功")
                3 -> showCustomToast(R.drawable.icon_toast_success, "新浪微博分享成功")
                4 -> showCustomToast(R.drawable.icon_toast_success, "QQ分享成功")
                5 -> showCustomToast(R.drawable.icon_toast_success, "QQ空间分享成功")
                6 -> showCustomToast(R.drawable.icon_toast_success, "短信分享成功")
                7 -> showCustomToast(R.drawable.icon_toast_common, "取消分享")
                8 -> showCustomToast(R.drawable.icon_toast_common, "请安装微信")
                9 -> showCustomToast(R.drawable.icon_toast_common, "请安装微博")
                10 -> showCustomToast(R.drawable.icon_toast_common, "请安装QQ")
                11 -> showCustomToast(R.drawable.icon_toast_fail, "分享失败")
                else -> {
                }
            }
        }

    }

    val departList = ArrayList<DepartmentBean>()


    private fun getBelongBean(userId: Int) {
        SoguApi.getService(ctx, UserService::class.java)
                .getProject(userId)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                loadStage(it.xm!!)
                                it.gz?.let {
                                    tv_6.text = it.count.toString()
                                }
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }
                }
    }

    fun loadStage(stageList: ArrayList<ProjectBelongBean.Cell1>) {
        var size = stageList.size

        val wm = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        val screenW = dm.widthPixels
        stages.removeAllViews()
        for (i in 0..(size - 1)) {
            var params: LinearLayout.LayoutParams
            if (size <= 5) {
                params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
                params.width = screenW / 5
            } else {
                params = LinearLayout.LayoutParams(screenW / 5 + 5, LinearLayout.LayoutParams.MATCH_PARENT)
                params.width = screenW / 5 + 5
            }
            var textView = TextView(context)
            textView.layoutParams = params
            textView.gravity = Gravity.CENTER
            textView.background = resources.getDrawable(R.drawable.selector_user_item)
            val str1 = stageList.get(i).count.toString()
            val str2 = stageList.get(i).name
            val sStr = SpannableString(str1 + "\n" + str2)

            sStr.setSpan(AbsoluteSizeSpan(Utils.spToPx(context, 18)), 0, str1.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sStr.setSpan(ForegroundColorSpan(Color.parseColor("#282828")), 0, str1.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            sStr.setSpan(AbsoluteSizeSpan(Utils.spToPx(context, 12)), str1.length + 1, sStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sStr.setSpan(ForegroundColorSpan(Color.parseColor("#a0a4aa")), str1.length + 1, sStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            textView.text = sStr

            textView.setOnClickListener {
                ProjectFocusActivity.start(activity, stageList.get(i).type!!, stageList.get(i).name!!)
            }

            stages.addView(textView)
        }
    }

    override fun onResume() {
        super.onResume()
        getBindingStatus()
        val user = Store.store.getUser(ctx)
        user?.uid?.let { getBelongBean(it) }
        if (null != user?.uid) {
            SoguApi.getService(ctx, UserService::class.java)
                    .userInfo(user.uid!!)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                payload.payload?.apply {
                                    Store.store.setUser(ctx, this)
                                    updateUser(this)
                                }
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }

                        }
                    }
        }
    }

    private fun updateUser(user: UserBean?) {
        if (null == user) return
        tv_name?.text = user.name
        tv_position?.text = user.position

        if (!TextUtils.isEmpty(user.email))
            tv_mail?.text = user.email
        if (user.url != null && !TextUtils.isEmpty(user.url)) {
            Glide.with(this@UserFragment)
                    .load(MyGlideUrl(user.headImage()))
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .transition(GenericTransitionOptions())
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user.name.first()
                            iv_user.setChar(ch)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                    })
                    .into(iv_user)
        } else {
            val ch = user.name.first()
            iv_user.setChar(ch)
        }
    }

    private fun getWxUrl() {
        SoguApi.getService(ctx, UserService::class.java)
                .getWxQRurl()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                it as LinkedTreeMap<*, *>
                                startActivity<FocusWXActivity>(Extras.URL to it["url"] as String)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    private var bindingStatus: WXBind? = null
    private fun getBindingStatus() {
        SoguApi.getService(ctx, UserService::class.java)
                .getBindingStatus()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                bindingStatus = it
                                if (it.subscribe == 1) {
                                    if (null != rl_bind) {
                                        rl_bind.setVisible(false)
                                    }
                                    if (null != iv_foucs) {
                                        iv_foucs?.imageResource = R.mipmap.icon_wx_focus
                                    }
                                } else {
                                    if (null != rl_bind) {
                                        rl_bind.setVisible(true)
                                    }
                                    if (null != tv_bind) {
                                        tv_bind?.text = if (it.is_sync == 1) "去关注" else "去绑定"
                                    }
                                    if (null != iv_foucs) {
                                        iv_foucs?.imageResource = R.mipmap.icon_wx_unfocus
                                    }
                                }
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }


    private fun updateBindingStatus(openId: String) {
        SoguApi.getService(ctx, UserService::class.java)
                .updateBindingStatus(openId)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            info { payload.payload }
                            getBindingStatus()
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }

    }


    override fun onStop() {
        super.onStop()
        baseActivity?.hideProgress()
    }


    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, UserFragment::class.java))
        }

        fun newInstance(): UserFragment {
            val fragment = UserFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
