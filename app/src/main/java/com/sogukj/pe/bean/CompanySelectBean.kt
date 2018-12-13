package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/5/31.
 */
data class CompanySelectBean(
        val base: String? = null,
        val companyType: Int? = null,
        val estiblishTime: String? = null,
        val id: Long = 0L,
        val legalPersonName: String? = null,
        val matchType: Any? = null,
        val name: String,
        val regCapital: String? = null,
        val type: Int? = null
)