package com.sogukj.pe.module.register

import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.Contact
import com.sogukj.pe.bean.MyContacts
import com.sogukj.pe.peExtended.firstLetter
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_invite_main.*
import org.jetbrains.anko.*

class InviteMainActivity : ToolbarActivity() {
    private lateinit var mAdapter: InviteAdapter
    private var inviteCode: String? = null
    private lateinit var invitePath: String
    private lateinit var header: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_main)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "邀请同事加入"
        setBack(true)
        val data = inviteData()
        mAdapter = InviteAdapter(data)
        contactsList.apply {
            layoutManager = LinearLayoutManager(this@InviteMainActivity)
            adapter = mAdapter
        }
        header = layoutInflater.inflate(R.layout.layout_invite_main_header, contactsList.parent as ViewGroup, false)
        mAdapter.addHeaderView(header)
        header.find<View>(R.id.addByPhoneLayout).clickWithTrigger {
            startActivity<InviteByPhoneActivity>(Extras.DATA to inviteCode)
        }
        header.find<View>(R.id.addByShareLayout).clickWithTrigger {
            this::invitePath.isInitialized.yes {
                startActivity<InviteByCodeActivity>(Extras.DATA to inviteCode, Extras.DATA2 to invitePath)
            }
        }
        header.find<View>(R.id.addByPCLayout).clickWithTrigger {
            startActivity<InviteByPcActivity>()
        }
        mAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val myContacts = adapter.data[position] as MyContacts
            sendInviteMessage(myContacts.t.phone, myContacts.t.name)
        }
        sideBar.setOnLetterChangedListener { index, c ->
            mAdapter.data.forEachIndexed { i, myContacts ->
                if (myContacts.isHeader && myContacts.header == c) {
                    val manager = contactsList.layoutManager as LinearLayoutManager
                    manager.scrollToPositionWithOffset(i, 0)
                }
            }
        }
        getInviteCode()
        initSearch()
    }

    private fun initSearch() {
        mSearchView.doSearch = { key ->
            if (key.isNotEmpty()) {
                val filter = mAdapter.data.filter {
                    if (!it.isHeader) {
                        it.t.name.contains(key) || it.t.phone.contains(key)
                    } else {
                        it.header.isEmpty()
                    }
                }
                if (filter.isNotEmpty()) {
                    mAdapter.removeHeaderView(header)
                    mAdapter.data.clear()
                    mAdapter.data.addAll(filter)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
        mSearchView.onTextChange = { text ->
            if (text.isEmpty()) {
                val data = inviteData()
                if (mAdapter.headerLayoutCount == 0) {
                    mAdapter.addHeaderView(header)
                    mAdapter.data.clear()
                    mAdapter.data.addAll(data)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun getContacts(): Map<String, String> {
        val map = HashMap<String, String>()
        //取得电话本中开始一项的光标
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        //向下移动光标
        while (cursor.moveToNext()) {
            val phones = ArrayList<String>()
            //取得联系人名字
            val nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val contact = cursor.getString(nameFieldColumnIndex)
            //取得电话号码
            val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val phone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null)
            while (phone.moveToNext()) {
                var phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                //格式化手机号
                phoneNumber = phoneNumber.replace("-", "").replace(" ", "")
                phones.add(phoneNumber)
            }
            map.put(contact, if (phones.isEmpty()) "" else phones[0])
            phone.close()
        }
        cursor.close()
        return map
    }


    private fun inviteData(): List<MyContacts> {
        val data =  ArrayList<MyContacts>()
        try {
            val map = getContacts()
            val contacts = ArrayList<Contact>()
            map.asIterable().forEach {
                val contact = Contact(it.key, it.value)
                contacts.add(contact)
            }
            contacts.sortBy { it.name.firstLetter }
            contacts.forEachIndexed { index, contact ->
                val thisLetter = contact.name.firstLetter
                if (index == 0) {
                    data.add(MyContacts(true, thisLetter))
                } else {
                    val lastLetter = contacts[index - 1].name.firstLetter
                    if (thisLetter != lastLetter) {
                        data.add(MyContacts(true, thisLetter))
                    }
                }
                data.add(MyContacts(contact))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }


    private fun getInviteCode() {
        val key = sp.getString(Extras.CompanyKey, "")
        if (key.isNotEmpty()) {
            SoguApi.getService(application, RegisterService::class.java).getInviteCode(key)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                payload.payload?.let {
                                    inviteCode = it.code
                                    invitePath = it.path
                                }
                            } else {
                                showTopSnackBar(payload.message)
                            }
                        }
                    }
        }
    }

    private fun sendInviteMessage(phone: String, name: String) {
        inviteCode?.let {
            SoguApi.getService(application, RegisterService::class.java).inviteByPhone(phone, it, name)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                showSuccessToast("邀请成功")
                            } else {
                                showTopSnackBar(payload.message)
                            }
                        }
                    }
        }
    }
}
