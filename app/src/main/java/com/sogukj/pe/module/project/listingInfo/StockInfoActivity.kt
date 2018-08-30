package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.StockBean
import com.sogukj.pe.service.InfoService
import com.sogukj.pe.service.socket.BusProvider
import com.sogukj.pe.service.socket.DzhConsts
import com.sogukj.pe.service.socket.QidHelper
import com.sogukj.pe.service.socket.WsEvent
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class StockInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    internal var qidHelper = QidHelper(StockInfoActivity.TAG)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_stock_quote)
        setBack(true)
        setTitle("股票行情")
        val red = ContextCompat.getColor(this, R.color.colorRed)
        val green = ContextCompat.getColor(this, R.color.colorGreen)
        val gray = ContextCompat.getColor(this, R.color.colorGray)
    }

    override fun onResume() {
        super.onResume()
        BusProvider.getInstance().register(this)
        getStockInfos()
    }

    private fun getStockInfos() {
        SoguApi.getService(application, InfoService::class.java)
                .stockInfo(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        if (null != data){
                            requestData(data)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })

    }

    private fun requestData(data: StockBean) {
        if (null != data.stockcode){
            DzhConsts.dzh_cancel(qidHelper.getQid("detail_quote"))
            DzhConsts.dzh_stkdata_detail(data.stockcode.toString(), 1, qidHelper.getQid("detail_quote"))
        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event : WsEvent){
        when(event.state){
            Extras.MESSAGE -> {
                try {
//                    val stkData = JsonBinder.fromJson(event.data, StkData::class.java)
//                    if (stkData.Err == 0){
//                        if (stkData.Qid.equals(qidHelper.getQid("hkquote"))){
//                            val data = stkData.data.repDataStkData
//                            if (null != data && data.size > 0){
//                                activity!!.runOnUiThread(object : Runnable{
//                                    override fun run() {
//                                        setHkIndexData(data)
//                                    }
//                                })
//                            }
//                        }
//                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }

            Extras.CONNECTED -> {

            }

            else -> {

            }

        }

    }
    override fun onPause() {
        super.onPause()
        BusProvider.getInstance().unregister(this)
    }

    companion object {
        val TAG = StockInfoActivity::class.java!!.getSimpleName()
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, StockInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
