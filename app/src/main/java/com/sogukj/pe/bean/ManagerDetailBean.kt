package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/5/24.
 */
class ManagerDetailBean {
    var id: Int? = null
    var zhName: String? = null//"发展阶段",  //中文名
    var control: Int? = null//控件类型:0=无控件,1=中型输入框,2=大型输入框,4=单选框,10=文件输入框
    var grade: Int? = null//是否需要打星,1=需要,0=不需要
    var mid: Int? = null
    var fmid : Int = 0
    var sid : Int = 0
    var pid : Int = 0
    var elsed: String? = null//当control=4时,代表的选项
    var ppid: Int? = null
    var must: Int? = null//是否必填,1=必填,0=非必填
    var contents: String? = null//当control不为0时,代表控件中的内容
    var files: HashMap<String, String>? = null//当control=10
    var star: String? = null//sring类型,0.5代表半颗星
    var child: ArrayList<ManagerDetailBean>? = null
//    "id": 1,            //请无视
//    "zhName": "发展阶段",  //中文名
//    "control": 1,//控件类型:0=无控件,1=中型输入框,2=大型输入框,4=单选框,10=文件输入框
//    "grade": 1, //是否需要打星,1=需要,0=不需要
//    "mid": 2,   //请无视
//    "elsed": "",    //当control=4时,代表的选项
//    "ppid": 0,  //请无视
//    "must": 0,  //是否必填,1=必填,0=非必填
//    "contents": "qwer", //当control不为0时,代表控件中的内容
//    "star": "3.5"   //sring类型,0.5代表半颗星
//    "child": [  //时有时无,child里面的内容结构同外层的一样
//    {
//        "id": 2,
//        "zhName": "发展阶段",
//        "control": 4,
//        "grade": 0,
//        "mid": 2,
//        "elsed": "初创期,成长期,稳定期,衰退期",
//        "ppid": 1,
//        "must": 0,
//        "contents": "稳定期",
//        "star": "0"
//    },
//    ],

}