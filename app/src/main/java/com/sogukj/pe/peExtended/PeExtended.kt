package com.sogukj.pe.peExtended

import com.sogukj.pe.BuildConfig
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.utils.CharacterParser
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.module.creditCollection.ShareHolderStepActivity
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.main.ContactsActivity

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

/**
 * 限定汉字
 */
val String.firstLetter: String
    get() {
        return CharacterParser.getInstance().getAlpha(Utils.stringFilter(this)).toUpperCase().substring(0, 1)
    }


fun getEnvironment(): String {
    return BuildConfig.ENVIRONMENT
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