package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/23.
 */
class SentimentInfoBean : Serializable {
    var remainder = 0
    var is_open = 0 // 1 : 开启 0 : 关闭
    var expire = "" //过期时间
}