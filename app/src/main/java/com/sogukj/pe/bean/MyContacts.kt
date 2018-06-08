package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.SectionEntity

/**
 * Created by admin on 2018/6/6.
 */
class MyContacts : SectionEntity<Contact> {

    constructor(t: Contact) : super(t)

    constructor(isHeader: Boolean, header: String) : super(isHeader, header)
}

data class Contact(val name: String, val phone: String)