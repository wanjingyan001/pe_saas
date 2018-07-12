package com.sogukj.pe.module.register

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import kotlinx.android.synthetic.main.activity_scale_selection.*
import org.jetbrains.anko.find

class ScaleSelectionActivity : ToolbarActivity() {
    lateinit var mAdapter: RecyclerAdapter<String>


    companion object {
        fun startForResult(context: Activity, scale: String) {
            val intent = Intent(context, ScaleSelectionActivity::class.java)
            intent.putExtra(Extras.DATA, scale)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scale_selection)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "规模选择"
        setBack(true)
        val string = intent.extras.getString(Extras.DATA)
        mAdapter = RecyclerAdapter(this) { _adapter, parent, type ->
            ScaleHolder(_adapter.getView(R.layout.item_scale_selection, parent))
        }
        mAdapter.onItemClick = { v, position ->
            mAdapter.selectedPosition = position
            mAdapter.notifyDataSetChanged()
            val intent = Intent()
            if (mAdapter.selectedPosition != -1) {
                intent.putExtra(Extras.DATA, mAdapter.dataList[mAdapter.selectedPosition])
                intent.putExtra(Extras.INDEX, mAdapter.selectedPosition + 1)
                setResult(Extras.RESULTCODE, intent)
            }
            finish()
        }
        val array = resources.getStringArray(R.array.register_organ_scale)
        mAdapter.dataList.addAll(array.toMutableList())

        mAdapter.selectedPosition = array.indexOf(string)
        mScaleList.apply {
            layoutManager = LinearLayoutManager(this@ScaleSelectionActivity)
            addItemDecoration(DividerItemDecoration(this@ScaleSelectionActivity, DividerItemDecoration.VERTICAL))
            adapter = mAdapter
        }
    }


    inner class ScaleHolder(itemView: View) : RecyclerHolder<String>(itemView) {
        private val selectionTv = itemView.find<TextView>(R.id.selectionTv)
        private val selectionIcon = itemView.find<ImageView>(R.id.selectionIcon)
        override fun setData(view: View, data: String, position: Int) {
            selectionTv.text = data
            if (position == mAdapter.selectedPosition) {
                selectionIcon.visibility = View.VISIBLE
            } else {
                selectionIcon.visibility = View.INVISIBLE
            }
        }
    }
}
