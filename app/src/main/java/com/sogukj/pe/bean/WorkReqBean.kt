package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/4.
 */
class WorkReqBean :Serializable {
    var ae = WorkEducationBean()
    var type = 0//   type =1     教育      type =2    工作
}