package com.sogukj.pe.service

import com.sogukj.pe.Consts
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by admin on 2018/5/23.
 */
interface InfoService {
    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Volatility")
    fun stockInfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<StockBean>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Companyinfo")
    fun companyInfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<CompanyBean>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Seniorexecutive")
    fun listSeniorExecutive(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<GaoGuanBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Holdingcompany")
    fun cangu(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<CanGuBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Announcement")
    fun listAnnouncement(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AnnouncementBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Issuerelated")
    fun issueInfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<IssueRelatedBean>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Equitychange")
    fun listEquityChange(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<EquityChangeBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Bonusinfo")
    fun listBonusInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BonusBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Allotmen")
    fun listAllotment(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AllotmentBean>>>

    @FormUrlEncoded
    @POST("/api/Listinformation/Sgcompanyinfo")
    fun bizinfo(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<BizInfoBean>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Holder")
    fun listShareholderInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ShareHolderBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Annualreport")
    fun listAnnualReport(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 50
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AnnualReportBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Changeinfo")
    fun listChangeRecord(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ChangeRecordBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Invest")
    fun listInvestment(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<InvestmentBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Staff")
    fun listKeyPersonal(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<KeyPersonalBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Findhistoryrongzi")
    fun listFinanceHistory(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<FinanceHistoryBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Findtzanli")
    fun listInvestEvent(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<InvestEventBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Findteammember")
    fun listCoreTeam(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TeamMemberBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Getproductinfo")
    fun listBizInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ProductBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Findjingpin")
    fun listProductInfo(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<ProductBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Employments")
    fun listRecruit(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<RecruitBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Bond")
    fun listBond(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BondBean>>>


    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Taxcredit")
    fun listTaxRate(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TaxRateBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Purchaseland")
    fun listLandPurchase(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<LandPurchaseBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Bids")
    fun listBids(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BidBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Qualification")
    fun listQualification(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<QualificationBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Checkinfo")
    fun listCheck(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<CheckBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Appbkinfo")
    fun listApp(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<AppBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Tm")
    fun listBrand(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BrandBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Patents")
    fun listPatent(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<PatentBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Icp")
    fun listICP(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<IcpBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Listinformation/Branch")
    fun listBranch(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<BranchBean>>>

    //非空（当type=1时1=>项目投资档案清单，2=>投资后项目跟踪管理清单，3=>项目推出档案清单    当type=2时,1=>储备期档案,2=>  存续期档案,3=> 退出期档案)
    @FormUrlEncoded
    @POST("/api/Listinformation/projectBook")
    fun projectBook(@Field("type") type: Int, @Field("company_id") company_id: Int): Observable<Payload<ProjectBookRSBean>>

    @POST("/api/Listinformation/projectFilter")
    fun projectFilter(): Observable<Payload<Map<Int, String>>>

    @POST("/api/Listinformation/uploadBook")
    fun uploadBook(@Body body: RequestBody): Observable<Payload<Object>>

    //type	number	1	文件类型	非空（1=>项目文书，2=>基金类型）
    @FormUrlEncoded
    @POST("/api/Listinformation/projectSelect")
    fun projectBookSearch(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("type") type: Int
            , @Field("fileClass") fileClass: String? = null
            , @Field("fuzzyQuery") fuzzyQuery: String? = null
            , @Field("status") status: Int? = null): Observable<Payload<List<ProjectBookBean>>>

    // 投资项目管理-立项尽调数据
    @FormUrlEncoded
    @POST("/api/Listinformation/surveyData")
    fun surveyData(@Field("company_id") company_id: Int): Observable<Payload<SurveyDataBean>>

    // 添加或修改立项尽调数据
    @POST("/api/Listinformation/addEditSurveyData")
    fun addEditSurveyData(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    // 投资项目管理-投资决策数据
    @FormUrlEncoded
    @POST("/api/Listinformation/investSuggest")
    fun investSuggest(@Field("company_id") company_id: Int): Observable<Payload<InvestSuggestBean>>

    // 投资项目管理-添加或修改投资决策数据
    @POST("/api/Listinformation/addEditInvestSuggest")
    fun addEditInvestSuggest(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    // 投资项目管理-投后管理数据
    @FormUrlEncoded
    @POST("/api/Listinformation/manageData")
    fun manageData(@Field("company_id") company_id: Int): Observable<Payload<ManageDataBean>>

    // 投资项目管理-添加或修改投后管理数据
    @POST("/api/Listinformation/addEditManageData")
    fun addEditManageData(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    //项目文书|基金档案列表
    // fc_id	int	 	公司ID	非空
    //type	int	 	文件所属项目类型	非空（1=>项目，2=>基金）
    //dir_id	int	 	所属目录	可空
    //page	int	1	页数	可空
    //pageSize	number	20	每页显示大小	可空
    //fileClass	number	 	分类筛选	可空(当type=1时选项有效，值为首次进入此页面返回的filter=>id)多选以逗号隔开类似 => 1,2
    //query	string	 	搜索关键字	可空
    //mark_id	int	 	文件格式	(可空)1=>文档类文件,2=>表格类文件,3=>演示类文件，4=>pdf类文件,5=>图片类文件，6=>其他
    @FormUrlEncoded
    @POST("/api/Listinformation/fileList")
    fun fileList(@Field("fc_id") fc_id: Int,
                 @Field("type") type: Int,
                 @Field("dir_id") dir_id: Int? = null,
                 @Field("page") page: Int? = 1,
                 @Field("pageSize") pageSize: Int = 20,
                 @Field("fileClass") fileClass: String? = null,
                 @Field("query") query: String? = null,
                 @Field("mark_id") mark_id: Int? = null): Observable<Payload<ArrayList<FileListBean>>>


    @POST("/api/Listinformation/uploadArchives")
    fun uploadArchives(@Body body: RequestBody): Observable<Payload<UploadFileBean>>

    //项目文书|基金档案文件 目录
    //type	number	1	文件类型	非空（1=>项目，2=>基金）
    //fc_id	number	 	项目或者基金id	非空
    @FormUrlEncoded
    @POST("/api/Listinformation/showCatalog")
    fun showCatalog(@Field("type") type: Int = 1, @Field("fc_id") fc_id: Int): Observable<Payload<ArrayList<DirBean>>>
}