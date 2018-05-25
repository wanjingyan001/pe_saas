package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_stock_quote.*
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class StockInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_stock_quote)
        setBack(true)
        setTitle("股票行情")
        val red = ContextCompat.getColor(this, R.color.colorRed)
        val green = ContextCompat.getColor(this, R.color.colorGreen)
        val gray = ContextCompat.getColor(this, R.color.colorGray)
        SoguApi.getService(application,InfoService::class.java)
                .stockInfo(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.apply {
                            if (null != hexm_float_price && !TextUtils.isEmpty(hexm_float_price?.trim())) {
                                val zhangdie = hexm_float_price!!.toFloat()
                                if (zhangdie > 0) {
                                    tv_price.textColor = red
                                    tv_zhangdie.textColor = red
                                    tv_zhangfu.textColor = red
                                } else if (zhangdie < 0) {
                                    tv_price.textColor = green
                                    tv_zhangdie.textColor = green
                                    tv_zhangfu.textColor = green
                                } else {
                                    tv_price.textColor = gray
                                    tv_zhangdie.textColor = gray
                                    tv_zhangfu.textColor = gray
                                }
                            }

                            tv_name.text = stockname
                            tv_obj.text = String.format("%06d", stockcode)
                            tv_price.text = hexm_curPrice
                            tv_zhangdie.text = "$hexm_float_price"
                            tv_zhangfu.text = "$hexm_float_rate"
                            tv_update_time.text = "更新时间:${timeshow}"
                            tv_zhangting.text = tmaxprice
                            tv_dieting.text = tminprice
                            tv_open.text = topenprice
                            tv_close.text = pprice
                            tv_high.text = thighprice
                            tv_low.text = tlowprice
                            tv_shizhi.text = tvalue
                            tv_shizhi_liutong.text = flowvalue
                            tv_liang.text = tamount
                            tv_e.text = tamounttotal
                            tv_jin.text = tvaluep
                            tv_pe.text = fvaluep
                            tv_zhenfu.text = trange
                            tv_huanshou.text = tchange
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, StockInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
