package com.sogukj.pe.service

import com.google.gson.JsonObject
import com.sogukj.pe.bean.*
import com.sogukj.pe.database.HomeFunctionReq
import com.sogukj.pe.database.MainFunIcon
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by admin on 2018/5/22.
 */
interface OtherService {
    //    @Headers(value = "Domain-Name: homeFunction")
    @FormUrlEncoded
    @POST("/api/Approve/getFundOrProject")
    fun listSelector(@FieldMap map: HashMap<String, Any>): Observable<Payload<List<CustomSealBean.ValueBean>>>

    @POST("/api/Message/getMessageIndex")
    fun msgIndex(): Observable<Payload<MessageIndexBean>>

    //可空（1=>待审批，2=>已审批）
    @FormUrlEncoded
    @POST("/api/Message/getMessageList")
    fun msgList(@Field("page") page: Int? = 1,
                @Field("pageSize") pageSize: Int? = 20,
                @Field("status") status: Int? = null,
                @Field("type") type: Int? = null,
                @Field("template_id") template_id: String? = null): Observable<Payload<ArrayList<MessageBean>>>

    //投资经理评价
    @FormUrlEncoded
    @POST("/api/Comment/assess")
    fun assess(@Field("company_id") company_id: Int,
               @Field("is_business") is_business: Int,
               @Field("is_ability") is_ability: Int): Observable<Payload<Any>>

    //首页小红点
    @POST("/api/Index/getNumber")
    fun getNumber(): Observable<Payload<NumberBean>>

    @FormUrlEncoded
    @POST
    fun qrNotify(@Url url: String, @Field("status") status: Int): Observable<Payload<Any>>

    @Headers(value = "Domain-Name: QRCode")
    @FormUrlEncoded
    @POST
    fun qrNotify_saas(@Url url: String, @Field("status") status: Int, @Field("phone") phone: String): Observable<Payload<Any>>


    //版本更新
    @Headers(value = "Domain-Name: upgrade")
    @POST("/api/Index/version")
    fun getVersion(): Observable<Payload<VersionBean>>

    /**
     * app首页按钮系列
     */
//    @Headers(value = "Domain-Name: homeFunction")
    @POST("/api/Index/homeButton")
    fun homeModuleButton(@Body req: HomeFunctionReq): Observable<Payload<List<MainFunIcon>>>


    @POST("/api/Message/sysMessageIndex")
    fun sysMsgIndex(): Observable<Payload<MessageIndexBean>>

    @FormUrlEncoded
    @POST("/api/Message/sysMessageInfo")
    fun sysMessageInfo(@Field("news_id") news_id: Int): Observable<Payload<GongGaoBean>>

    @POST("/api/Message/getNewPop")
    fun getNewPop(): Observable<Payload<MessageBean>>

    @FormUrlEncoded
    @POST("/api/Message/deleteSysNews")
    fun deleteSysNews(@Field("news_id") news_id: Int): Observable<Payload<Any>>

    //可空（1=>待审批，2=>已审批）
    @FormUrlEncoded
    @POST("/api/Message/sysMessageList")
    fun sysMsgList(@Field("page") page: Int? = 1,
                   @Field("pageSize") pageSize: Int? = 20): Observable<Payload<ArrayList<MessageBean>>>


    /**
     * 获取付费套餐
     */
    @POST("/api/Pay/payBillCombo")
    fun getPayType(): Observable<Payload<List<PackageBean>>>

    /**
     * 获取支付的订单信息
     */
    @POST("/api/Saas/Alipay")
    fun getPayInfo(@Body req: PayReq): Observable<Payload<String>>


    /**
     * 获取支付的订单信息
     */
    @FormUrlEncoded
    @POST("/api/Saas/Alipay")
    fun getPayInfo(@Field("key") key: String,
                   @Field("sid") sid: Int): Observable<Payload<String>>

    /**
     * 获取首页常用功能上方的4个功能按钮
     *
     */
    @POST("/api/index/bannerstatus")
    fun getFourModules(): Observable<Payload<List<MainModule>>>

    /**
     * 项目基本信息
     */
    @FormUrlEncoded
    @POST("/api/Company/showComBase")
    fun getProBuildInfo(@Field("company_id") company_id:Int): Observable<Payload<NewProjectInfo>>

