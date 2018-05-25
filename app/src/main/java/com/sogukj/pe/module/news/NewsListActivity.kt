package com.sogukj.pe.module.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.main.MainMsgFragment

class NewsListActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_list)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, MainMsgFragment.newInstance())
                .commit()
    }


    override val menuId: Int
        get() = R.menu.menu_to_address

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.to_address -> {
                ContactsActivity.Companion.start(this,null,isCreateTeam = true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, NewsListActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
