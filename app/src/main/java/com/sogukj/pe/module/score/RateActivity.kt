package com.sogukj.pe.module.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.ArrayPagerAdapter
import com.sogukj.pe.bean.GradeCheckBean
import kotlinx.android.synthetic.main.activity_rate.*
import org.jetbrains.anko.textColor


class RateActivity : ToolbarActivity() {

    companion object {
        /**
         * check_person 被评分人信息
         * isShow-- 是否展示页面  true为展示页面，false是打分界面
         */
        fun start(ctx: Context?, check_person: GradeCheckBean.ScoreItem, isShow: Boolean, type: Int) {
            val intent = Intent(ctx, RateActivity::class.java)
            intent.putExtra(Extras.DATA, check_person)
            intent.putExtra(Extras.FLAG, isShow)
            intent.putExtra(Extras.TYPE, type)
            ctx?.startActivity(intent)
        }
    }

    lateinit var person: GradeCheckBean.ScoreItem
    var isShow = false
    var type = 0

    lateinit var fragments: Array<BaseFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)

        setBack(true)
        title = "考核评分"
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.icon_back_gray)
        }

        person = intent.getSerializableExtra(Extras.DATA) as GradeCheckBean.ScoreItem
        isShow = intent.getBooleanExtra(Extras.FLAG, false) // = false 打分界面，true展示界面
        type = intent.getIntExtra(Extras.TYPE, 0)

        //1=>其他模版 2=>风控部模版 3=>投资部模版
//        if (person.type == 3) {
//            fragments = arrayOf(
//                    InvestManageFragment.newInstance(person, isShow)
//            )
//        } else if (person.type == 2) {
//            fragments = arrayOf(
//                    FengKongFragment.newInstance(person, isShow)
//            )
//        } else if (person.type == 1) {
//            fragments = arrayOf(
//                    RateFragment.newInstance(person, isShow, type)
//            )
//        } else {
//        }

        fragments = arrayOf(
                RateFragment.newInstance(person, isShow, type)
        )
        var adapter = ArrayPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = adapter
    }
}
