package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/18.
 */
class RecruitBean : Serializable {
    var title: String = ""//	Varchar	招聘职称,题目
    var createTime: String = ""//	Date	创建日期
    var city: String = ""//	Varchar	所在城市
    var district: String = ""//	Varchar	所在区
    var experience: String = ""//	Varchar	经验
    var oriSalary: String = ""//	Varchar	薪资
}