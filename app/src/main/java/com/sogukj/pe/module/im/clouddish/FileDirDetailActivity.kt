package com.sogukj.pe.module.im.clouddish

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

/**
 * Created by CH-ZH on 2018/10/25.
 */
class FileDirDetailActivity : ToolbarActivity() {
    private var invokeType = 1
    private var path = ""
    private var title = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_dir)
        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)
        setTitle(title)
        invokeType = intent.getIntExtra(Extras.TYPE,1)
        path = intent.getStringExtra(Extras.TYPE1)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_detail,CloudMineFileFragment.newInstance(1,invokeType,path,"/${title}"),CloudMineFileFragment.TAG).commit()
    }
}