package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.DzhResp
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.StkDataDetail
import com.sogukj.pe.bean.StockBean
import com.sogukj.pe.peUtils.StockUtil
import com.sogukj.pe.service.InfoService
import com.sogukj.pe.service.socket.*
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_stock_quote.*
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
    private var code = "";
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
            code = data.stockcode!!
            Log.e("TAG","code == " + code)
            DzhConsts.dzh_cancel(qidHelper.getQid("detail_quote"))
            DzhConsts.dzh_stkdata_detail(Utils.getStockCode(data.stockcode), 1, qidHelper.getQid("detail_quote"))
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event : WsEvent){
        when(event.state){
            Extras.MESSAGE -> {
                try {
                    Log.e("TAG","event.data ===" + event.data)
                    val dzh = JsonBinder.fromJson(event.data, DzhResp::class.java);
                    if (dzh.Err == 0){
                        if (dzh.Qid.equals(qidHelper.getQid("detail_quote"))) {
                            val data = JsonBinder.fromJson(event.data, StkDataDetail::class.java)
                            val repDataStkData = data.getData().getRepDataStkData().get(0)
                            if (null == repDataStkData) return
                            runOnUiThread {
                                setStockQuoteData(repDataStkData)
                            }

                        }
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }

            Extras.CONNECTED -> {
                getStockInfos()
            }

            else -> {

            }
        }
    }

    private fun setStockQuoteData(repDataStkData: StkDataDetail.Data.RepDataStkData) {
        StockUtil.setCacheZuiXinJiaText(tv_price,repDataStkData.zuiXinJia,repDataStkData.zhangDie,repDataStkData.shiFouTingPai,"停牌")
        tv_name.text = repDataStkData.zhongWenJianCheng
        tv_obj.text = code
        StockUtil.setColorText(tv_zhangdie, repDataStkData.zhangDie, repDataStkData.shiFouTingPai, "")
        StockUtil.setColorText(tv_zhangfu, repDataStkData.zhangFu, repDataStkData.shiFouTingPai, "%")
        tv_update_time.text = String.format(Utils.getTime(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"),R.string.tv_update_time)
        StockUtil.setText(tv_zhangting, repDataStkData.getZhangTing(), repDataStkData.getShiFouTingPai(), "")
        StockUtil.setText(tv_dieting, repDataStkData.getDieTing(), repDataStkData.getShiFouTingPai(), "")

        StockUtil.setColorText(tv_open, repDataStkData.getKaiPanJia(), repDataStkData.getZuoShou(), repDataStkData.getShiFouTingPai(), "")
        StockUtil.setText(tv_close, repDataStkData.zuoShou, repDataStkData.shiFouTingPai, "")

        StockUtil.setColorText(tv_high, repDataStkData.zuiGaoJia, repDataStkData.zuoShou, repDataStkData.shiFouTingPai)
        StockUtil.setColorText(tv_low, repDataStkData.zuiDiJia, repDataStkData.zuoShou, repDataStkData.shiFouTingPai)

        StockUtil.setTextUnit(tv_shizhi, repDataStkData.getZongShiZhi().toLong() * 10000, repDataStkData.getShiFouTingPai())
        StockUtil.setTextUnit(tv_shizhi_liutong, repDataStkData.liuTongShiZhi.toLong() * 10000, repDataStkData.shiFouTingPai)
        StockUtil.setChengJiaoLiangText(tv_liang, repDataStkData.getChengJiaoLiang(), repDataStkData.getShiFouTingPai())
        StockUtil.setChengJiaoEText(tv_e, repDataStkData.getChengJiaoE(), repDataStkData.getShiFouTingPai())

        StockUtil.setText(tv_jin, repDataStkData.getShiJingLv(), repDataStkData.getShiFouTingPai(), "")
        StockUtil.setText(tv_pe, repDataStkData.getShiYingLv(), repDataStkData.getShiFouTingPai(), "")
        StockUtil.setText(tv_zhenfu, repDataStkData.getZhenFu(), repDataStkData.getShiFouTingPai(), "%")
        StockUtil.setText(tv_huanshou, repDataStkData.getHuanShou(), repDataStkData.getShiFouTingPai(), "%")
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
