package com.sogukj.pe.module.user

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.register.OrganViewModel
import com.sogukj.pe.widgets.CircleImageView
import io.reactivex.Maybe
import kotlinx.android.synthetic.main.activity_admin_transfer.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class AdminTransferActivity : ToolbarActivity() {
    private lateinit var memberAdapter: RecyclerAdapter<UserBean>
    private val model by lazy { ViewModelProviders.of(this).get(OrganViewModel::class.java) }
    var searchKey = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_transfer)
        title = "转让管理员"
        setBack(true)
        memberAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            MemberHolder(_adapter.getView(R.layout.item_admin_transfer, parent))
        }
        memberAdapter.onItemClick = { v, p ->
            search_edt.clearFocus()
            val content = "${memberAdapter.dataList[p].name}将成为管理员，确定后你将立刻失去管理员身份。"
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .title("转让管理员")
                    .content(content)
                    .positiveText("同意")
                    .negativeText("取消")
                    .onPositive { dialog, which ->
                        dialog.dismiss()
                        //todo 调用接口转让管理员.
                    }.show()
        }
        memberList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = memberAdapter
        }
        getMemberList()
        initSearch()
    }

    private fun initSearch() {
        search_edt.filters = Utils.getFilter(this)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                searchWithName()
                true
            } else {
                false
            }
        }
        search_edt.textChangedListener {
            onTextChanged { charSequence, start, before, count ->
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    getMemberList()
                }
            }
        }
    }

    private fun searchWithName() {
        val filter = memberAdapter.dataList.filter { it.name.contains(searchKey) }
        memberAdapter.refreshData(filter)
    }


    private fun getMemberList() = runBlocking {
        val key = sp.getString(Extras.CompanyKey, "")
        model.loadMemberList(application, key).observe(this@AdminTransferActivity, Observer {
            it?.let {
                memberAdapter.refreshData(it)
            }
        })
    }

    inner class MemberHolder(itemView: View) : RecyclerHolder<UserBean>(itemView) {
        val selectIcon = itemView.find<ImageView>(R.id.selectIcon)
        val userImg = itemView.find<CircleImageView>(R.id.userHeadImg)
        val userName = itemView.find<TextView>(R.id.userName)
        val userPosition = itemView.find<TextView>(R.id.userPosition)
        override fun setData(view: View, data: UserBean, position: Int) {
            selectIcon.setVisible(false)
            userPosition.setVisible(true)
            if (data.headImage().isNullOrEmpty()) {
                val ch = data.name.first()
                userImg.setChar(ch)
            } else {
                Glide.with(context)
                        .load(data.headImage())
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                        .into(userImg)
            }
            userName.text = data.name
            userPosition.text = data.depart_name
        }
    }
}
