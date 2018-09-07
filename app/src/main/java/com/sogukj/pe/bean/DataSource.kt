package com.sogukj.pe.bean

/**
 * Created by admin on 2018/9/5.
 */
data class PdfBook(val title: String,
                   val name: String,
                   val url: String,
                   val time:String)


data class PatentItem(
		val mark: String,// 发明
		val name: String,// 多自由度并联动感平台数据实时采集系统及方法
        val number: String,// 201711336514.4
        val url: String,// Patent/201711336514'
        val summary: String,// 摘要:本发明提供多自由度并联动感平台数据实时采集系统及方法，该系统包括多自由度并联机构采集器、处理器和终端；处理器与多自由度并联机构采集器电连接，处理器读取所述电压传感器采集的运动数据，通过预处理得到运动参数，传输给所述终端；终端上还设有应用程序...
        val author: String,// 齐乐无穷(北京)文化传媒有限公司
        val date: String// 2017-12-14
)


data class PatentDetail(
		var name: String,// 多自由度并联动感平台数据实时采集系统及方法
		var number: String,// 201711336514.4
		var date: String,// 2017-12-14
		var summary: String,// 本发明提供多自由度并联动感平台数据实时采集系统及方法，该系统包括多自由度并联机构采集器、处理器和终端；处理器与多自由度并联机构采集器电连接，处理器读取所述电压传感器采集的运动数据，通过预处理得到运动参数，传输给所述终端；终端上还设有应用程序，应用程序接收用户输入的视频类型，并从存储器中选择与该视频类型对应的视频；应用程序根据运动参数选择视频中的帧画面，显示所述帧画面，提高了采集的实时性，实现仿真动作采集与平台运动实时的联动，提高运动仿真数据采集的准确性和易处理性，有助于提高运动仿真数据形成的快速性和简便性。
		var apply_name: String,// 齐乐无穷(北京)文化传媒有限公司
		var apply_addr: String,// 100016 北京市朝阳区东四环********(隐藏)
		var invent: String,// 吕冀
		var main_cnumber: String,// G06F3/01(2006.01)I
		var branch_cnumber: List<String>,
		var law: Law,
		var other: Other
)

data class Other(
		var 主权项: String,// 一种多自由度并联动感平台数据实时采集系统，其特征在于，包括多自由度并联机构采集器、处理器和终端；所述多自由度并联机构采集器包括多个电压传感器；所处理器与所述多自由度并联机构采集器电连接，所述处理器用于给多自由度并联机构采集器供电，所述处理器还用于读取所述电压传感器采集的运动数据，通过预处理得到运动参数，将运动参数传输给所述终端；所述终端的存储器中存储有多个视频，每个视频设置有视频类型；所述终端上还设有应用程序，所述应用程序用于接收用户输入的视频类型，并从存储器中选择与该视频类型对应的视频，以得到展示视频；所述应用程序还用于接收运动数据，对所述运动数据进行解析和过滤，以得到过滤数据，根据过滤数据选择展示视频中的帧画面，显示该帧画面。
		var 公开号: String,// 107967060A
		var 公开日: String,// 2018-04-27
		var 专利代理机构: String,// 北京寰华知识产权代理有限公司 11408
		var 代理人: String,// 林柳岑
		var 颁证日: String,//
		var 优先权: String,//
		var 国际申请: String,//
		var 国际公布: String,//
		var 进入国家日期: String//
)

data class Law(
		var date: String,// 2018-04-27
		var status: String// 公开
)