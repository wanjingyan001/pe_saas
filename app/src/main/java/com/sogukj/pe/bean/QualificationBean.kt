package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/22.
 */
class QualificationBean : Serializable {
    var deviceName: String = ""//	Varchar	设备名称
    var licenceNum: String = ""//	Varchar	许可证编号
    var licenceType: String = ""//	Varchar	证书种类
    var issueDate: String = ""//	Date	发证日期
    var toDate: String = ""//	Date	有效期至
    var deviceType: String = ""//Varchar	设备型号
    var applyCompany: String = ""//Varchar	申请单位
    var productCompany: String = ""//	Varchar	生产企业
}