package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/1/23.
 */
class QuitInfoBean {
    var type: Int? = null
    var invest: String? = null
    var cost: String? = null
    var income: String? = null
    var compensation: String? = null
    var profit: String? = null
    var outIncome: String? = null
    var investRate: String? = null
    var investTime: String? = null
    var outTime: String? = null
    var days: Int? = null
    var annualRate: String? = null
    var investHour: String? = null
    var IRR: String? = null
    var supply: String? = null
    var surplus: String? = null
    var summary: String? = null
    //1	type	number	类型	1=>部分退出，2=>全部退出
    //2	invest	string	投资主体	type=2时返回，type=1时隐藏此字段
    //3	cost	string	成本
    //4	income	string	退出收入
    //5	compensation	string	补偿款	type=2时返回，type=1时隐藏此字段
    //6	profit	string	分红
    //7	outIncome	string	退出收益
    //8	investRate	string	投资收益率
    //9	investTime	string	投资时间
    //10	outTime	string	退出时间
    //11	days	number	投资天数
    //12	annualRate	string	年化收益率
    //13	investHour	string	投资时长
    //14	IRR	string	IRR
    //15	supply	string	补充	type=1时返回，type=2隐藏此字段
    //16	surplus	string	未退出成本	type=1时返回，type=2隐藏此字段
    //17	summary	string	退出总结


    var number: String? = null
//    var title: String? = null
//    var id: Int? = null
//    var name: String? = null
//    var outTime: String? = null
}