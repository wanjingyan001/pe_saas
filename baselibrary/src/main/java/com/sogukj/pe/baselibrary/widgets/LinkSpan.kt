package com.sogukj.pe.baselibrary.widgets

import android.text.TextPaint
import android.text.style.UnderlineSpan

/**
 * Created by qinfei on 17/9/22.
 */
class LinkSpan : UnderlineSpan() {

    public override fun updateDrawState(ds: TextPaint) {
        ds.color = ds.linkColor
        ds.isUnderlineText = false
    }
}