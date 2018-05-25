package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/16.
 */
class TeamMemberBean : Serializable {
    var name: String? = null//Varchar	姓名
    var title: String? = null//	Varchar	人物标签
    var desc: String? = null//	Text	人物介绍
    var icon: String? = null//	Text	logo来源
}