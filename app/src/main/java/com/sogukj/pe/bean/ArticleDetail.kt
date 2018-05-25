package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2018/3/22.
 */
class ArticleDetail : Serializable {
    var id: Int? = null
    var title: String? = null//文章标题
    var author: String? = null//文章作者
    var source: String? = null//文章来源
    var contents: String? = null//文章内容
    var time: String? = null//文章发布时间
}