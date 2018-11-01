package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/11/1.
 */
class FileSearchInfo : Serializable {
    var file_type = "" // "Folder" 表示目录
    var file_name = ""
    var create_time = ""
    var url = ""
}