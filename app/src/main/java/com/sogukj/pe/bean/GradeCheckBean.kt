package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2017/12/20.
 */
class GradeCheckBean : Serializable { //1=>进入绩效考核列表页面，2=>进入岗位胜任力列表 3=>进入风控部填写页，4=>进入投资部填写页
    var finish_grade: ArrayList<ScoreItem>? = null//打分界面  type为1或为2返回
    var ready_grade: ArrayList<ScoreItem>? = null//打分界面  type为1或为2返回
    var fengkong: ArrayList<FengKongItem>? = null//风控填写页面   type为3返回：//废弃
    var touzhi: ArrayList<TouZiItem>? = null//投资部填写页 type为4返回：//废弃

    class ScoreItem : Serializable {
        var user_id: Int? = null
        var name: String? = null
        var department: String? = null
        var position: String? = null
        var url: String? = null
        var grade_date: String? = null
        var type: Int? = null //1=>其他模版 2=>风控部模版 3=>投资部模版
    }

    class FengKongItem : Serializable {
        var title: String? = null
        var is_btn: Int? = null
        var biaozhun: String? = null
        var data: ArrayList<FengKongInnerItem>? = null

        class FengKongInnerItem : Serializable {
            var performance_id: Int? = null
            var zhibiao: String? = null
        }
    }

    class TouZiItem : Serializable {
        var title: String? = null
        var data: ArrayList<TouZiInnerItem>? = null

        class TouZiInnerItem : Serializable {
            var performance_id: Int? = null
            var zhibiao: String? = null
        }
    }
}