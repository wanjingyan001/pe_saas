package com.sogukj.pe.module.fileSelector

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nbsp.materialfilepicker.utils.FileTypeUtils
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.FileUtil
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.io.File

/**
 * Created by admin on 2018/2/27.
 */
class DirectoryAdapter(val context: Context, val files: MutableList<File>,val activity: FileMainActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    lateinit var mOnItemClickListener: OnItemClickListener

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DirectoryViewHolder) {
            holder.setData(holder.itemView, files[position], position)
        } else if (holder is FileViewHolder) {
            holder.setData(holder.itemView, files[position], position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                DirectoryViewHolder(LayoutInflater.from(context).inflate(R.layout.item_directory, parent,false))
            }
            else -> {
                FileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_document_list, parent,false))
            }
        }
    }

    override fun getItemCount(): Int = files.size

    override fun getItemViewType(position: Int): Int {
        return if (files[position].isDirectory) 0 else 1
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }

    fun getModel(index: Int): File {
        return files[index]
    }

    fun changeData(newFiles: MutableList<File>){
        files.clear()
        files.addAll(newFiles)
    }

    inner class DirectoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.find<TextView>(R.id.item_file_title)
        fun setData(view: View, data: File, position: Int) {
            title.text = data.name
            view.setOnClickListener {
                mOnItemClickListener.onItemClick(view,position)
            }
        }
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val slector = itemView.find<ImageView>(R.id.selectIcon)
        val icon = itemView.find<ImageView>(R.id.file_icon)
        val name = itemView.find<TextView>(R.id.file_name)
        val info = itemView.find<TextView>(R.id.file_info)
        fun setData(view: View, data: File, position: Int) {
            slector.isSelected = activity.selectedFile.contains(data)
            if (FileUtil.getFileType(data.absolutePath) != null) {
                Glide.with(context)
                        .load(data.absoluteFile)
                        .apply(RequestOptions().error(R.drawable.icon_pic))
                        .into(icon)
            } else {
                icon.imageResource = FileTypeUtils.getFileType(data).icon
            }
            name.text = data.name
            val builder = StringBuilder()
            val time = Utils.getTime(data.lastModified(), "yyyy/MM/dd HH:mm")
            builder.append(time.substring(2,time.length) + "  ")
            builder.append(FileUtil.formatFileSize(data.length(), FileUtil.SizeUnit.Auto))
            info.text = builder.toString()
            view.setOnClickListener {
                if (activity.selectedFile.contains(data)) {
                    slector.isSelected = false
                } else {
                    if (activity.selectedFile.size < activity.maxSize) {
                        slector.isSelected = true
                    }
                }
                mOnItemClickListener.onItemClick(view,position)
            }
        }
    }
}