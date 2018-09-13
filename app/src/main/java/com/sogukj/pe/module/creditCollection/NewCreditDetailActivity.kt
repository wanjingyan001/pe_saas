package com.sogukj.pe.module.creditCollection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.NewCreditInfo
import kotlinx.android.synthetic.main.activity_new_credit_detail.*
import org.jetbrains.anko.textColor

class NewCreditDetailActivity : ToolbarActivity() {
    private lateinit var info: NewCreditInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_credit_detail)
        title = "个人资质"
        setBack(true)
        info = intent.getParcelableExtra(Extras.BEAN)
        info.apply {
            monthlyAverage.text = (avg_amt ?: "无").isEmpty().yes { "无" }.otherWise { avg_amt }
            recentIncome.text = (rcnt_income ?: "无").isEmpty().yes { "无" }.otherWise { rcnt_income }
            longTermIncome.text = (long_income ?: "无").isEmpty().yes { "无" }.otherWise { long_income }
            fluctuation.text = (income_chg ?: "无").isEmpty().yes { "无" }.otherWise { income_chg }
            stable.text = (regincome_level ?: "无").isEmpty().yes { "无" }.otherWise { regincome_level }
            setLevelBg(stabilityTv2Num, rcnt_econ ?: "")
            setLevelBg(stabilityTv3Num, long_econ ?: "")
            setLevelBg(stabilityTv4Num, noregincome_lst_mons ?: "")
            setPreferenceBg(stabilityTv5Num, life_cons ?: "")
            setPreferenceBg(stabilityTv6Num, digi_cons ?: "")
            setPreferenceBg(stabilityTv7Num, trav_cons ?: "")
            setPreferenceBg(stabilityTv8Num, invest_cons ?: "")
            num1.text = (income_prov ?: "无").isEmpty().yes { "无" }.otherWise { income_prov }
            num2.text = (if_house ?: "无").isEmpty().yes { "无" }.otherWise { if_house }
            num3.text = (business_type ?: "无").isEmpty().yes { "无" }.otherWise { business_type }
            num4.text = (if_car ?: "无").isEmpty().yes { "无" }.otherWise { if_car }
            num5.text = (noincome_lst_mons ?: "无").isEmpty().yes { "无" }.otherWise { noincome_lst_mons }
            num6.text = (avg_fre ?: "无").isEmpty().yes { "无" }.otherWise { avg_fre }
        }
    }


    private fun setLevelBg(tv: TextView, levelStr: String) {
        levelStr.isEmpty().yes {
            tv.text = "无"
            tv.textColor = resources.getColor(R.color.text_1)
            tv.setBackgroundResource(0)
        }.otherWise {
            when (levelStr.toDouble()) {
                in 0.0..0.3 -> tv.setBackgroundResource(R.drawable.bg_stability_type1)
                in 0.3..0.6 -> tv.setBackgroundResource(R.drawable.bg_stability_type2)
                in 0.6..1.0 -> tv.setBackgroundResource(R.drawable.bg_stability_type3)
            }
            tv.textColor = resources.getColor(R.color.white)
            tv.text = levelStr
        }

    }

    private fun setPreferenceBg(tv: TextView, levelStr: String) {
        levelStr.isEmpty().yes {
            tv.text = "无"
            tv.textColor = resources.getColor(R.color.text_1)
            tv.setBackgroundResource(0)
        }.otherWise {
            when (levelStr.toDouble()) {
                in 0.0..0.3 -> tv.setBackgroundResource(R.drawable.bg_stability_type4)
                in 0.3..0.6 -> tv.setBackgroundResource(R.drawable.bg_stability_type2)
                in 0.6..1.0 -> tv.setBackgroundResource(R.drawable.bg_stability_type5)
            }
            tv.textColor = resources.getColor(R.color.white)
            tv.text = levelStr
        }

    }
}
