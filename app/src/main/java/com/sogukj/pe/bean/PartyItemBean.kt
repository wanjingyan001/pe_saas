package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2018/3/21.
 */
class PartyItemBean : Serializable {
    var articles: List<ArticleBean>? = null
    var files: List<FileBean>? = null
}

class ArticleBean : Serializable {
    var id: Int? = null
    var title: String? = null
    var time: String? = null
}

class FileBean : Serializable {
    var id: Int? = null
    var file_name: String? = null
    var url: String? = null
    var time: String? = null
}