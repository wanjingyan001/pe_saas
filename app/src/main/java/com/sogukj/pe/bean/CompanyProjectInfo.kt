package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/9.
 */
class KeyNode : Serializable {
    var end_time: String? = null//结束时间
    var title: String? = null//标题
    var type: Int? = 0//类别
    var data_id: Int? = 0//
    var name: String? = null//姓名 当project_type=3时，多人名以逗号隔开
    var finish_time: String? = null//完成时间  仅当project_type=2返回
}


class MatterDetails : Serializable {
    var year: String = ""
    var data = ArrayList<KeyNode>()
}

