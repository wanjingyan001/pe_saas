package com.sogukj.pe.service

import com.google.gson.JsonObject
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by CH-ZH on 2018/11/7.
 */
interface StaticHttpUtils {
    /**
     * 提交发票信息
     */
    @POST("/api/Order/submitInvoice")
    fun submitBillDetail(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    /**
     * 订单列表
     */
    @FormUrlEncoded
    @POST("/api/Order/invoiceOrderList")
    fun billOrderList(@Field("page") page: Int? = 1,
                      @Field("pageSize") pageSize: Int? = 15): Observable<Payload<List<MineReceiptBean>>>

    /**
     * 发票历史
     */
    @FormUrlEncoded
    @POST("/api/Order/historyInvoice")
    fun billHisList(@Field("page") page: Int? = 1,
                    @Field("pageSize") pageSize: Int? = 15): Observable<Payload<List<InvoiceHisBean>>>

    /**
     * 发票详情
     */
    @FormUrlEncoded
    @POST("/api/Order/invoiceInfo")
    fun getBillDetailInfo(@Field("id") id: Int): Observable<Payload<BillDetailBean>>

    /**
     * 新增发票抬头
     */
    @POST("/api/Order/addInvoiceTitle")
    fun addBillHeader(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    /**
     * 发票抬头
     */
    @FormUrlEncoded
    @POST("/api/Order/getTitleList")
    fun getBillHeaderList(@Field("page") page: Int? = 1,
                          @Field("pageSize") pageSize: Int? = 15): Observable<Payload<List<InvoiceHisBean>>>


    /**
     * 我的钱包
     */
    @POST("/api/Pay/getwalletInfo")
    fun getMineWalletData(): Observable<Payload<List<MineWalletBean>>>

    /**
     * 钱包明细
     */
    @FormUrlEncoded
    @POST("/api/Pay/WalletDetial")
    fun getMineWalletDetail(@Field("type") type: Int): Observable<Payload<RechargeRecordBean>>

    /**
     * 发票详情
     */
    @FormUrlEncoded
    @POST("/api/Order/getTitleInfo")
    fun getBillHeaderInfo(@Field("id") id: Int): Observable<Payload<BillDetailBean>>

    /**
     * 钱包充值
     */
    @FormUrlEncoded
    @POST("/api/Pay/rechargeWallet")
    fun getPayInfo(@Field("type") type: Int, @Field("fee") fee: String,
                   @Field("source") source: Int): Observable<Payload<String>>

    /**
     * 舆情开启状态
     */
    @FormUrlEncoded
    @POST("/api/Pay/getYuqinInfo")
    fun getSentimentInfo(@Field("company_id") company_id: Int): Observable<Payload<SentimentInfoBean>>

    /**
     * 舆情开关
     */
    @FormUrlEncoded
    @POST("/api/Pay/getYuqinOpen")
    fun setSentimentStatus(@Field("company_id") company_id: Int): Observable<Payload<Any>>

    /**
     * 个人账户信息
     */
    @POST("/api/Pay/getAccountPoson")
    fun getPersonAccountInfo(): Observable<Payload<RechargeRecordBean>>

    /**
     * 企业账户信息
     */
    @POST("/api/Pay/getAccountBusiness")
    fun getBussAccountInfo(): Observable<Payload<RechargeRecordBean>>

    /**
     * 账户支付
     */
    @FormUrlEncoded
    @POST("/api/Pay/WalletPay")
    fun getAccountPayInfo(@Field("order_type") order_type: Int, @Field("order_count") order_count: Int,
                          @Field("pay_type") pay_type: Int, @Field("fee") fee: String, @Field("type_id") type_id: String? = null): Observable<Payload<Any>>

    /**
     * 获取云盘文件内容
     */
    @GET("/api/cloud/getList")
    fun getMineCloudDishData(@Query("file_path") file_path: String, @Query("phone") phone: String): Observable<Payload<List<CloudFileBean>>>

    /**
     * 上传文件
     */
    @POST("/api/cloud/uploadLocalFile")
    fun uploadImFileToCloud(@Body body: RequestBody): Observable<Payload<Any>>

    /**
     * 新建文件夹
     */
    @FormUrlEncoded
    @POST("/api/cloud/addFolder")
    fun createNewDir(@Field("file_path") file_path: String, @Field("folder_name") folder_name: String,
                     @Field("phone") phone: String): Observable<Payload<Any>>

    /**
     * 文件动态
     */
    @GET("/api/cloud/getFileNews")
    fun getFileDynamicData(@Query("page") page: Int? = 1, @Query("phone") phone: String,
                           @Query("limit") limit: Int? = 15): Observable<Payload<List<FileDynamicBean>>>

    /**
     * 文件筛选类型
     */
    @GET("/api/cloud/filterFileType")
    fun getFileFillterData(@Query("page") page: Int? = 1, @Query("phone") phone: String,
                           @Query("search") search: String, @Query("file_path") file_path: String
                           , @Query("size") size: Int? = 15): Observable<Payload<List<CloudFileBean>>>

    /**
     * 文件搜索
     */
    @GET("/api/cloud/searchFileList")
    fun getFileSearchData(@Query("page") page: Int? = 1, @Query("phone") phone: String,
                          @Query("search") search: String, @Query("size") size: Int? = 15): Observable<Payload<List<CloudFileBean>>>

    /**
     * 文件夹/文件删除
     */
    @GET("/api/cloud/deleteFolder")
    fun deleteCloudFile(@Query("file_path") file_path: String, @Query("phone") phone: String): Observable<Payload<Any>>

    /**
     * 文件夹/文件批量删除
     */
    @FormUrlEncoded
    @POST("/api/cloud/multipleDeleteFolder")
    fun deleteBatchCloudFile(@Field("file_path") file_path: String, @Field("phone") phone: String): Observable<Payload<Any>>

    /**
     * 文件/文件夹移动，重命名
     */
    @FormUrlEncoded
    @POST("/api/cloud/moveFolder")
    fun cloudFileRemoveOrRename(@Field("file_path") file_path: String, @Field("new_file_path") new_file_path: String,
                                @Field("phone") phone: String): Observable<Payload<Any>>

    /**
     * 文件复制
     */
    @FormUrlEncoded
    @POST("/api/cloud/copyFolder")
    fun copyCloudFile(@Field("file_path") file_path: String, @Field("new_file_path") new_file_path: String,
                      @Field("phone") phone: String, @Field("file_name") file_name: String): Observable<Payload<Any>>


    /**
     * 获取文件预览路径
     */
    @GET("/api/cloud/getPreviewFilePath")
    fun getFilePreviewPath(@Query("file_path") file_path: String, @Query("phone") phone: String): Observable<Payload<JsonObject>>

    /**
     * 文件夹/文件批量移动
     */
    @FormUrlEncoded
    @POST("/api/cloud/multipleMoveFolder")
    fun removeBatchCloudFile(@Field("file_path") file_path: String, @Field("phone") phone: String): Observable<Payload<Any>>

    /**
     * 获取个人文件容量与已使用量
     */
    @GET("/api/cloud/getSelfFileCapacity")
    fun getDirMemoryData(@Query("file_path") file_path: String, @Query("phone") phone: String): Observable<Payload<JsonObject>>

    /**
     * 发票抬头信息匹配
     */
    @FormUrlEncoded
    @POST("/api/Order/searchTitle")
    fun searchReceiptTitle(@Field("search") search: String, @Field("page") page: Int,
                           @Field("pageSize") pageSize: Int = 15): Observable<Payload<List<SearchReceiptBean>>>

    /**
     * 获取付费套餐
     */
    @POST("/api/Pay/payBillCombo")
    fun getPayType(): Observable<Payload<List<PackageBean>>>

    /**
     * 扩容套餐购买
     */
    @POST("/api/pay/BillComboPay")
    fun getDilatationPayInfo(@Body req: PayReq): Observable<Payload<Any>>
}