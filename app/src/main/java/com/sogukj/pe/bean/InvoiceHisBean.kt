package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/18.
 */
class InvoiceHisBean : Serializable {
    var add_time = ""
    var status = 1
    var type = ""
    var title = ""
    var amount = ""
    var id : Int ? = null
    var tax_no = ""
}