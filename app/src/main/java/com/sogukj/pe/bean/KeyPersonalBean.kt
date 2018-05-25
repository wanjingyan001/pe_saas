package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/15.
 */
class KeyPersonalBean : Serializable {
    var id: Int? = null//	人物id
    var logo: String? = null//	Varchar	人物logo
    var name: String? = null//   Varchar    人或公司名
    var typeJoin: List<String?>? = null//  Varchar    职位
    var toco: Int? = null//  Int    有几家公司
    var type: Int? = null//   Tinyint    类型    1 公司 2 人
}