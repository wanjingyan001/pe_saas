package com.sogukj.pe.module.register

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.UserBean
import kotlinx.android.synthetic.main.activity_child_department.*
import org.jetbrains.anko.ctx

class ChildDepartmentActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_complete
    private val memberData = ArrayList<UserBean>()
    private val memberAdapter: MemberAdapter by lazy { MemberAdapter(ctx, memberData) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_department)
        title = "添加子部门"
        setBack(true)


        memberList.apply {
            layoutManager = GridLayoutManager(ctx,6)
            adapter = memberAdapter
        }
    }
}
