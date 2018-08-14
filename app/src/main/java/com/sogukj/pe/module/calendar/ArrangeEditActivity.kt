package com.sogukj.pe.module.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.widgets.WorkArrangePerson
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_arrange_edit.*
import org.jetbrains.anko.*

//When writing this code, only God and I know what it is doing.
//Now, only God knows
class ArrangeEditActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.arrange_edit_save
    private lateinit var editAdapter: RecyclerAdapter<NewArrangeBean>
    val attendRequestCode = 0x001
    val participateRequestCode = 0x002
    lateinit var data: ArrayList<NewArrangeBean>
    private var currentBean: NewArrangeBean? = null
    private var currentPosition = 0
    private var position: Int by extraDelegate(Extras.INDEX, -1)
    private val mine by lazy { Store.store.getUser(this) }

    companion object {
        fun start(context: Activity, weeklyData: ArrayList<NewArrangeBean>, offset: String?, position: Int? = null) {
            val intent = Intent(context, ArrangeEditActivity::class.java)
            intent.putExtra(Extras.LIST, weeklyData)
            intent.putExtra(Extras.ID, offset)
            intent.putExtra(Extras.INDEX, position)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrange_edit)
        title = "班子工作安排详情"
        setBack(true)
        data = intent.getSerializableExtra(Extras.LIST) as ArrayList<NewArrangeBean>
        val offset = intent.getStringExtra(Extras.ID)
        supportInvalidateOptionsMenu()
        if (data.size == 1) {
            if (data[0].child.size == 1 && data[0].child[0].id == 0) {
                saveLayout.setVisible(false)
            } else {
                saveLayout.setVisible(true)
            }
        } else {
            saveLayout.setVisible(false)
        }
        editAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ArrangeHolder(_adapter.getView(R.layout.item_arrange_edit, parent))
        }
        editAdapter.dataList.addAll(data)
        arrangeEditList.setItemViewCacheSize(0)
        arrangeEditList.layoutManager = LinearLayoutManager(this)
        arrangeEditList.adapter = editAdapter
        saveBtn.clickWithTrigger {
            submitWeeklyWork(true)
        }
        if (offset != null) {
            val inflate = layoutInflater.inflate(R.layout.layout_arrange_weekly_header, arrangeEditList, false)
            arrangeEditList.addHeaderView(inflate)
            when (offset.toInt()) {
                -1 -> {
                    inflate.backgroundResource = R.drawable.bg_last_week
                    inflate.find<TextView>(R.id.weeklyTv).text = "上周"
                }
                0 -> {
                    inflate.backgroundResource = R.drawable.bg_this_week
                    inflate.find<TextView>(R.id.weeklyTv).text = "本周"
                }
                1 -> {
                    inflate.backgroundResource = R.drawable.bg_next_week
                    inflate.find<TextView>(R.id.weeklyTv).text = "下周"
                }
                else -> {
                    inflate.backgroundColor = Color.parseColor("#f7f9fc")
                    val firstTime = data[0].date
                    val lastTime = data[6].date
                    inflate.find<TextView>(R.id.weeklyTv).text = "${firstTime?.substring(5, firstTime.length)}~${lastTime?.substring(5, lastTime.length)}"

                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        if (data.size == 1 && (data[0].child.size > 1 || data[0].child[0].id != 0)) {
            menuInflater.inflate(R.menu.arrange_edit_delete, menu)
        } else {
            menuInflater.inflate(R.menu.arrange_edit_save, menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save_edit -> {
                submitWeeklyWork(true)
            }
            R.id.arrange_delete -> {
                MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .content("是否删除本条工作安排")
                        .positiveText("确定")
                        .negativeText("取消")
                        .onPositive { dialog, which ->
                            dialog.dismiss()
                            data[0].child[position].apply {
                                reasons = ""
                                place = ""
                                attendee = ArrayList()
                                participant = ArrayList()
                            }
                            submitWeeklyWork(false)
                        }
                        .onNegative { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Extras.RESULTCODE && currentBean != null) {
            val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            val bean = this.data.find { it.pid == currentBean!!.pid }
            val changeBean = if (this.data.size == 1&& !hasEmpty()) currentBean!!.child[position] else currentBean!!.child[currentPosition]
            when (requestCode) {
                attendRequestCode -> {
                    runOnUiThread {
                        bean?.let {
                            val childBean = if (this.data.size == 1 && !hasEmpty()) it.child[position] else it.child[currentPosition]
                            childBean.attendee = list.map { Person(it.uid!!, it.url, it.name, it.position, it.depart_name) }
                            childBean.create_id = mine!!.uid!!
                            childBean.lv = mine!!.is_admin

                            changeBean.attendee = list.map { Person(it.uid!!, it.url, it.name, it.position, it.depart_name) }
                            arrangeEditList.adapter = editAdapter
                        }
                    }
                }
                participateRequestCode -> {
                    runOnUiThread {
                        bean?.let {
                            val childBean = if (this.data.size == 1 && !hasEmpty()) it.child[position] else it.child[currentPosition]
                            childBean.participant = list.map { Person(it.uid!!, it.url, it.name, it.position, it.depart_name) }
                            childBean.create_id = mine!!.uid!!
                            childBean.lv = mine!!.is_admin

                            changeBean.participant = list.map { Person(it.uid!!, it.url, it.name, it.position, it.depart_name) }
                            arrangeEditList.adapter = editAdapter
                        }
                    }
                }
            }
        }
    }

    private fun submitWeeklyWork(isSave: Boolean) {
        this.data.forEach {
            info { "${it.jsonStr} \n" }
        }
        SoguApi.getService(application, CalendarService::class.java)
                .getWeeklyWorkList(ArrangeReqBean(flag = 2, data = this.data))
                .execute {
                    onSubscribe {
                        showProgress("正在保存")
                    }
                    onNext { payload ->
                        if (payload.isOk) {
                            if (isSave) {
                                showSuccessToast("保存成功")
                            } else {
                                showSuccessToast("删除成功")
                            }
                            val intent = Intent()
                            intent.putExtra(Extras.DATA, data)
                            setResult(Extras.RESULTCODE, intent)
                            finish()
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                    onError {
                        hideProgress()
                    }
                    onComplete {
                        hideProgress()
                    }
                }
    }

    private fun hasEmpty() = data[0].child.find { it.id == 0 } != null

    inner class ArrangeHolder(item: View) : RecyclerHolder<NewArrangeBean>(item) {
        val list = item.find<RecyclerView>(R.id.arrangeContentList)
        val add = item.find<TextView>(R.id.addArrange)
        val addLayout = item.find<LinearLayout>(R.id.addLayout)
        override fun setData(view: View, data: NewArrangeBean, position: Int) {
            val recyclerAdapter = RecyclerAdapter<ChildBean>(ctx) { _adapter, parent, _ ->
                EditHolder(_adapter.getView(R.layout.item_arrange_edit_child, parent), data, position)
            }
            if (this@ArrangeEditActivity.position != -1) {
                recyclerAdapter.dataList.add(data.child[this@ArrangeEditActivity.position])
            } else {
                recyclerAdapter.dataList.addAll(data.child)
            }
            list.apply {
                layoutManager = LinearLayoutManager(ctx)
                adapter = recyclerAdapter
            }
            addLayout.setVisible(this@ArrangeEditActivity.data.size > 1 || hasEmpty())
            add.clickWithTrigger {
                if (hasEmpty()) {
                    if (this@ArrangeEditActivity.data[0].child.find { it.create_id == 0 } != null) {
                        showErrorToast("请填写内容后再新增安排")
                        return@clickWithTrigger
                    }
                }
                mine?.let {
                    this@ArrangeEditActivity.data[position].child.add(ChildBean(0, create_id = it.uid!!, lv = it.is_admin))
                    recyclerAdapter.notifyItemInserted(data.child.size - 1)
//                    this@ArrangeEditActivity.position = editAdapter.dataList[position].child.size - 1
                    arrangeEditList.adapter = editAdapter
                }
            }
        }
    }

    inner class EditHolder(convertView: View, private val arrangeBean: NewArrangeBean, private val parentPosition: Int)
        : RecyclerHolder<ChildBean>(convertView) {
        val weeklyTv = convertView.find<TextView>(R.id.weeklyTv)
        val dayOfMonth = convertView.find<TextView>(R.id.dayOfMonth)
        val workContentEdit = convertView.find<EditText>(R.id.workContentEdit)
        val attendLayout = convertView.find<LinearLayout>(R.id.attendLayout)
        val personAttend = convertView.find<WorkArrangePerson>(R.id.person_attend)
        val participateLayout = convertView.find<LinearLayout>(R.id.participateLayout)
        val personParticipate = convertView.find<WorkArrangePerson>(R.id.person_participate)
        val addressLayout = convertView.find<LinearLayout>(R.id.addressLayout)
        val addressEdit = convertView.find<EditText>(R.id.addressEdit)

        override fun setData(view: View, data: ChildBean, position: Int) {
            weeklyTv.text = arrangeBean.weekday
            dayOfMonth.text = arrangeBean.date.substring(5, arrangeBean.date.length)
            val childBean = if (this@ArrangeEditActivity.data.size == 1 && !hasEmpty())
                this@ArrangeEditActivity.data[0].child[this@ArrangeEditActivity.position]
            else
                this@ArrangeEditActivity.data[parentPosition].child[position]

            workContentEdit.filters = Utils.getFilter(ctx)
            addressEdit.filters = Utils.getFilter(ctx)
            if (data.reasons.isNullOrEmpty()) {
                workContentEdit.setText("")
            }
            data.reasons?.let {
                workContentEdit.setText(it.trim())
            }
            val canEdit = (mine!!.is_admin > data.lv) or (mine!!.uid == data.create_id) or (data.create_id == 0)
            workContentEdit.isEnabled = canEdit
            attendLayout.isEnabled = canEdit
            participateLayout.isEnabled = canEdit
            addressEdit.isEnabled = canEdit
            val attList = ArrayList<UserBean>()
            data.attendee?.let {
                it.forEach {
                    val bean = UserBean()
                    bean.uid = it.uid
                    bean.url = it.url
                    bean.name = it.name
                    it.position.let {
                        bean.position = it
                    }
                    attList.add(bean)
                }
                if (attList.isNotEmpty()) {
                    personAttend.setPersons(attList)
                } else {
                    personAttend.setPersons(ArrayList())
                }
            }
            val particList = ArrayList<UserBean>()
            data.participant?.let {
                it.forEach {
                    val bean = UserBean()
                    bean.uid = it.uid
                    bean.url = it.url
                    bean.name = it.name
                    it.position.let {
                        bean.position = it
                    }
                    particList.add(bean)
                }
                if (particList.isNotEmpty()) {
                    personParticipate.setPersons(particList)
                } else {
                    personParticipate.setPersons(ArrayList())
                }
            }
            data.place?.let {
                addressEdit.setText(it.trim())
            }
            attendLayout.setOnClickListener {
                currentBean = arrangeBean
                currentPosition = position
                ContactsActivity.start(this@ArrangeEditActivity, attList, true, false, attendRequestCode)
            }
            participateLayout.setOnClickListener {
                currentBean = arrangeBean
                currentPosition = position
                ContactsActivity.start(this@ArrangeEditActivity, particList, true, false, participateRequestCode)
            }
            val contentWatcher: TextWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    childBean.reasons = workContentEdit.noSpace
                    childBean.create_id = mine!!.uid!!
                    childBean.lv = mine!!.is_admin
                }

            }
            workContentEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    workContentEdit.setSelection(workContentEdit.text.length)
                    workContentEdit.addTextChangedListener(contentWatcher)
                } else {
                    workContentEdit.removeTextChangedListener(contentWatcher)
                }
            }
            addressLayout.setOnClickListener {
                addressEdit.requestFocus()
            }
            val addressWatcher: TextWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    childBean.place = addressEdit.noSpace
                    childBean.create_id = mine!!.uid!!
                    childBean.lv = mine!!.is_admin
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
            }
            addressEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    addressEdit.setSelection(addressEdit.text.toString().length)
                    addressEdit.addTextChangedListener(addressWatcher)
                } else {
                    addressEdit.removeTextChangedListener(addressWatcher)
                }
            }
        }
    }
}
