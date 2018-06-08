package com.sogukj.pe.module.register

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.bean.MyContacts

/**
 * Created by admin on 2018/6/6.
 */
class InviteAdapter(data: List<MyContacts>)
    : BaseSectionQuickAdapter<MyContacts, BaseViewHolder>(R.layout.item_invite_contact,R.layout.layout_inviter_group_header, data) {
    override fun convertHead(helper: BaseViewHolder, item: MyContacts) {
        helper.getView<TextView>(R.id.letter).text = item.header
    }

    override fun convert(helper: BaseViewHolder, item: MyContacts) {
        val name = helper.getView<TextView>(R.id.contactName)
        val phone = helper.getView<TextView>(R.id.contactPhone)
        name.text = item.t.name
        phone.text = item.t.phone
        helper.addOnClickListener(R.id.addSelected)
    }

}