package com.sogukj.pe.module.im.clouddish

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity

/**
 * Created by CH-ZH on 2018/10/25.
 */
class FileDirDetailActivity : ToolbarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_dir)
        setBack(true)
        setTitle(intent.getStringExtra(Extras.TITLE))

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_detail,CloudMineFileFragment.newInstance(1),CloudMineFileFragment.TAG).commit()
    }
}