package com.sogukj.pe.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.amap.api.mapcore.util.it
import com.sogukj.pe.R
import com.sogukj.pe.R.id.list
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.item_control_seal_list.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import kotlin.reflect.jvm.internal.impl.load.java.lazy.ContextKt.child
import com.sogukj.pe.R.id.view


/**
 * 审批人箭头分隔符(@drawable/jt)
 * Created by admin on 2018/10/16.
 */
class ApproverItemDecoration constructor(val context: Context, val sizes: List<Int>) : RecyclerView.ItemDecoration() {

    private val mPaint = Paint()
    private var bitmap: Bitmap?
    private var list: List<Int>
//    private var mDraw: Drawable?

    init {
        list = if (sizes.isNotEmpty()) {
            sizes.mapIndexed { index, i ->
                var newValue = sizes[0]
                (1..index).forEach {
                    newValue += sizes[it]
                }
                newValue - 1
            }.subList(0, sizes.size - 1)
        } else {
            listOf()
        }
        AnkoLogger("WJY").info { list }
        mPaint.isAntiAlias = true
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.jt)


//       val a = context.obtainStyledAttributes(intArrayOf(R.drawable.jt))
//        mDraw = a.getDrawable(0)
//        a.recycle()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        if (position > -1) {
            list.forEach {
                Log.d("WJY", it.toString())
                if (parent.getChildAdapterPosition(view) == it) {
                    Log.d("WJY", "===>" + parent.getChildAdapterPosition(view).toString())
                    outRect.set(0, 0,  Utils.dpToPx(parent.context, 20), 0)
//
                }
            }
        }

    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        bitmap?.let { bitmap ->
            (0 until parent.childCount).forEach {
                val view = parent.getChildAt(it)
                val params = parent.layoutParams as ConstraintLayout.LayoutParams
                val header = view.find<CircleImageView>(R.id.approverHeader)
                val index = parent.getChildAdapterPosition(view)
                if (list.contains(index)) {
                    val left = view.right + params.rightMargin
                    c.drawBitmap(bitmap, left.toFloat() + Utils.dpToPx(parent.context, 5), (header.height - bitmap.height).toFloat(), mPaint)
                }
            }
        }
    }

}