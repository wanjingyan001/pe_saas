package com.sogukj.pe.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by CH-ZH on 2018/8/30.
 */
public class StkDataDetail extends DzhResp implements Serializable {

    Data Data;

    public StkDataDetail.Data getData() {
        return Data;
    }

    public void setData(StkDataDetail.Data data) {
        Data = data;
    }

    public static final class Data implements Serializable{
        int Id;
        List<RepDataStkData> RepDataStkData;

        public int getId() {
            return Id;
        }

        public List<RepDataStkData> getRepDataStkData() {
            return RepDataStkData;
        }

        public static final class RepDataStkData implements Serializable{
            String Obj;
            String ZhongWenJianCheng;
            float ZuiXinJia;
            float ZhangFu;
            float ZhangDie;
            float FenZhongZhangFu5;
            int ShiFouTingPai;
            float ZuiGaoJia;
            float ZuiDiJia;
            long ChengJiaoLiang;
            long ChengJiaoE;
            float KaiPanJia;
            float ZuoShou;
            float ZhangTing;
            float DieTing;
            float LiangBi;
            float JunJia;
            String HuanShou;
            float ZhenFu;
            String WeiBi;
            long WaiPan;
            long NeiPan;
            float LiuTongAGu;
            int LiuTongShiZhi;
            float ZongGuBen;
            int ZongShiZhi;
            float ShiYingLv;
            float ShiJingLv;

            public float getZhangTing() {
                return ZhangTing;
            }

            public void setZhangTing(float zhangTing) {
                ZhangTing = zhangTing;
            }

            public float getDieTing() {
                return DieTing;
            }

            public void setDieTing(float dieTing) {
                DieTing = dieTing;
            }

            public float getLiangBi() {
                return LiangBi;
            }

            public void setLiangBi(float liangBi) {
                LiangBi = liangBi;
            }

            public float getJunJia() {
                return JunJia;
            }

            public void setJunJia(float junJia) {
                JunJia = junJia;
            }

            public float getZhenFu() {
                return ZhenFu;
            }

            public void setZhenFu(float zhenFu) {
                ZhenFu = zhenFu;
            }

            public String getWeiBi() {
                return WeiBi;
            }

            public void setWeiBi(String weiBi) {
                WeiBi = weiBi;
            }

            public long getWaiPan() {
                return WaiPan;
            }

            public void setWaiPan(long waiPan) {
                WaiPan = waiPan;
            }

            public long getNeiPan() {
                return NeiPan;
            }

            public void setNeiPan(long neiPan) {
                NeiPan = neiPan;
            }

            public float getLiuTongAGu() {
                return LiuTongAGu;
            }

            public void setLiuTongAGu(float liuTongAGu) {
                LiuTongAGu = liuTongAGu;
            }

            public int getLiuTongShiZhi() {
                return LiuTongShiZhi;
            }

            public void setLiuTongShiZhi(int liuTongShiZhi) {
                LiuTongShiZhi = liuTongShiZhi;
            }

            public float getZongGuBen() {
                return ZongGuBen;
            }

            public void setZongGuBen(float zongGuBen) {
                ZongGuBen = zongGuBen;
            }

            public int getZongShiZhi() {
                return ZongShiZhi;
            }

            public void setZongShiZhi(int zongShiZhi) {
                ZongShiZhi = zongShiZhi;
            }

            public float getShiYingLv() {
                return ShiYingLv;
            }

            public void setShiYingLv(float shiYingLv) {
                ShiYingLv = shiYingLv;
            }

            public float getShiJingLv() {
                return ShiJingLv;
            }

            public void setShiJingLv(float shiJingLv) {
                ShiJingLv = shiJingLv;
            }

            public RepDataStkData(String obj) {
                this.Obj = obj;
            }

            public String getObj() {
                return Obj;
            }

            public void setObj(String obj) {
                Obj = obj;
            }

            public float getZuiXinJia() {
                return ZuiXinJia;
            }

            public void setZuiXinJia(float zuiXinJia) {
                ZuiXinJia = zuiXinJia;
            }

            public float getZhangFu() {
                return ZhangFu;
            }

            public void setZhangFu(float zhangFu) {
                ZhangFu = zhangFu;
            }

            public int getShiFouTingPai() {
                return ShiFouTingPai;
            }

            public void setShiFouTingPai(int shiFouTingPai) {
                ShiFouTingPai = shiFouTingPai;
            }

            public float getZhangDie() {
                return ZhangDie;
            }

            public void setZhangDie(float zhangDie) {
                ZhangDie = zhangDie;
            }

            public String getZhongWenJianCheng() {
                return ZhongWenJianCheng;
            }

            public void setZhongWenJianCheng(String zhongWenJianCheng) {
                ZhongWenJianCheng = zhongWenJianCheng;
            }

            public float getFenZhongZhangFu5() {
                return FenZhongZhangFu5;
            }

            public void setFenZhongZhangFu5(float fenZhongZhangFu5) {
                FenZhongZhangFu5 = fenZhongZhangFu5;
            }

            public float getZuiGaoJia() {
                return ZuiGaoJia;
            }

            public void setZuiGaoJia(float zuiGaoJia) {
                ZuiGaoJia = zuiGaoJia;
            }

            public float getZuiDiJia() {
                return ZuiDiJia;
            }

            public void setZuiDiJia(float zuiDiJia) {
                ZuiDiJia = zuiDiJia;
            }

            public long getChengJiaoLiang() {
                return ChengJiaoLiang;
            }

            public void setChengJiaoLiang(long chengJiaoLiang) {
                ChengJiaoLiang = chengJiaoLiang;
            }

            public long getChengJiaoE() {
                return ChengJiaoE;
            }

            public void setChengJiaoE(long chengJiaoE) {
                ChengJiaoE = chengJiaoE;
            }

            public float getKaiPanJia() {
                return KaiPanJia;
            }

            public void setKaiPanJia(float kaiPanJia) {
                KaiPanJia = kaiPanJia;
            }

            public String getHuanShou() {
                return HuanShou;
            }

            public void setHuanShou(String huanShou) {
                HuanShou = huanShou;
            }

            public float getZuoShou() {
                return ZuoShou;
            }

            public void setZuoShou(float zuoShou) {
                ZuoShou = zuoShou;
            }
        }
    }
}