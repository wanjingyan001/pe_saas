package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/26.
 */
class FileDynamicBean : Serializable {
    var img = ""
    var title = ""
    var user = ""
    var time = ""
    var fileImage : List<FileImage> ? = null
    var file : List<FileOther> ? = null
    class FileImage : Serializable{
        var image = ""
    }

    class FileOther : Serializable{
        var fileName = ""
        var fileSize = ""
    }
}