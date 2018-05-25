package com.sogukj.pe.module.score

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.XmlDb
import kotlinx.android.synthetic.main.activity_rule.*
import org.jetbrains.anko.textColor

class RuleActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, RuleActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule)

        setBack(true)
        title = "评分细则"
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        web.settings.javaScriptEnabled = true
        web.loadUrl(XmlDb.open(context).get(Extras.RULE, ""))
    }
}
