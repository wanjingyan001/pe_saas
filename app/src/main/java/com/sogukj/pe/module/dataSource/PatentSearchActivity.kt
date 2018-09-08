package com.sogukj.pe.module.dataSource

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
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
            startActivity<PatentDataActivity>(Extras.DATA to searchEdt.textStr)
            when(type){
                0 -> {
                    startActivity<PatentDataActivity>(Extras.DATA to searchEdt.textStr)
                }
                1 -> {

                }
            }
            finish()
        }
    }
}