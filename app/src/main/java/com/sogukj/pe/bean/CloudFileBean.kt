package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudFileBean : Serializable{
    var file_name = ""
    var create_time = ""
    var isSelect = false
    var file_type = "" // "Folder" 表示目录
    var catalogue = "" //目录
    var canSelect = false
}