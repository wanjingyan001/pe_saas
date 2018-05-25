package com.sogukj.pe.peUtils;

import android.util.Log;

import com.sogukj.pe.baselibrary.utils.CharacterParser;
import com.sogukj.pe.bean.FinanceListBean;
import com.sogukj.pe.bean.FundSmallBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class SortUtil {

    public static void sort(ArrayList<FinanceListBean> list) {
        Collections.sort(list, new Comparator<FinanceListBean>() {
            @Override
            public int compare(FinanceListBean o1, FinanceListBean o2) {
                // 返回值为int类型，大于0表示正序，小于0表示逆序
                //2018年04月26日 19:54
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                try {
                    Date date1 = format.parse(o1.getIssueTime());
                    Date date2 = format.parse(o2.getIssueTime());
                    long d1 = date1.getTime();
                    long d2 = date2.getTime();
                    if (d1 > d2) {
                        return -1;
                    } else {
                        return 1;
                    }
                } catch (ParseException e) {
                    return 1;
                }
            }
        });
    }


    public static void sortByName(ArrayList<FundSmallBean> list) {
        Collections.sort(list, new Comparator<FundSmallBean>() {
            @Override
            public int compare(FundSmallBean o1, FundSmallBean o2) {
                //中文的括号
                String bean1Name = CharacterParser.getInstance().getAlpha(o1.getFundName().replaceAll("（", "").replaceAll("）", "")).toUpperCase();
                String bean2Name = CharacterParser.getInstance().getAlpha(o2.getFundName().replaceAll("（", "").replaceAll("）", "")).toUpperCase();
                int cmpLen = Math.min(bean1Name.length(), bean2Name.length());
                Log.e("str", o1.getFundName() + bean1Name + "      " + o2.getFundName() + bean2Name);
                for (int i = 0; i < cmpLen; i++) {
                    char ch1 = bean1Name.charAt(i);
                    char ch2 = bean2Name.charAt(i);
                    if (ch1 == ch2) {
                        continue;
                    } else if (ch1 > ch2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                if (bean1Name.length() > cmpLen) {
                    return 1;
                }
                if (bean2Name.length() > cmpLen) {
                    return -1;
                }
                return 1;
            }
        });
    }

}
