package com.sogukj.pe.bean;

import com.sogukj.pe.widgets.indexbar.BaseIndexPinyinBean;

/**
 * Created by CH-ZH on 2018/8/30.
 */
public class CityBean extends BaseIndexPinyinBean {

    private String city;//城市名字
    private boolean isTop;//是否是最上面的 不需要被转化成拼音的
    private boolean seclected;
    private int id;
    public CityBean() {

    }

    public CityBean(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public CityBean setCity(String city) {
        this.city = city;
        return this;
    }

    public boolean isTop() {
        return isTop;
    }

    public CityBean setTop(boolean top) {
        isTop = top;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSeclected() {
        return seclected;
    }

    public void setSeclected(boolean seclected) {
        this.seclected = seclected;
    }

    @Override
    public String getTarget() {
        return city;
    }

    @Override
    public boolean isNeedToPinyin() {
        return !isTop;
    }


    @Override
    public boolean isShowSuspension() {
        return !isTop;
    }
}

