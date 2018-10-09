package com.sogukj.pe.bean

import java.io.File
import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/27.
 */
class ProjectApproveInfo : Serializable {
     var name = ""
     var class_id: Int? = null
     var pid: Int? = null
     var must: Int? = null
     var fields = ""
     var control: Int? = null
     var floor: Int? = null

     var frame: List<ApproveInfo>? = null
     var files: List<ApproveFile>? = null
     var son : List<ApproveSon>? = null
    class ApproveFile : Serializable {
         var file_id: Int? = null
         var originUrl: String = ""
         var url = ""
         var preview = ""
         var size = ""
         var file_name = ""
         var file : File? = null
         var date  = ""
         var filePath = ""
    }

     class ApproveInfo : Serializable {
         var year = ""
         var asset = ""
         var income = ""
         var profit = ""
     }

    class ApproveSon : Serializable{
        var name = ""
        var class_id: Int? = null
        var pid: Int? = null
        var must: Int? = null
        var fields = ""
        var control: Int? = null
        var floor: Int? = null
        var files: List<ApproveFile>? = null
    }
}