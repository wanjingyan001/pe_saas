/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: DDService
 * Author: admin
 * Date: 2018/11/15 下午4:03
 * Description: 钉钉接口(https://open-doc.dingtalk.com/microapp/native/ddvlch)
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 *
 */
package com.sogukj.pe.ddshare

import io.reactivex.Observable
import retrofit2.http.*

/**
 *
 * @Description:    钉钉登录接口
 * @Author:         万经言
 * @CreateDate:     2018/11/15 下午4:03
 * @UpdateUser:     更新者
 * @UpdateDate:     2018/11/15 下午4:03
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
interface DDService {
    /**
     * 获取account_token
     */
    @GET("/sns/gettoken")
    fun getAccountToken(@Query("appid") appid: String = DDShareActivity.DDApp_Id,
                        @Query("appsecret") appsecret: String = DDShareActivity.DD_APP_SECRET)
            : Observable<AccountToken>

    @POST("/sns/get_persistent_code")
    fun getAuthorizeCode(@Query("access_token") access_token: String,
                         @Body tmp_auth_code: AuthorizeReq): Observable<AuthorizeCode>


    @FormUrlEncoded
    @POST("/api/Dingding/getUnionid")
    fun getUnionid(@Field("code")code:String):Observable<String>

}