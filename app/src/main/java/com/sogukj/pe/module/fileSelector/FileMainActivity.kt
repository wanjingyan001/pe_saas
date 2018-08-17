package com.sogukj.pe.module.fileSelector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.KeyEvent
import android.widget.AdapterView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.partyBuild.PartyUploadActivity
import com.sogukj.pe.module.user.UserFragment
import com.sogukj.pe.peUtils.FileUtil
import kotlinx.android.synthetic.main.activity_file_main.*
import org.jetbrains.anko.info
import java.io.File
import kotlin.properties.Delegates

class FileMainActivity : BaseActivity(), ViewPager.OnPageChangeListener {
    val selectedFile = ArrayList<File>()
    var maxSize: Int by Delegates.notNull()//最大选择数量
    var isReplace: Boolean = false//文件选择替换功能(单选)
    var isForResult: Boolean = false////是否需要返回选择的文件
    private val comDocFragment by lazy { CommonDocumentsFragment.newInstance() }
    private val allFileFragment by lazy { AllFileFragment() }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        info { "文件管理器时间1:${System.currentTimeMillis() - UserFragment.startTime}" }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_main)
        Utils.setWindowStatusBarColor(this, R.color.white)

        maxSize = intent.getIntExtra(Extras.DATA, 9)
        isReplace = intent.getBooleanExtra(Extras.FLAG, false)
        isForResult = intent.getBooleanExtra(Extras.TYPE, false)
        if (!isForResult) {
            help.text = "删除"
        } else {
            help.text = "帮助"
        }
        back.setOnClickListener {
            finish()
        }
        val spinner = SpinnerWindow(this, AdapterView.OnItemClickListener { parent, view, position, id ->
            file_pager.currentItem = position
            directory_type.text = parent.adapter.getItem(position).toString()
        })
        directory_type.setOnClickListener {
            if (!spinner.isShowing) {
                spinner.showAsDropDown(directory_type)
            } else {
                spinner.dismiss()
            }
        }


        send_selected_files.setOnClickListener {
            if (isForResult) {
                val intent = Intent()
                val paths = ArrayList<String>()
                selectedFile.forEach {
                    paths.add(it.path)
                }
                intent.putExtra(Extras.LIST, paths)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                deleteFiles()
            }
        }
        help.setOnClickListener {
            if (isForResult) {
                MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .content(R.string.file_help)
                        .positiveText("确定")
                        .onPositive { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
            } else {
                deleteFiles()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val fragments = listOf(comDocFragment)
        file_pager.adapter = FilePageAdapter(supportFragmentManager, fragments)
        file_pager.addOnPageChangeListener(this)
    }

    fun showSelectedInfo() {
        var size: Long = 0
        selectedFile.forEach {
            size += it.length()
        }
        selected_files_size.text = "已选择 : ${FileUtil.formatFileSize(size, FileUtil.SizeUnit.Auto)}"
        send_selected_files.isEnabled = selectedFile.size > 0
        send_selected_files.text = "选择(${selectedFile.size}/$maxSize)"
    }

    fun sendChangeFile(file: File) {
        val requestCode = intent.getIntExtra(Extras.ID, -1)
        val intent = Intent()
        if (requestCode == PartyUploadActivity.SELECTFILE) {
            intent.putExtra(Extras.DATA, file)
        } else {
            intent.putExtra(Extras.DATA, file.path)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun deleteFiles() {
        if (selectedFile.isNotEmpty()) {
            MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .title("警告")
                    .content("是否确认删除选中的文件")
                    .positiveText("确定")
                    .negativeText("取消")
                    .onPositive { dialog, _ ->
                        dialog.dismiss()
                        if (comDocFragment.isVisible) {
                            val item = comDocFragment.pagerAdapter.getCurrentItem()
                            item.deleteFile(selectedFile)
                        }
                        if (allFileFragment.isVisible) {
                            selectedFile.forEach {
                                if (it.exists()) {
                                    //通知列表刷新
                                    it.delete()
                                }
                            }
                            val fragment = allFileFragment.childFragmentManager.findFragmentById(R.id.contentLayout) as StorageFileFragment
                            fragment.changeData()
                        }
                        selectedFile.clear()
                        showSelectedInfo()
                    }
                    .onNegative { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        supportFragmentManager.fragments.forEach {
            if (it is AllFileFragment) {
                return it.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        when (position) {
            0 -> directory_type.text = "常用应用"
            1 -> directory_type.text = "内部存储"
        }
    }

    inner class FilePageAdapter(fm: FragmentManager, val fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                val intent = Intent()
                val paths = data.getStringArrayListExtra(Extras.LIST)
                intent.putExtra(Extras.LIST, paths)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                val file = data.getSerializableExtra(Extras.DATA) as File
                sendChangeFile(file)
            }
        }
    }

    companion object {
        fun start(context: Activity, maxSize: Int? = 9, isReplace: Boolean? = false, requestCode: Int) {
            val intent = Intent(context, FileMainActivity::class.java)
            intent.putExtra(Extras.DATA, maxSize)
            intent.putExtra(Extras.FLAG, isReplace)
            intent.putExtra(Extras.ID, requestCode)
            intent.putExtra(Extras.TYPE, true)
            context.startActivityForResult(intent, requestCode)
        }

        fun start(context: Context) {
            val intent = Intent(context, FileMainActivity::class.java)
            intent.putExtra(Extras.DATA, 9)
            intent.putExtra(Extras.FLAG, false)
            intent.putExtra(Extras.TYPE, false)
            context.startActivity(intent)
        }
    }
}
