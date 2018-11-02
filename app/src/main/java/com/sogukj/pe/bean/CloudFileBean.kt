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
    var used_bytes = "" //字节
    var file_path = ""
    var url = "https://18121442041:admin@ownclouds.pewinner.com/remote.php/webdav/我的文件/我的文件1.png?downloadStartSecret=eaIxJKYzQnh"
}