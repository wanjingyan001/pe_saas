package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2017/11/28.
 */
class SurveyDataBean : Serializable {
    var company: Company? = null
    var system: ManageSystem? = null
    var law: Law? = null
    var vocation: Vocation? = null
    var team: Team? = null

    //公司
    class Company {
        var developStage: String? = null//	发展阶段
        var generalStrategy: String? = null//整体战略
        var mainBusiness: String? = null//主营业务
        var porterAnalysis: String? = null//波特分析
        var productTechnique: String? = null//制造工艺/服务流程
        var occupancy: String? = null//市场占有率
        var marketAbility: String? = null//营销能力
        var execution: String? = null//执行力
        var contrast: String? = null//比较优势劣势
        var humanResource: String? = null//人力资源
        var culture: String? = null//文化
        var customerAnalysis: String? = null//客户分析
        var financeSituation: String? = null//过往融资情况
        var financeFile: String? = null//融资文件
        var potentialDebt: String? = null//潜在债务
        var marketBarriers: String? = null//上市障碍
        var riskChance: String? = null//风险与机遇
    }

    //管理制度
    class ManageSystem {
        var finance: String? = null//财务管理
        var staff: String? = null//人员管理
        var riskControl: String? = null//风控
        var business: String? = null//业务管理
        var conference: String? = null//会议
        var managerOffice: String? = null//总经办
        var supplyChain: String? = null//供应链管理
        var sell: String? = null//销售管理
        var production: String? = null//生产管理
        var quality: String? = null//品质管理
        var seal: String? = null//印章管理
    }

    //法律
    class Law {
        var history: String? = null//历史沿革
        var operation: String? = null//运营资质
        var compliance: String? = null//合规性
        var equityStructure: String? = null//股权结构
        var invisibleProperty: String? = null//无形资产情况
        var legalDispute: String? = null//法律纠纷
        var otherRisk: String? = null//其他风险提示
    }

    //行业
    class Vocation {
        var developHistory: String? = null//发展历史
        var industryChain: String? = null//产业链
        var scale: String? = null//规模
        var affinity: String? = null//集中度
        var increaseRate: String? = null//增大率
        var prosperity: String? = null//景气度
        var legal_regulatory: String? = null//法律法规
        var compete: String? = null//竞争
        var drive_factory: String? = null//驱动因素
        var relate: String? = null//关联产业情况
        var report: String? = null//行研报告
        var tradeRisk: String? = null//行业风险
    }

    //创始人及团队
    class Team {
        var details: String? = null//履历
        var behaviour: String? = null//品行
        var family: String? = null//家庭关系
        var assetLiability: String? = null//资产与债务
        var characteristic: String? = null//特点
        var contract: String? = null//契约精神
        var praise: String? = null//口碑
        var achievement: String? = null//过往业绩
        var shortcoming: String? = null//缺点
        var entrepreneur: String? = null//企业家精神
        var introduce: String? = null//团队介绍
        var encourage: String? = null//激励方式
    }
}