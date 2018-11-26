package com.sogukj.pe.module.creditCollection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.Validators.or
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.noEmpty
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.NewCreditInfo
import kotlinx.android.synthetic.main.activity_new_credit_detail.*
import org.jetbrains.anko.info
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
            monthlyAverage.text = avg_amt.noEmpty
            recentIncome.text = rcnt_income.noEmpty
            longTermIncome.text = long_income.noEmpty
            fluctuation.text = income_chg.noEmpty
            stable.text = regincome_level.noEmpty
            setLevelBg(stabilityTv2Num, rcnt_econ ?: "")
            setLevelBg(stabilityTv3Num, long_econ ?: "")
            setLevelBg(stabilityTv4Num, noregincome_lst_mons ?: "")
            setPreferenceBg(stabilityTv5Num, life_cons ?: "")
            setPreferenceBg(stabilityTv6Num, digi_cons ?: "")
            setPreferenceBg(stabilityTv7Num, trav_cons ?: "")
            setPreferenceBg(stabilityTv8Num, invest_cons ?: "")
            num1.text = income_prov.noEmpty
            num2.text = if_house.noEmpty
            num3.text = business_type.noEmpty
            num4.text = if_car.noEmpty
            num5.text = noincome_lst_mons.noEmpty
            num6.text = avg_fre.noEmpty
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
                in 0.4..0.6 -> tv.setBackgroundResource(R.drawable.bg_stability_type2)
                in 0.7..1.0 -> tv.setBackgroundResource(R.drawable.bg_stability_type3)
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
                in 0.4..0.6 -> tv.setBackgroundResource(R.drawable.bg_stability_type2)
                in 0.7..1.0 -> tv.setBackgroundResource(R.drawable.bg_stability_type5)
            }
            tv.textColor = resources.getColor(R.color.white)
            tv.text = levelStr
        }

    }
}
