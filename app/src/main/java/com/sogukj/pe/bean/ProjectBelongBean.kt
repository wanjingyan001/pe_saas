package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/7/13.
 */
class ProjectBelongBean {
    var xm: ArrayList<Cell1>? = null
    var gz: Cell1? = null

    class Cell1 {
        var name: String? = null
        var type: Int? = null//
        var count: Int? = null
        var red: Int? = null
    }

//    "xm": [
//    {
//        "name": "调研",  // string
//        "type": 6,      // int
//        "count": 1,     // int
//        "red": 0        // int
//    },
//    ......
//    ],
//    "gz": {
//        "count": 3,
//        "name": "关注",
//        "red": 0
//    }
}