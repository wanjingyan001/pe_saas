package com.sogukj.pe.bean

/**
 * Created by admin on 2018/5/8.
 */
class TaskDraft {
    var taskReqBean: TaskModifyBean? = null
    var company:  CustomSealBean.ValueBean? = null
    var executorList: ArrayList<UserBean>? = null//执行人
    var watcherList: ArrayList<UserBean>? = null//抄送人
}