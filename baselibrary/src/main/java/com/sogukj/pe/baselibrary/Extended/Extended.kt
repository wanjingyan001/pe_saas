package com.sogukj.pe.baselibrary.Extended

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.interf.MoneyUnit
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.OnClickFastListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.windowManager
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * kotlin扩展方法
 * Created by admin on 2018/3/30.
 */

inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}

inline fun <reified T> Gson.arrayFromJson(json: String): List<T> {
    return Gson().fromJson(json, object : TypeToken<List<T>>() {}.type)
}

fun View.setOnClickFastListener(listener: OnClickFastListener.(v: View) -> Unit) {
    this.setOnClickListener {
        listener.invoke(object : OnClickFastListener() {
            override fun onFastClick(v: View) {

            }
        }, it)
    }
}

val String?.withOutEmpty: String
    get() = (this.isNullOrBlank() || this == "null").yes { "--" }.otherWise { this!! }

val String?.noEmpty: String
    get() = (this.isNullOrBlank() || this == "null").yes { "无" }.otherWise { this!! }

fun <T> List<T>?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()
/**
 * 判断EditText的text是否不为空
 */
fun EditText.isNullOrEmpty(): Boolean =
        text?.trim().isNullOrEmpty()

/**
 * edittext扩展属性，获取其文本
 */
val EditText.textStr: String
    get() = text.trim().toString().replace(" ", "")

val EditText.noSpace: String
    get() = text.trimStart().trimEnd().toString()

val Any?.jsonStr: String
    get() = if (this == null) "" else Gson().toJson(this)

val Any?.jsonOrNull: String?
    get() = if (this == null) null else {
        if (this is String?) {
            if (this.isEmpty()){
                null
            }else{
                this
            }
        } else {
            Gson().toJson(this)
        }
    }

val TextView.textStr: String
    get() = text.trim().toString().replace(" ", "")

/**
 * 扩展View是否可见，VISIBLE 与 GONE。
 */
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setDrawable(textView: TextView, direct: Int, drawable: Drawable) {

    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    when (direct) {
        0 -> textView.setCompoundDrawables(drawable, null, null, null)
        1 -> textView.setCompoundDrawables(null, drawable, null, null)
        2 -> textView.setCompoundDrawables(null, null, drawable, null)
        3 -> textView.setCompoundDrawables(null, null, null, drawable)
        -1 -> textView.setCompoundDrawables(null, null, null, null)
    }
}

fun <T> Observable<T>.execute(init: Ex_T0_Unit<SubscriberHelper<T>>) {
    val subscriberHelper = SubscriberHelper<T>()
    init(subscriberHelper)
    subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriberHelper)
}


fun CharSequence?.checkEmpty(): CharSequence {
    return if (this == null || this.isEmpty() || this == "null")
        ""
    else
        this
}

fun Number.toMoney(needDecimal: Boolean = false): String {
    return this.toMoney(MoneyUnit.Default, needDecimal)
}

fun Number.toMoney(unit: MoneyUnit, needDecimal: Boolean = false): String {
    val money = Utils.formatMoney(BigDecimal(this.toDouble() / unit.unit))
    return if (needDecimal) money else money.substring(0, money.lastIndexOf("."))
}

fun Number?.toMoneyWithUnit(needDecimal: Boolean = false): String {
    if (this == null || this == 0) {
        return "0"
    } else
        return when (this) {
            in (MoneyUnit.TenThousand.unit until MoneyUnit.TenMillion.unit) -> {
                "${this.toMoney(MoneyUnit.TenThousand, needDecimal)}万"
            }
            in (MoneyUnit.TenMillion.unit until MoneyUnit.Billion.unit) -> {
                "${this.toMoney(MoneyUnit.TenMillion, needDecimal)}千万"
            }
            in (MoneyUnit.Billion.unit until Long.MAX_VALUE) -> {
                "${this.toMoney(MoneyUnit.Billion, needDecimal)}十亿"
            }
            else -> {
                this.toMoney(MoneyUnit.Default, needDecimal)
            }
        }
}


fun <T1, T2, T3> ifNotNull(value1: T1?, value2: T2?, value3: T3?, bothNotNull: (T1, T2, T3) -> (Unit)) {
    if (value1 != null && value2 != null && value3 != null) {
        bothNotNull(value1, value2, value3)
    }
}

fun <T1, T2> ifNotNull(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)) {
    if (value1 != null && value2 != null) {
        bothNotNull(value1, value2)
    }
}

