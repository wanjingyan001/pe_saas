package com.sogukj.pe.peExtended

import android.content.Context
import android.preference.PreferenceManager
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Consts
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.utils.CharacterParser
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.creditCollection.ShareHolderStepActivity
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.main.ContactsActivity
import java.util.regex.Pattern

/**
 * Created by admin on 2018/5/22.
 */
fun ActivityHelper.removeTeamSelectActivity() {
    activities
            .filterIsInstance<ContactsActivity>()
            .forEach { it.finish() }
}

fun ActivityHelper.removeStep1() {
    activities
            .filterIsInstance<ShareHolderStepActivity>()
            .forEach { it.finish() }
}

fun ActivityHelper.hasCreditListActivity(): Boolean {
    return activities.any { it is ShareholderCreditActivity }
}


val RootPath: String
    get() = if (BuildConfig.DEBUG) Consts.DEV_HTTP_HOST else Consts.HTTP_HOST

/**
 * 限定汉字
 */

val String.firstLetter: String
    get() {
        val pattern = Pattern.compile("[^\\u4E00-\\u9FA5]")
        val matcher = pattern.matcher(this)
        val s = CharacterParser.getInstance().getAlpha(Utils.stringFilter(this.filter { matcher.find() })).toUpperCase()
        return if (s.isEmpty()) {
            ""
        } else {
            s.substring(0, 1)
        }
    }

fun Context.needIm(): Boolean {
    val data = PreferenceManager.getDefaultSharedPreferences(this).getString(Extras.SAAS_BASIC_DATA, "")
    return data.contains("享聊")
}

fun getEnvironment(): String {
    return BuildConfig.ENVIRONMENT
}

fun getIntEnvironment(): Int {
    return BuildConfig.environment
}

fun defaultHeadImg(): Int {
    return when (getEnvironment()) {
        "civc" -> R.mipmap.img_logo_user
        "ht" -> R.mipmap.img_logo_user_ht
        else -> R.mipmap.default_head
    }
}

fun defaultIc(): Int {
    return when (getEnvironment()) {
        "civc" -> R.mipmap.ic_launcher_zd
        "ht" -> R.mipmap.ic_launcher
        "kk" -> R.mipmap.ic_launcher_kk
        "yge" -> R.mipmap.ic_launcher_yge
        else -> R.mipmap.ic_launcher_pe
    }
}