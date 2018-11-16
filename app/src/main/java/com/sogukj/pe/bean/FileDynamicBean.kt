package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/26.
 */
class FileDynamicBean : Serializable {
    /**
     * "id": 1,
    "uid": "18368493790",// 操作人手机号
    "display_name":"XXX",// 操作人姓名，头像则是操作人名字的第一个字，配合上背景色
    "fileid": 1020,
    "file_type": 7,
    "parent_type": 6,
    "show": "11111在'共享文件夹'中上传了1个文件",// 提示文字
    "filename": "timg.jpeg",// 文件名
    "size": "76867",// 文件大小
    "storage": 74,
    "add_time": "2018-10-24 18:14:38",// 操作日期
    "is_delete": 1,// 是否是删除操作，如果是删除操作，则不需要显示文件类型，只需要显示一条提示信息
    "fullpath": "files/共享文件夹/timg.jpeg"
     */
    var id = ""
    var uid = ""
    var display_name = ""
    var fileid = ""
    var file_type = ""
    var parent_type = ""
    var show = ""
    var filename = ""
    var size = ""
    var storage = ""
    var add_time = ""
    var is_delete = 1 // 2:删除
    var had_deleted = 0 // 0:文件未删除 1:文件已删除
    var fullpath = ""
    var preview_url = ""
    var user_head = ""
}