    /**
     * 添加修改项目基本数据
     */
    @POST("/api/Company/newProject")
    fun createProjectBuild(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    /**
     * 立项信息填写
     */
    @FormUrlEncoded
    @POST("/api/Company/comFileList")
    fun getProApproveInfo(@Field("company_id") company_id:Int,@Field("floor") floor : Int,
                                   @Field("type") type : Int = 0 ): Observable<Payload<List<List<ProjectApproveInfo>>>>

    /**
     * 删除文件
     */
    @FormUrlEncoded
    @POST("/api/Company/deleteFile")
    fun deleteProjectFile(@Field("file_id") file_id:Int) : Observable<Payload<Any>>

    /**
     * 立项提交
     */
    @POST("/api/Company/uploadMoreFile")
    fun createProjectApprove(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    /**
     * 上传文件
     */
    @POST("/api/Company/uploadFlowFile")
    fun uploadProFile(@Body body: RequestBody): Observable<Payload<ProUploadFileBean>>

    /**
     * 删除OSS文件
     */
    @FormUrlEncoded
    @POST("/api/Company/deleteOssFile")
    fun deleteProjectOssFile(@Field("filepath") filepath:String) : Observable<Payload<Any>>

    /**
     * 提交审批
     */
    @POST("/api/Company/submitApprove")
    fun commitApprove(@Body map: HashMap<String, Any>): Observable<Payload<Any>>


    /**
     * 审批记录
     */
    @FormUrlEncoded
    @POST("/api/Company/approveRecord")
    fun getApproveRecord(@Field("company_id") company_id:Int,@Field("floor") floor : Int): Observable<Payload<ApproveRecordInfo>>

    /**
     * 分配审批
     */
    @FormUrlEncoded
    @POST("/api/Company/allotApproval")
    fun commitAllocationApprove(@Field("user_id") user_id : Int,@Field("company_id") company_id:Int,
                                @Field("floor") floor : Int): Observable<Payload<Any>>


    /**
     * 添加投资主体信息
     */
    @POST("/api/Company/submitInvest")
    fun addLinkFund(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    /**
     * 查看投资主体信息
     */
    @FormUrlEncoded
    @POST("/api/Company/investSubject")
    fun getLinkFund(@Field("company_id") company_id:Int): Observable<Payload<List<LinkFundBean>>>

    /**
     * 基金列表
     */
    @FormUrlEncoded
    @POST("/api/Archives/allProjectOrFund")
    fun getLinkFundList(@Field("type") type:Int = 2,@Field("flag") flag:Int = 1): Observable<Payload<List<InvestFundBean>>>


    /**
     * 尚荣新项目流程配置
     */
    @POST("/api/Company/hideButton")
    fun configApprove():Observable<Payload<JsonObject>>

    /**
     * 关于
     */
    @POST("/api/index/getxieyiurl")
    fun getAboutPage():Observable<Payload<AboutPageBean>>

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
                   @Field("pageSize") pageSize: Int? = 20): Observable<Payload<List<MineReceiptBean>>>

    /**
     * 发票历史
     */
    @FormUrlEncoded
    @POST("/api/Order/historyInvoice")
    fun billHisList(@Field("page") page: Int? = 1,
                      @Field("pageSize") pageSize: Int? = 20): Observable<Payload<List<InvoiceHisBean>>>

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
                    @Field("pageSize") pageSize: Int? = 20): Observable<Payload<List<InvoiceHisBean>>>


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
    fun getMineWalletDetail(@Field("type") type : Int): Observable<Payload<RechargeRecordBean>>

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
    fun getPayInfo(@Field("type") type: Int,@Field("fee") fee : String,
                   @Field("source")source:Int): Observable<Payload<String>>

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
    fun getAccountPayInfo(@Field("order_type")order_type:Int,@Field("order_count")order_count:Int,
                          @Field("pay_type")pay_type:Int,@Field("fee")fee:String,@Field("type_id")type_id:String ? = null):Observable<Payload<Any>>

    /**
     * 获取云盘文件内容
     */
    @GET("/api/cloud/getList")
    fun getMineCloudDishData(@Query("file_path") file_path:String):Observable<Payload<List<CloudFileBean>>>

    /**
     * 上传文件
     */
    @POST("/api/cloud/uploadLocalFile")
    fun uploadImFileToCloud(@Body body: RequestBody): Observable<Payload<Payload<Any>>>

    /**
     * 新建文件夹
     */
    @FormUrlEncoded
    @POST("/api/cloud/addFolder")
    fun createNewDir(@Field("file_path")file_path:String,@Field("folder_name")folder_name:String):Observable<Payload<Any>>
}