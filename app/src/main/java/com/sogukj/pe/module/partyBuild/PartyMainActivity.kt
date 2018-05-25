package com.sogukj.pe.module.partyBuild

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.widget.TextView
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.PartyTabBean
import com.sogukj.pe.service.PartyBuildService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_party_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find

class PartyMainActivity : BaseActivity() {
    lateinit var adapter: PartyAdapter

    companion object {
        val ARTICLE = 1
        val FILE = 2
        val TABS: String = "tabsKey"
        fun start(context: Context) {
            val intent = Intent(context, PartyMainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_main)
        Utils.setWindowStatusBarColor(this, R.color.party_toolbar_red)
        toolbar.background = resources.getDrawable(R.color.party_toolbar_red)
        toolbar_title.text = "党建专栏"
        categoryList()
        addTv.setOnClickListener {
            PartyUploadActivity.start(this)
        }
        back.setOnClickListener {
            finish()
        }
    }


    private fun initPager(tabs: List<PartyTabBean>) {
        adapter = PartyAdapter(supportFragmentManager, tabs)
        contentPager.adapter = adapter
        tabLayout.setViewPager(contentPager)
        tabLayout.setTabViewFactory { parent, _ ->
            parent.removeAllViews()
            for (i in 0 until tabs.size){
                val view = LayoutInflater.from(this).inflate(R.layout.item_party_indicator, parent,false)
                view.find<TextView>(R.id.indicatorTv).text = tabs[i].classname
                parent.addView(view)
            }
        }
    }

    private fun categoryList() {
        SoguApi.getService(application,PartyBuildService::class.java)
                .categoryList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            XmlDb.open(ctx).set(TABS, Gson().toJson(it))
                            initPager(it)
                        }
                    }
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE) {
            supportFragmentManager.fragments.forEach {
                if (it.userVisibleHint && it is PartyListFragment) {
                    it.doRequest()
                }
            }
        }
    }


    inner class PartyAdapter(fm: FragmentManager?, val tabs: List<PartyTabBean>) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            val bean = tabs[position]
            return PartyListFragment.newInstance(bean.id, bean.classname, bean.picture)
        }

        override fun getCount(): Int = tabs.size

        override fun getPageTitle(position: Int): CharSequence {
            return tabs[position].classname
        }
    }
}
