package com.sogukj.pe.module.register

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.chad.library.adapter.base.BaseQuickAdapter
import com.sogukj.pe.R
import com.sogukj.pe.R.drawable.contact
import com.sogukj.pe.R.layout.header
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.CharacterParser
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.Contact
import com.sogukj.pe.bean.MyContacts
import com.sogukj.pe.peExtended.firstLetter
import kotlinx.android.synthetic.main.activity_invite_main.*
import kotlinx.android.synthetic.main.layout_invite_main_header.*
import kotlinx.android.synthetic.main.layout_invite_main_header.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onEditorAction
import qdx.stickyheaderdecoration.NormalDecoration

class InviteMainActivity : ToolbarActivity() {
    private lateinit var mAdapter: InviteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_main)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "邀请同事加入"
        setBack(true)
        val map = getContacts()
        //所有联系人首字母list
//        val list = map.filterKeys { it.isNotEmpty() }.map {
//            it.key.firstLetter
//        }.toSet().toList().sorted()

        val data = ArrayList<MyContacts>()
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
            }else{
                val lastLetter = contacts[index - 1].name.firstLetter
                if (thisLetter != lastLetter){
                    data.add(MyContacts(true, thisLetter))
                }
            }
            data.add(MyContacts(contact))
        }
        mAdapter = InviteAdapter(data)
        contactsList.apply {
            layoutManager = LinearLayoutManager(this@InviteMainActivity)
            adapter = mAdapter
        }
        val header = layoutInflater.inflate(R.layout.layout_invite_main_header, contactsList.parent as ViewGroup, false)
        mAdapter.addHeaderView(header)
        header.find<View>(R.id.addByPhoneLayout).clickWithTrigger {
            startActivity<InviteByPhoneActivity>()
        }
        header.find<View>(R.id.addByShareLayout).clickWithTrigger {
            startActivity<InviteByCodeActivity>()
        }
        header.find<View>(R.id.addByPCLayout).clickWithTrigger {
            startActivity<InviteByPcActivity>()
        }
        mAdapter.onItemChildClickListener  = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val myContacts = adapter.data[position] as MyContacts
            info { "姓名:${myContacts.t.name}===>电话:${myContacts.t.phone}" }
        }
        sideBar.setOnLetterChangedListener { index, c ->
            mAdapter.data.forEachIndexed { i, myContacts ->
                if (myContacts.isHeader && myContacts.header == c){
                    val manager = contactsList.layoutManager as LinearLayoutManager
                   manager.scrollToPositionWithOffset(i,0)
                }
            }
        }
        mSearchView.filters = Utils.getFilter(this)
        mSearchView.onEditorAction { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

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
}
