package com.sogukj.pe.module.im.clouddish

import android.os.Bundle
import com.sogukj.pe.Consts
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.BatchRemoveBean
import kotlinx.android.synthetic.main.white_normal_toolbar.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.imageResource

/**
 * Created by CH-ZH on 2018/10/25.
 */
class FileDirDetailActivity : ToolbarActivity() {
    private var invokeType = 1
    private var path = ""
    private var title = ""
    private var dir = ""
    private var isSave = true
    private var realDir = ""
    private var fileName = ""
    private var previousPath = ""
    private var isCopy = false
    private var isRemove = false
    private var batchPath : BatchRemoveBean ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_dir)
        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)
        invokeType = intent.getIntExtra(Extras.TYPE,1)
        path = intent.getStringExtra(Extras.TYPE1)
        dir = intent.getStringExtra(Extras.TYPE2)
        isSave = intent.getBooleanExtra("isSave",true)
        isCopy = intent.getBooleanExtra("isCopy",false)
        isRemove = intent.getBooleanExtra("isRemove",false)
        fileName = intent.getStringExtra("fileName")
        previousPath = intent.getStringExtra("previousPath")
        batchPath = intent.getSerializableExtra("batchPath") as BatchRemoveBean?
        RetrofitUrlManager.getInstance().putDomain("CloudPath", Consts.CLOUD_HOST)
        if (!isSave){
            Utils.setWindowStatusBarColor(this, R.color.white)
            toolbar?.setBackgroundColor(resources.getColor(R.color.white))
            toolbar_back.imageResource = R.drawable.back_chevron
            toolbar_title.setTextColor(resources.getColor(R.color.black_28))
        }
        if (title.isNullOrEmpty()){
            realDir = dir
            if (dir.contains("我的文件")){
                setTitle("我的文件")
            }else if (dir.contains("基金文件")){
                setTitle("基金文件")
            }else if (dir.contains("项目文件")){
                setTitle("项目文件")
            }else if (dir.contains("群组文件")){
                setTitle("群组文件")
            }else{
                setTitle("我的文件")
            }

        }else{
            realDir = dir+"/${title}"
            setTitle(title)
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_detail,CloudMineFileFragment.newInstance(1,invokeType,path,realDir,
                isSave,false,isCopy,fileName,previousPath,batchPath!!,isRemove),CloudMineFileFragment.TAG).commit()
    }
}