fun <T1, T2> ifNotNullReturnBlo(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)): Boolean {
    return if (value1 != null && value2 != null) {
        bothNotNull(value1, value2)
        true
    } else {
        false
    }
}

fun Context.isWifi(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetInfo = connectivityManager.activeNetworkInfo
    return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun Activity.fullScreen() {
    val sdkInt = Build.VERSION.SDK_INT
    if (sdkInt >= Build.VERSION_CODES.KITKAT) {
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = option
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    } else {
        val attributes = window.attributes
        attributes.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        window.attributes = attributes
    }
}

fun BottomNavigationItem.initNavTextColor(): BottomNavigationItem =
        setActiveColorResource(R.color.main_bottom_bar_color)
                .setInActiveColorResource(R.color.text_3)

fun BottomNavigationItem.initNavTextColor1(): BottomNavigationItem =
        setActiveColorResource(R.color.white)
                .setInActiveColorResource(R.color.text_3)

fun BottomNavigationItem.initNavTextColor2(): BottomNavigationItem =
        setActiveColorResource(R.color.colorPrimary)
                .setInActiveColorResource(R.color.text_3)


val Context.inputIsActive: Boolean
    get() {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return manager.isActive
    }

fun Context.showInput(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (!inputIsActive)
        imm.showSoftInput(view, 0)
}

val Context.screenWidth: Int
    get() = windowManager.defaultDisplay.width

val Context.screenHeight: Int
    get() = windowManager.defaultDisplay.height

val Context.screenWidthDp: Int
    get() {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels / dm.density.toInt()
    }

val Context.screenHeightDp: Int
    get() {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.heightPixels / dm.density.toInt()
    }

fun ViewGroup.childEdtGetFocus() {
    (0 until this.childCount)
            .map { getChildAt(it) }
            .filterIsInstance<EditText>()
            .forEach {
                this.setOnClickListener {
                    it.isFocusable = true
                    it.isFocusableInTouchMode = true
                    it.requestFocus()
                    this.context.showInput(it)
                }
            }
}

/**
 * 用法
 * val mainActivity = requireActivity() as? MainActivity
 *   mainActivity?.apply {
 *  }
 * requireActivity().safeCast<MainActivity> {
 *  }
 */
inline fun <reified T> Any.safeCast(action: T.() -> Unit) {
    if (this is T) {
        this.action()
    }
}


val map: LinkedHashMap<KClass<*>, Function1<*, Any>> = LinkedHashMap()

inline fun <reified V : Any> register(noinline action: Function1<V, Any>) {
    map[V::class] = action
}

/***
 * 设置延迟时间的View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @return T
 */
fun <T : View> T.withTrigger(delay: Long = 600): T {
    triggerDelay = delay
    return this
}

/***
 * 点击事件的View扩展
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener {

    if (clickEnable()) {
        block(it as T)
    }
}

/***
 * 带延迟过滤的点击事件View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.clickWithTrigger(time: Long = 600, block: (T) -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (clickEnable()) {
            block(it as T)
        }
    }
}

private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(1123460103) != null) getTag(1123460103) as Long else 0
    set(value) {
        setTag(1123460103, value)
    }

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(1123461123) != null) getTag(1123461123) as Long else -1
    set(value) {
        setTag(1123461123, value)
    }

private fun <T : View> T.clickEnable(): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        flag = true
    }
    triggerLastTime = currentClickTime
    return flag
}

private var mClickTime = 0L
fun isClickEnable(delay: Long): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - mClickTime >= delay) {
        flag = true
    }
    mClickTime = currentClickTime
    return flag
}


fun <T : Any> T.deepCopy(): T {
    //①判断是否为数据类，不是的话直接返回
    if (!this::class.isData) {
        return this
    }
    //②数据类一定有主构造器，不用怕，这里放心使用 !! 来转为非空类型
    return this::class.primaryConstructor!!.let { primaryConstructor ->
        primaryConstructor.parameters
                .map { parameter ->
                    val value =
                            (this::class as KClass<T>).declaredMemberProperties.first { it.name == parameter.name }.get(this)
                    //③如果主构造器参数类型为数据类，递归调用
                    if ((parameter.type.classifier as? KClass<*>)?.isData == true) {
                        parameter to value?.deepCopy()
                    } else {
                        parameter to value
                    }
                }
                .toMap()
                .let(primaryConstructor::callBy)
    }
}