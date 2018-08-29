package com.sogukj.pe.baselibrary.Extended

/**
 * Created by admin on 2018/8/29.
 */

sealed class BooleanExt<out T>

object OtherWise : BooleanExt<Nothing>()
class WhitData<T>(val data: T) : BooleanExt<T>()

inline fun <T> Boolean.yes(block: () -> T) =
        when {
            this -> WhitData(block())
            else -> OtherWise
        }

inline fun <T> Boolean.no(block: () -> T) =
        when {
            this -> OtherWise
            else -> WhitData(block())
        }

inline fun <T> BooleanExt<T>.otherWise(block: () -> T) =
        when (this) {
            is OtherWise -> block()
            is WhitData -> this.data
        }