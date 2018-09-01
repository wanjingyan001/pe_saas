package com.sogukj.pe.service.socket

/**
 * Created by CH-ZH on 2018/8/30.
 */
class DzhConsts {
    companion object {
        //取消订阅
        val DZH_CANCEL = "/cancel?qid=%s"
        //个股详情
        val DZH_STKDATA_DETAIL = "/stkdata?obj=%s&field=ZhongWenJianCheng,ZuiXinJia,ZhangFu,ZhangDie,FenZhongZhangFu5,ShiFouTingPai,ZuiGaoJia,ZuiDiJia,ChengJiaoLiang,ChengJiaoE,KaiPanJia,ZuoShou,ZhangTing,DieTing,LiangBi,JunJia,HuanShou,ZhenFu,WeiBi,WaiPan,NeiPan,LiuTongAGu,LiuTongShiZhi,ZongShiZhi,ShiYingLv,ShiJingLv,ZongGuBen&sub=%d&qid=%s"
        open fun dzh_cancel(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_CANCEL, *args)))
        }

        fun dzh_stkdata_detail(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_STKDATA_DETAIL, *args)))
        }
    }
}