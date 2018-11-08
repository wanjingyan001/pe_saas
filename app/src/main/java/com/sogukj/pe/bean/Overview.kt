package com.sogukj.pe.bean

/**
 * Created by admin on 2018/11/1.
 */

data class UserProjectInfo(
        var user_id: Int,// 40
        var name: String,// 刘晓杰
        var count: Int,// 共复制项目数
        var company: List<Company>
)

data class Company(
        var name: String,//(调研,储备,立项,已投,退出等tag)
        var type: Int,// 6
        var count: Int// 数量
)

data class ProjectAdd(
        var list: List<NewPro>,
        var count: Int
)

data class NewPro(
        var id: Int,// 662
        var cname: String,// 1236
        var name: String?,// 尹加久
        var type: Int,// 9
        var url: String,//头像
        var status: String,// 退出管理
        var add_time: String,// 前天 10:38
        var update_time: String// 10-20 18:23
)


data class Opinion(
        var company_id: Int,// 361
        var name: String,// 苏州朗动网络科技有限公司
        var total: Int// 500
)