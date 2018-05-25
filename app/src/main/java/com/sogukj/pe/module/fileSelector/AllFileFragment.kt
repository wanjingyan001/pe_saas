package com.sogukj.pe.module.fileSelector


import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nbsp.materialfilepicker.utils.FileUtils
import com.sogukj.pe.R

import org.jetbrains.anko.support.v4.toast
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
class AllFileFragment : Fragment(), StorageFileFragment.FileClickListener {
    private val startPath = Environment.getExternalStorageDirectory().absolutePath
    private var mCurrentPath = startPath
    lateinit var fileActivity: FileMainActivity
    override fun onFileClicked(clickedFile: File) {
        if (clickedFile.isDirectory) {
            mCurrentPath = clickedFile.path
            if (mCurrentPath == "/storage/emulated") {
                mCurrentPath = Environment.getExternalStorageDirectory().absolutePath
            }
            val fragment = StorageFileFragment.newInstance(clickedFile.path)
            fragment.setListener(this)
            childFragmentManager.beginTransaction()
                    .replace(R.id.contentLayout, fragment)
                    .addToBackStack(null)
                    .commit()
        } else {
            if (!fileActivity.isReplace){
                //添加文件
                if (fileActivity.selectedFile.contains(clickedFile)) {
                    fileActivity.selectedFile.remove(clickedFile)
                } else {
                    if (fileActivity.selectedFile.size < fileActivity.maxSize) {
                        fileActivity.selectedFile.add(clickedFile)
                    } else {
                        toast("最多只能选择${fileActivity.maxSize}个")
                    }
                }
                fileActivity.showSelectedInfo()
            }else{
                fileActivity.sendChangeFile(clickedFile)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        fileActivity = activity as FileMainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_all_file, container, false)
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return if (mCurrentPath != startPath) {
                childFragmentManager.popBackStack()
                mCurrentPath = FileUtils.cutLastSegmentOfPath(mCurrentPath)
                true
            } else {
                activity?.finish()
                true
            }
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragment = StorageFileFragment.newInstance(startPath)
        fragment.setListener(this)
        childFragmentManager.beginTransaction()
                .replace(R.id.contentLayout, fragment,"All")
                .addToBackStack(null)
                .commit()
    }
}
