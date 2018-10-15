package com.sogukj.pe.module.other

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.toMoney
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PackageBean
import com.sogukj.pe.bean.PackageChild
import kotlinx.android.synthetic.main.activity_pay_result.*
import org.jetbrains.anko.startActivity

class PayResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_result)
        Utils.setWindowStatusBarColor(this, R.color.main_bottom_bar_color)
        val packageBean = intent.getParcelableExtra<PackageBean>(Extras.DATA)
        val child = intent.getParcelableExtra<PackageChild>(Extras.BEAN)
        mPackageName.text = packageBean.mealName
        PayName.text = packageBean.mealName
        amount.text = child.name
        payPlace.text = "￥${child.price.toMoney()}"
        newTotal.text = child.name

        val i = packageBean.max - packageBean.used
        if (packageBean.mealName == "项目套餐") {
            oldTotal.text = "${packageBean.max}个项目"
            oldSurplusNum.text = "剩余$i"
            tv4.text = "购买后项目总个数"
            if (child.quantity == 0){
                surplus.text = "无限个"
            }else{
                surplus.text = "${child.quantity + i}个"
            }
        } else if (packageBean.mealName == "征信套餐") {
            oldTotal.text = "${packageBean.max}次征信"
            oldSurplusNum.text = "剩余$i"
            tv4.text = "购买后征信总次数"
            surplus.text = "${child.quantity + i}次"
        }




        toolbar_back.clickWithTrigger {
            finish()
        }
        continuePay.clickWithTrigger {
            finish()
        }
    }
}
