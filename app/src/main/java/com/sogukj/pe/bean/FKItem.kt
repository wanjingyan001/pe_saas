package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/22.
 */
class FKItem {
    var jdxm: JDXM? = null
    var thgl: THGL? = null
    var pfbz: ArrayList<PFBZ>? = null

    class JDXM {
        var pName: String? = null
        var standard: String? = null
        var selfScore: Int? = null
        var data: ArrayList<InnerData>? = null

        class InnerData {
            var target: String? = null
            var info: String? = null
        }
    }

    class THGL {
        var pName: String? = null
        var data: ArrayList<ItemData>? = null

        class ItemData {
            var id: Int? = null
            var name: String? = null
            var weight: String? = null
            var total_score: Int? = null
            var target: String? = null
            var type: Int? = null
            var info: String? = null
            var offset: Int? = null
            var desc: String? = null
            var score: String? = null
        }
    }
}

//"jdxm": {
//    "pName": "尽调项目数量（30%）",
//    "standard": "每个参与尽调项目得20分；最高分数：120分",
//    "selfScore": 36,
//    "data": []
//          "target": "外网",
//          "info": "哈哈不不不"
//},

//"thgl": [
//{
//    "pName": "投后管理执行情况",
//    "data": [
//    {
//        "id": 76,
//        "name": "每季度财务报告",
//        "weight": "10",
//        "total_score": 120,
//        "target": "每季度财务报告(10%)",
//        "type": 1,
//        "info": null,
//        "offset": 1
//    },
//    {
//        "id": 77,
//        "name": "重大事项揭示",
//        "weight": "10",
//        "total_score": 120,
//        "target": "重大事项揭示(10%)",
//        "type": 1,
//        "info": null,
//        "offset": 1
//    },
//    {
//        "id": 78,
//        "name": "增值服务",
//        "weight": "10",
//        "total_score": 120,
//        "target": "增值服务(10%)",
//        "type": 1,
//        "info": null,
//        "offset": 1
//    },
//    {
//        "id": 79,
//        "name": "投资协议执行情况",
//        "weight": "10",
//        "total_score": 120,
//        "target": "投资协议执行情况(10%)",
//        "type": 1,
//        "info": null,
//        "offset": 1
//    }
//    ]
//}
//],
//"pfbz": [
//{
//    "level": "优秀",
//    "ss": 101,
//    "es": 120
//},
//{
//    "level": "良好",
//    "ss": 81,
//    "es": 100
//},
//{
//    "level": "合格",
//    "ss": 61,
//    "es": 80
//},
//{
//    "level": "不称职",
//    "ss": 0,
//    "es": 60
//}
//]