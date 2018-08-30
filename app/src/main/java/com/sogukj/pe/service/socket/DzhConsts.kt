package com.sogukj.pe.service.socket

/**
 * Created by CH-ZH on 2018/8/30.
 */
class DzhConsts {
    companion object {
        //大智慧
        const val DZH_APP_ID = "dcdc435cc4aa11e587bf0242ac1101de"
        const val DZH_SECRET_KEY = "InsQbm2rXG5z"
        //取消订阅
        val DZH_CANCEL = "/cancel?qid=%s"
        //大行情
        val DZH_STKDATA = "/stkdata?obj=%s&field=ZhongWenJianCheng,ZuiXinJia,ZhangFu,ShiFouTingPai,ZhangDie,FenZhongZhangFu5,LingZhangGu,DaDanDangRiLiuRuE&count=%d&sub=%d&qid=%s"
        //个股详情
        val DZH_STKDATA_DETAIL = "/stkdata?obj=%s&field=ZhongWenJianCheng,ZuiXinJia,ZhangFu,ZhangDie,FenZhongZhangFu5,ShiFouTingPai,ZuiGaoJia,ZuiDiJia,ChengJiaoLiang,ChengJiaoE,KaiPanJia,ZuoShou,ZhangTing,DieTing,LiangBi,JunJia,HuanShou,ZhenFu,WeiBi,WaiPan,NeiPan,LiuTongAGu,LiuTongShiZhi,ZongShiZhi,ShiYingLv,ShiJingLv,ZongGuBen&sub=%d&qid=%s"
        //大行情 排序
        val DZH_STKDATA_ORDERBY = "/stkdata?obj=%s&field=ZhongWenJianCheng,ZuiXinJia,ZhangFu,ShiFouTingPai,ZhangDie,FenZhongZhangFu5,LingZhangGu&orderby=%s&desc=%b&count=%d&mode=2&sub=%d&qid=%s"
        //相关个股
        val DZH_STKDATA_THEME_ORDERBY = "/stkdata?gql=block=股票\\\\大智慧自定义\\\\指数板块\\\\%s&field=ZhongWenJianCheng,ZhangFu,ZhangDie,ZuiXinJia,ShiFouTingPai&orderby=ZhangFu&desc=true&count=%d&sub=1&mode=2&qid=%s"
        //大行情 A股排序
        val DZH_STKDATA_GQL = "/stkdata?gql=block=股票\\\\市场分类\\\\全部A股&field=ZhongWenJianCheng,ZuiXinJia,ZhangFu,ShiFouTingPai,ZhangDie,FenZhongZhangFu5&orderby=%s&desc=%b&start=%d&count=%d&mode=2&sub=%d&qid=%s"
        //行业涨幅
        val DZH_SORT = "/sort/range?&field=ZhangFu&market=%s&desc=%b&count=%d&mode=2&sub=%d&qid=%s"
        //分时
        val DZH_MIN = "/quote/min?obj=%s&sub=%d&qid=%s"
        //五日
        val DZH_DAY = "/quote/kline?obj=%s&period=%s&begin_time=%s&start=1&qid=%s"
        //大行情-买卖
        val DZH_STKDATA_SELL = "/stkdata?obj=%s&field=ZuoShou,WeiTuoMaiRuJia1,WeiTuoMaiRuJia2,WeiTuoMaiRuJia3,WeiTuoMaiRuJia4,WeiTuoMaiRuJia5,WeiTuoMaiRuLiang1,WeiTuoMaiRuLiang2,WeiTuoMaiRuLiang3,WeiTuoMaiRuLiang4,WeiTuoMaiRuLiang5,WeiTuoMaiChuJia1,WeiTuoMaiChuJia2,WeiTuoMaiChuJia3,WeiTuoMaiChuJia4,WeiTuoMaiChuJia5,WeiTuoMaiChuLiang1,WeiTuoMaiChuLiang2,WeiTuoMaiChuLiang3,WeiTuoMaiChuLiang4,WeiTuoMaiChuLiang5&sub=%d&qid=%s"
        //分笔
        val DZH_TICK = "/quote/tick?obj=%s&start=-16&sub=%d&qid=%s"
        //K线
        val DZH_KLINE = "/quote/kline?obj=%s&period=%s&start=%d&count=%d&split=%d&sub=%d&qid=%s"
        //ma
        val DZH_INDICATOR = "/indicator/calc?obj=%s&start=-161&count=161&period=%s&name=MA&parameter=P1=5,P2=10,P3=20,P4=30,P5=60,P6=120&split=%d&output=json&sub=%d&qid=%s"
        val DZH_MA = "/indicator/calc?obj=%s&name=MA&start=-161&count=161&period=%s&parameter=%s&split=%d&output=json&sub=%d&qid=%s"
        val DZH_MA2 = "/indicator/calc?obj=%s&name=MA&start=%d&count=%d&period=%s&parameter=%s&split=%d&output=json&sub=%d&qid=%s"
        val DZH_ZIJIN = "/quote/l2stat?field=ShiJian,DaDanLiuRuJinE,DaDanLiuChuJinE&obj=%s&start=-1&count=1&sub=1&mode=2&qid=%s"
        //资金流向
        val DZH_L2STAT = "/quote/l2stat?obj=%s&start=-161&count=161&field=ShiJian,DaDanLiuRuJinE,DaDanLiuChuJinE&sub=%d&qid=%s"
        //资金流向
        val DZH_L2STAT2 = "/quote/l2stat?obj=%s&start=%d&count=%d&field=ShiJian,DaDanLiuRuJinE,DaDanLiuChuJinE&sub=%d&qid=%s"
        //日内资金流向
        val DZH_L2STAT_DAY = "/quote/l2stat?obj=%s&start=%d&count=%d&field=ShiJian,WeiTuoMaiRu,WeiTuoMaiChu,MaiRuZhongDanBiLi,MaiRuDaDanBiLi,MaiRuTeDaDanBiLi,MaiChuZhongDanBiLi,MaiChuDaDanBiLi,MaiChuTeDaDanBiLi,DuanXianMaiRu,DuanXianMaiChu,DuanXianChiHuo,DuanXianTuHuo,DaDanLiuRuJinE,DaDanLiuChuJinE&sub=%d&qid=%s"
        //F10-公司简介
        val DZH_F10_BRIEF = "/f10/gsgk?obj=%s&field=Gsmc,Dsz,Dm,Dmdzyx,Zcdz,Ssrq,Sshy,Zyfw,Dh,Gswz&qid=%s"
        //F10-主营构成
        val DZH_F10_FORM = "/f10/zygc?obj=%s&field=Hy,Zysr,Zysrzb&qid=%s"
        //F10-分红扩股
        val DZH_F10_DIVIDEND = "/f10/gbfh/fhkg?obj=%s&field=Date,Mgsg,Mgzz,Mgfh,Mgp,Gqdjr,Cqcxr&start=-4&qid=%s"
        //F10-公司高管
        val DZH_F10_MANAGER = "/f10/glc?obj=%s&qid=%s"
        //F10-股本
        val DZH_F10_EQUITY = "/f10/gbfh/gbjg?obj=%s&field=zgb,ltgf&start=-1&qid=%s"
        val DZH_F10_CONTROLLER = "/f10/gdjc/sjkzr?obj=%s&qid=%s"
        val DZH_F10_MAJORHOLDER = "/f10/gdjc/kggd?obj=%s&qid=%s"
        val DZH_F10_TOPTEN = "/f10/gdjc/sdgd?obj=%s&field=Gdrs,Xh,Gdmc,Cgs,Zzgs,Zjqk,Gbxz,Gsdm&start=-1&qid=%s"
        //F10-股东户数
        val DZH_F10_HOLDERNUM = "/f10/gdjc/gdhs?obj=%s&start=-4&qid=%s"
        //F10-主要指标
        val DZH_F10_TARGET = "/f10/cwts/zycwzb?obj=%s&field=date,jbmgsy,mgjzc,mgwfplr,mggjj,mgjyxjll,jqjzcsyl&start=-4&qid=%s"
        //F10-利润表
        val DZH_F10_PROFIT = "/f10/cwts/lrfpbzy?obj=%s&start=-4&qid=%s"
        //F10-资产负债表
        val DZH_F10_DEBT = "/f10/cwts/zcfzbzy?obj=%s&field=date,zzc,zfz,gdqy,zbgjj&start=-4&qid=%s"
        //F10-现金流量表
        val DZH_F10_CASH = "/f10/cwts/xjllbzy?obj=%s&field=date,jyxjje,tzxjje,czxjje,xjjzje&start=-4&qid=%s"
        //题材资金流向
        val DZH_BLOCKSTAT = "/blockstat?gql=block=股票\\\\大智慧自定义\\\\指数板块\\\\%s&cfdays=%d&field=ZiJinLiuXiang&sub=%d&qid=%s"
        val DZH_ZHIBIAO = "/indicator/calc?obj=%s&type=ind&name=%s&start=-161&count=161&period=%s&split=%d&sub=%d&qid=%s"

        //macd
        val DZH_MACD = "/indicator/calc?obj=%s&start=%d&count=%d&type=ind&name=MACD&period=%s&split=%d&sub=%d&qid=%s"
        //kdj
        val DZH_KDJ = "/indicator/calc?obj=%s&start=%d&count=%d&type=ind&period=%s&name=KDJ&split=%d&output=json&sub=%d&qid=%s"
        //rsi
        val DZH_RSI = "/indicator/calc?obj=%s&start=%d&count=%d&type=ind&period=%s&name=RSI&split=%d&output=json&sub=%d&qid=%s"
        //boll
        val DZH_BOLL = "/indicator/calc?obj=%s&start=%d&count=%d&type=ind&period=%s&name=BOLL&split=%d&output=json&sub=%d&qid=%s"

        open fun dzh_cancel(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_CANCEL, *args)))
        }

        fun dzh_stkdata(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_STKDATA, *args)))
        }

        fun dzh_stkdata_detail(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_STKDATA_DETAIL, *args)))
        }

        fun dzh_stkdata_orderby(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_STKDATA_ORDERBY, *args)))
        }

        fun dzh_stkdata_theme_orderby(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_STKDATA_THEME_ORDERBY, *args)))
        }

        fun dzh_stkdata_gql(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_STKDATA_GQL, *args)))
        }

        fun dzh_sort(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_SORT, *args)))
        }

        fun dzh_min(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_MIN, *args)))
        }

        fun dzh_day(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_DAY, *args)))
        }

        fun dzh_stkdata_sell(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_STKDATA_SELL, *args)))
        }

        fun dzh_quote_tick(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_TICK, *args)))
        }

        fun dzh_kline(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_KLINE, *args)))
        }

        fun dzh_ma(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_MA, *args)))
        }

        fun dzh_ma2(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_MA2, *args)))
        }

        fun dzh_l2stat(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_L2STAT, *args)))
        }

        fun dzh_l2stat_day2(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_L2STAT2, *args)))
        }

        fun dzh_l2stat_day(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_L2STAT_DAY, *args)))
        }

        fun dzh_f10_brief(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_BRIEF, *args)))
        }

        fun dzh_f10_form(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_FORM, *args)))
        }

        fun dzh_f10_dividend(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_DIVIDEND, *args)))
        }

        fun dzh_f10_manager(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_MANAGER, *args)))
        }

        fun dzh_f10_target(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_TARGET, *args)))
        }

        fun dzh_f10_profit(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_PROFIT, *args)))
        }

        fun dzh_f10_debt(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_DEBT, *args)))
        }

        fun dzh_f10_cash(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_CASH, *args)))
        }

        fun dzh_f10_equity(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_EQUITY, *args)))
        }

        fun dzh_f10_controller(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_CONTROLLER, *args)))
        }

        fun dzh_f10_topten(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_TOPTEN, *args)))
        }

        fun dzh_f10_holdernum(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_F10_HOLDERNUM, *args)))
        }

        fun dzh_blockstate(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_BLOCKSTAT, *args)))
        }

        fun dzh_zhibiao(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_ZHIBIAO, *args)))
        }

        fun dzh_macd(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_MACD, *args)))
        }

        fun dzh_kdj(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_KDJ, *args)))
        }

        fun dzh_rsi(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_RSI, *args)))
        }

        fun dzh_boll(vararg args: Any) {
            BusProvider.getInstance().post(DzhEvent(String.format(DZH_BOLL, *args)))
        }
    }
}