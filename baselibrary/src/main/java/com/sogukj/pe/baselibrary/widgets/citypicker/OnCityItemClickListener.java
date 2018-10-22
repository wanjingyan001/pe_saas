package com.sogukj.pe.baselibrary.widgets.citypicker;

import com.sogukj.pe.baselibrary.widgets.citypicker.bean.CityBean;
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.DistrictBean;
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.ProvinceBean;

/**
 * Created by CH-ZH on 2018/10/19.
 */
public abstract class OnCityItemClickListener {
    /**
     * 当选择省市区三级选择器时，需要覆盖此方法
     * @param province
     * @param city
     * @param district
     */
    public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {

    }

    /**
     * 取消
     */
    public void onCancel() {

    }
}
