package com.sogukj.pe.module.im

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.widget.TextView
import com.amap.api.mapcore.util.fm
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import kotlinx.android.synthetic.main.activity_ack_msg_info.*
import org.jetbrains.anko.find

class AckMsgInfoActivity : ToolbarActivity() {

    companion object {
        private val titles = arrayOf("已读人员列表", "未读人员列表")
        fun start(context: Context, imMsg: IMMessage) {
            val intent = Intent(context, AckMsgInfoActivity::class.java)
            intent.putExtra(Extras.DATA, imMsg)
            context.startActivity(intent)
        }
    }

    private lateinit var imMessage: IMMessage
    private lateinit var adapter: AckPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ack_msg_info)
        title = "消息人接收列表"
        setBack(true)
        imMessage = intent.getSerializableExtra(Extras.DATA) as IMMessage
        initPages()
    }

    private fun initPages() {
        adapter = AckPageAdapter(supportFragmentManager, titles)
        ackMsgContent.adapter = adapter
        tabLayout.setViewPager(ackMsgContent)
        tabLayout.setTabViewFactory { parent, _ ->
            parent.removeAllViews()
            for (i in 0 until titles.size) {
                val view = LayoutInflater.from(this).inflate(R.layout.item_msg_read_indicator, parent, false)
                view.find<TextView>(R.id.indicatorTv).text = titles[i]
                parent.addView(view)
            }
        }
    }

    inner class AckPageAdapter(fm: FragmentManager, private val titles: Array<String>) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> ReadAckMsgTabFragment.newInstance(imMessage)
                else -> UnreadAckMsgTabFragment.newInstance(imMessage)
            }
        }

        override fun getCount(): Int = titles.size

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }
}
