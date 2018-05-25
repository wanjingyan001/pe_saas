package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/22.
 */
class CheckBean : Serializable {
    var checkOrg: String? = ""//	Varchar	检查实施机关
    var checkDate: String? = ""//	Date	日期
    var checkResult: String? = ""//Varchar	结果
    var checkType: String? = ""//	Varchar	类型
}