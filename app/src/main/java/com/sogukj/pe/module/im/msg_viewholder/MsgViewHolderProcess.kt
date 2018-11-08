package com.sogukj.pe.module.im.msg_viewholder

import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.bean.NewProjectProcess
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.project.originpro.OtherProjectShowActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity
import com.sogukj.pe.module.project.originpro.ProjectUploadShowActivity
import kotlinx.android.synthetic.main.item_im_project_process.view.*
import org.jetbrains.anko.startActivity

/**
 * Created by admin on 2018/11/2.
 */
class MsgViewHolderProcess(adapter: BaseMultiItemFetchLoadAdapter<*, *>) : MsgViewHolderBase(adapter)  {
    private lateinit var data: NewProjectProcess
    override fun getContentResId(): Int  = R.layout.item_im_project_process

    override fun inflateContentView() {

    }

    override fun bindContentView() {
        val attachment = message.attachment as ProcessAttachment
        data = attachment.bean
        view.approvalTip.text = "${data.subName}的${data.title}；\n在手机或电脑上快速处理审批！"
    }

    override fun onItemLongClick(): Boolean {
        return true
    }

    override fun onItemClick() {
        super.onItemClick()
        val floor = data.floor
        val project = ProjectBean()
        project.company_id = data.id
        project.floor = data.floor
        project.name = data.cName
        when (floor) {
            2 -> {
                //立项
                context.startActivity<ProjectApprovalShowActivity>(Extras.DATA to project, Extras.FLAG to floor)
            }
            3 -> {
                //预审会
                context.startActivity<ProjectUploadShowActivity>(Extras.DATA to project, Extras.FLAG to floor)
            }
            4 -> {
                //投决会
                context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                        Extras.TITLE to "投决会")
            }
            5 -> {
                //签约付款
                context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                        Extras.TITLE to "签约付款")
            }
            6 -> {
                //投后管理
                context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                        Extras.TITLE to "投后管理")
            }
            7 -> {
                //退出管理
                context.startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to floor,
                        Extras.TITLE to "退出管理")
            }
        }
    }
}