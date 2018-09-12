package com.sogukj.pe.module.creditCollection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_credit_select.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx

@Route(path = ARouterPath.CreditSelectActivity)
class CreditSelectActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_select)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        toolbar?.setBackgroundColor(resources.getColor(R.color.transparent))
        title = "征信类型选择"
        setBack(true)
        typeLayout1.clickWithTrigger {
            startActivity<HundredSearchActivity>()
        }
        typeLayout2.clickWithTrigger {
            oldCredit()
        }
    }


    private fun oldCredit(){
        XmlDb.open(this).set("INNER", "FALSE")
        val first = XmlDb.open(this).get("FIRST", "TRUE")
        if (first == "FALSE") {
            SoguApi.getService(application, CreditService::class.java)
                    .showCreditList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (payload.payload == null) {
                                ShareHolderStepActivity.start(context, 1, 0, "")
                            } else {
                                if (payload.payload!!.size == 0) {
                                    ShareHolderStepActivity.start(context, 1, 0, "")
                                } else {
                                    val project = ProjectBean()
                                    project.name = ""
                                    project.company_id = 0
                                    ShareholderCreditActivity.start(context, project)
                                }
                            }
                        } else {
                            ShareHolderStepActivity.start(context, 1, 0, "")
                        }
                    }, { e ->
                        Trace.e(e)
                        ShareHolderStepActivity.start(context, 1, 0, "")
                    })
        } else if (first == "TRUE") {
            ShareHolderDescActivity.start(context, ProjectBean(), "OUTER")
            XmlDb.open(this).set("FIRST", "FALSE")
        }
    }
}
