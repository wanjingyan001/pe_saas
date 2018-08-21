package com.sogukj.pe.service

import com.sogukj.pe.bean.ChatFileBean
import com.sogukj.pe.bean.UserBean
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/22.
 */
interface ImService {
    //查询群组文件
    @FormUrlEncoded
    @POST("/api/Message/chatFile")
    fun chatFile(@Field("type") type: Int, @Field("tid") tid: Int? = null): Observable<Payload<List<ChatFileBean>>>


    //获取IM用户对应信息
    @FormUrlEncoded
    @POST("/api/userFont/showInfo")
    fun showIMUserInfo(@Field("user_id") user_id: Int): Observable<Payload<UserBean>>

    //加入项目群组,第一次调用是创建群组
    @FormUrlEncoded
    @POST("/api/News/createJoinGroup")
    fun createJoinGroup(@Field("accid") accid: String, @Field("company_id") company_id: String): Observable<Payload<Int>>

    /**
     * 保存群id
     */
    @FormUrlEncoded
    @POST("/api/Message/saveGroup")
    fun saveGroupId(@Field("company_id") company_id: Int,
                    @Field("group_id") group_id: String): Observable<Payload<Any>>

    /**
     * 创建群组时获取群头像
     */
    @FormUrlEncoded
    @POST("/api/Message/groupImage")
    fun getTeamGroupHeader(@Field("uids") uids: String): Observable<Payload<String>>


    /**
     * 解散项目群
     */
    @FormUrlEncoded
    @POST("/api/Message/deleteGroup")
    fun deleteTeam(@Field("company_id") company_id: String,
                   @Field("group_id") group_id: String): Observable<Payload<Any>>
}