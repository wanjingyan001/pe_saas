package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/30.
 */
class ApproveRecordInfo : Serializable {
    var flow : List<ApproveFlow> ? = null
    var click : Int ? = null //0=>无操作，1=>审批人，2=>发起人
    var button : List<ApproveButton> ? = null
    var repeat : Int ? = null // 1:重复编辑
    class ApproveFlow : Serializable{
        var approval_id : Int ? = null
        var content = ""
        var approve_time = ""
        var uid : Int ? = null
        var status : Int ? = null // -2=>失效审批，-1=否决，0=待审批，1=同意通过，2=同意上立项会 ,3=>重新发起
        var position = ""
        var url = ""
        var name = ""
        var file : List<RecordFile> ? = null
        var meet : ApproveMeet ? = null
        var fenpei = ""
    }

    class RecordFile : Serializable{
        var id : Int ? = null
        var file_name = ""
        var url = ""
        var size = ""
    }

    class ApproveMeet : Serializable{
        var meeting_time = ""
        var meeter : List<Meeter> ? = null
    }

    class Meeter : Serializable{
        var url = ""
        var name = ""
    }

    class ApproveButton : Serializable{
        var type = 0  // -1:退回修改 1:同意通过 2:同意上立项会 3:退出 4:分配
        var name = ""
    }
}