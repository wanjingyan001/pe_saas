package com.sogukj.pe.bean

/**
 * Created by qinfei on 17/10/26.
 */
class ApproveFilterBean {
    var leave: ItemBean? = null
    var approve: ItemBean? = null
    var sign: ItemBean? = null


    class ItemBean {
        var kind: Map<String, String>? = null
        var status: Map<String, String>? = null
    }
}