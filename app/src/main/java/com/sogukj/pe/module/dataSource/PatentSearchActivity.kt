package com.sogukj.pe.module.dataSource

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.dataSource.lawcase.LawSearchResultActivity
import kotlinx.android.synthetic.main.activity_patent_search.*
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.startActivity

class PatentSearchActivity : ToolbarActivity() {
    private var type = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patent_search)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        Utils.setWindowStatusBarColor(this,R.color.white)
        setBack(true)
        type = intent.getIntExtra(Extras.DATA,0)
        when(type){
            0 -> {
                title = "专利数据大全"
                searchEdt.hint = "请输入专利关键词/专利号"
                searchBanner.setImageResource(R.mipmap.bg_patent_search)
            }
            1 -> {
                title = "法律案例大全"
                searchEdt.hint = "请输入关键词"
                searchBanner.setImageResource(R.mipmap.bg_law_search)
            }
        }

        searchEdt.textChangedListener {
            onTextChanged { charSequence, start, before, count ->
                clear.setVisible( searchEdt.textStr.isNotEmpty())
            }
        }
        clear.clickWithTrigger {
            searchEdt.setText("")
        }
        search.clickWithTrigger {
            Utils.toggleSoftInput(this,searchEdt)
            when(type){
                0 -> {
                    searchEdt.textStr.isNotEmpty().yes {
                        startActivity<PatentListActivity>(Extras.CODE to searchEdt.textStr)
                    }.otherWise {
                        showCommonToast("请输入查询内容")
                    }
                }
                1 -> {
                    startActivity<LawSearchResultActivity>(Extras.DATA to searchEdt.textStr)
                }
            }
//            finish()
        }
    }
}
