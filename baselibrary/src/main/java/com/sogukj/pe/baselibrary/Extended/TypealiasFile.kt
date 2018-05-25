package com.sogukj.pe.baselibrary.Extended

import android.text.Editable
import android.view.View

/**
 * Created by admin on 2018/4/9.
 */
/**
 * 无返回值函数类型别名
 */
typealias T0_Unit = T0_R<Unit>
typealias T1_Unit<T> = T1_R<T, Unit>
typealias T2_Unit<T1, T2> = T2_R<T1, T2, Unit>
typealias T4_Unit<T1, T2, T3, T4> = T4_R<T1, T2, T3, T4, Unit>

/**
 * 有返回值函数类型别名
 */
typealias T0_R<R> = () -> R
typealias T1_R<T, R> = (T) -> R
typealias T2_R<T1, T2, R> = (T1, T2) -> R
typealias T4_R<T1, T2, T3, T4, R> = (T1, T2, T3, T4) -> R

/**
 * 扩展函数类型别名
 */
typealias Ex_T0_R<T, R> = T.() -> R


typealias Ex_T0_Unit<T> = Ex_T0_R<T, Unit>

/**----------------------------下面是具体的类型------------------------------------*/
typealias T0_Boolean = T0_R<Boolean>
typealias View_Unit = T1_Unit<View>
typealias Throwable_Unit = T1_Unit<Throwable>
typealias Editable_Unit = T1_Unit<Editable>

/** textWatcher */
typealias BeforeTextChanged = T4_Unit<CharSequence, Int, Int, Int>