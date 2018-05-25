package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2018/2/5.
 */
class ChatFileBean : Serializable {
    var size: String? = null//文件大小
    var url: String? = null//文件路径
    var file_name: String? = null//名字
    var user_name: String? = null//文件上传者
    var time: String? = null//文件上传时间
}