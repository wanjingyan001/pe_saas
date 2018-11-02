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
    var preview_url = "http://kuaizhangkuaixunyuyin.oss-cn-hangzhou.aliyuncs.com/photo/Android/6499ba1b-3f5e-4903-a4a5-5a17bf4d2aea.jpg"
}