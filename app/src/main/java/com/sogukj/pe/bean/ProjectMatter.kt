package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/7.
 */
class ProjectMatterMD(var MDTime: String) : Serializable

class ProjectMatterInfo(var time: String, var content: String) : Serializable

class ProjectMatterCompany(var companyName: String,
                           var companyId: String) : Serializable

