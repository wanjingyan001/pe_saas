package com.sogukj.pe.module.dataSource

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_patent_search.*
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.startActivity

class PatentSearchActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patent_search)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        Utils.setWindowStatusBarColor(this,R.color.white)
        setBack(true)
        title = "专利数据大全"
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
            finish()
        }
    }

}
