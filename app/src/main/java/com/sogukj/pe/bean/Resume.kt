package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/2.
 */

class Resume :Serializable {
    var baseInfo: BaseInfoBean? = null//基础信息
    var eduction: List<EductionBean>? = null//教育经历
    var work: List<WorkBean>? = null//工作经历

   inner class BaseInfoBean :Serializable{
        /**
         * position : 风控副总裁
         * sex : 3
         * language :
         * workYear :
         * city : 2
         * pid : 0
         * cityName : 北京
         * educationLevel :
         * email : 1401128990@qq.com
         * phone : 18895625589
         */
        var name:String? = null//姓名
        var position: String? = null//职位
        var sex: Int = 0//性别(1=>男,2=>女,3=>未知)
        var language: String? = null//语言
        var workYear: String? = null//工作年限
        var city: Int = 0//所在城市ID
        var pid: Int = 0//所在城市父ID
        var cityName: String? = null//所在城市中文名
        var educationLevel: String? = null//最高学历
        var email: String? = null//联系邮箱
        var phone: String? = null//联系电话
    }

   inner class EductionBean :Serializable{
        /**
         * id : 1
         * toSchoolDate : 2011/06
         * graduationDate : 2015/09
         * school : 中国科技大
         * education : 本科
         * major : 网络工程
         * majorInfo :
         */

        var id: Int = 0//
        var toSchoolDate: String? = null//入学时间
        var graduationDate: String? = null//毕业时间
        var school: String? = null//学校
        var education: String? = null//学历
        var major: String? = null//专业
        var majorInfo: String? = null//专业描述
    }

   inner class WorkBean :Serializable{
        /**
         * id : 1
         * employDate : 2017/01
         * leaveDate : 至今
         * company : 424
         * responsibility : 244444
         * jobInfo :
         * department :
         * companyScale :
         * companyProperty :
         * trade : 2
         * trade_name : 会计/金融/银行/保险
         * pid : 0
         */

        var id: Int = 0//
        var employDate: String? = null//入职时间
        var leaveDate: String? = null//离职时间
        var company: String? = null//公司
        var responsibility: String? = null//职能
        var jobInfo: String? = null//
        var department: String? = null//部门
        var companyScale: String? = null//公司规模
        var companyProperty: String? = null//公司性质
        var trade: Int = 0//行业id
        var trade_name: String? = null//行业名
        var pid: Int = 0//行业父id
    }
}
