package com.sogukj.pe.bean

import com.google.gson.Gson
import java.io.Serializable

/**
 * Created by admin on 2018/1/25.
 */
class TeamBean : Serializable {
    var project_id: String? = null
    var project_name: String? = null


    override fun toString(): String = Gson().toJson(this)
}