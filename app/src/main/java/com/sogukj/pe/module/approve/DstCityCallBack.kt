package com.sogukj.pe.module.approve

import com.sogukj.pe.bean.CityArea
import java.util.*

/**
 * Created by CH-ZH on 2018/8/30.
 */
interface DstCityCallBack {
    fun setHisCityData(city: ArrayList<CityArea.City>)
}