package com.sogukj.pe.module.approve.baseView.controlView

import android.app.Activity
import android.content.Context
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import com.sogukj.pe.module.approve.baseView.viewBean.AttachmentBean
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_control_attach_selection.view.*
import kotlinx.android.synthetic.main.layout_control_attach_selection.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import java.io.File

/**
 * 附件选择
 * Created by admin on 2018/10/8.
 */
class AttachmentSelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    lateinit var attachAdapter: RecyclerAdapter<AttachmentBean>
    override fun getContentResId(): Int = R.layout.layout_control_attach_selection

    override fun bindContentView() {
        hasInit.yes {
            attachAdapter = RecyclerAdapter(context) { _adapter, parent, _ ->
                AttachHolder(_adapter.getView(R.layout.item_control_attach_selection, parent))
            }
            inflate.attachList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = attachAdapter
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
            controlBean.value?.let { values ->
                values.isNotEmpty().yes {
                    try {
                        val beans = mutableListOf<AttachmentBean>()
                        values.forEach { map ->
                            val treeMap = map as LinkedTreeMap<String, Any>
                            beans.add(AttachmentBean(treeMap["name"] as String, treeMap["url"] as String, treeMap["size"] as String))
                        }
                        controlBean.value?.clear()
                        controlBean.value?.addAll(beans)
                        attachAdapter.refreshData(controlBean.value as MutableList<AttachmentBean>)
                    } catch (e: ClassCastException) {
                        e.printStackTrace()
                    }
                }
            }
            inflate.selectLayout.clickWithTrigger {
                (activity as BaseActivity).showProgress("正在读取内存文件")
                AvoidOnResult(activity)
                        .startForResult<FileMainActivity>(Extras.REQUESTCODE, Extras.TYPE to true)
                        .filter { it.resultCode == Activity.RESULT_OK }
                        .flatMap {
                            val paths = it.data.getStringArrayListExtra(Extras.LIST)
                            Observable.fromIterable(paths)
                        }.subscribe { path ->
                    (activity as BaseActivity).hideProgress()
                    uploadPhones(File(path))
                }
            }
        }
    }

    private fun uploadPhones(file: File) {
        SoguApi.getService(activity.application, ApproveService::class.java)
                .uploadFiles(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                        .build())
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("上传成功")
                            payload.payload?.let {
                                controlBean.value?.add(it)
                                attachAdapter.dataList.add(it)
                                attachAdapter.notifyItemInserted(attachAdapter.dataList.size)
                            }
                        }.otherWise {
                            showErrorToast("上传失败")
                        }
                    }
                    onError {
                        showErrorToast("上传失败")
                    }
                }
    }


    inner class AttachHolder(itemView: View) : RecyclerHolder<AttachmentBean>(itemView) {
        override fun setData(view: View, data: AttachmentBean, position: Int) {
            view.attachTitle.text = data.name
            view.attachSize.text = data.size
            view.attachDelete.clickWithTrigger {
                controlBean.value?.let {
                    it.removeAt(position)
                    attachAdapter.dataList.removeAt(position)
                    attachAdapter.notifyItemRemoved(position)
                }
            }
        }
    }

}