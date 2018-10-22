package com.sogukj.pe.bean

import java.io.File
import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectFileInfo : Serializable {
    var name  = ""
    var file : File? = null
    var url = ""
}