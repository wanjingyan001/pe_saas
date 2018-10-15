package com.sogukj.pe.module.approve.baseView.controlView

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.EasyPhotos
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_project.view.*
import kotlinx.android.synthetic.main.layout_control_phone_selection.view.*
import kotlinx.android.synthetic.main.item_control_phone_list.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.io.File
import kotlin.properties.Delegates

/**
 * 图片选择
 * Created by admin on 2018/9/27.
 */
class PhoneSelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {

    private val PHONE_TYPE = 1
    private val ADD_TYPE = 2
    private val Max_Phone = 9
    private lateinit var phoneAdapter: PhoneAdapter
    private val selectedPhones by Delegates.observable(arrayListOf<String>(), { _, _, newValue ->
        controlBean.value?.let {
            it.clear()
            it.addAll(newValue)
        }
    })

    override fun getContentResId(): Int = R.layout.layout_control_phone_selection

    override fun bindContentView() {
        hasInit.yes {
            inflate.phoneSelectionTitle.text = controlBean.name
            controlBean.value?.let {
                it.isNotEmpty().yes {
                    selectedPhones.addAll(it as List<String>)
                }
            }
            phoneAdapter = PhoneAdapter(activity, selectedPhones)
            phoneAdapter.onItemClick = { _, position ->
                controlBean.value!!.removeAt(position)
                phoneAdapter.notifyItemRemoved(position)
            }
            phoneAdapter.onPhoneAdd = {
                EasyPhotos.createAlbum(activity, true, GlideEngine.getInstance())
                        .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                        .setPuzzleMenu(false)
                        .setCount(Max_Phone)
                        .setSelectedPhotoPaths(selectedPhones)
                        .setOriginalMenu(true, true, null)
                        .start(Extras.REQUESTCODE)
            }
            inflate.phoneList.apply {
                layoutManager = GridLayoutManager(activity, 3)
                adapter = phoneAdapter
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Extras.REQUESTCODE && resultCode == Activity.RESULT_OK && data != null) {
            val phones = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
            selectedPhones.clear()
            selectedPhones.addAll(phones)
            phoneAdapter.notifyDataSetChanged()
            phones.map { File(it) }.forEach {
                uploadPhones(it)
            }
        }
    }


    private fun uploadPhones(file:File){
        SoguApi.getService(activity.application,ApproveService::class.java)
                .uploadFiles(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                        .build())
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                controlBean.value?.add(it)
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

    inner class PhoneAdapter(val ctx: Context, val data: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((v: View, position: Int) -> Unit)? = null
        var onPhoneAdd: (() -> Unit)? = null
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is PhoneHolder -> holder.setData(position, data[position])
                is AddHolder -> holder.setData()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                PHONE_TYPE -> PhoneHolder(LayoutInflater.from(ctx).inflate(R.layout.item_control_phone_list, parent, false))
                else -> AddHolder(LayoutInflater.from(ctx).inflate(R.layout.item_control_phone_list, parent, false))
            }
        }

        override fun getItemCount(): Int {
            return if (data.size > 8)
                9
            else
                data.size + 1
        }


        override fun getItemViewType(position: Int): Int {
            return if (position + 1 <= data.size) {
                PHONE_TYPE
            } else {
                ADD_TYPE
            }
        }


        inner class PhoneHolder(val item: View) : RecyclerView.ViewHolder(item) {
            val img = item.find<ImageView>(R.id.phoneImage)
            val remove = item.find<ImageView>(R.id.removeImage)
            fun setData(position: Int, data: String) {
                Glide.with(ctx).load(data).thumbnail(0.1f).into(img)
                item.clickWithTrigger {
                    if (null != onItemClick)
                        onItemClick!!(item, position)
                }
            }
        }

        inner class AddHolder(val item: View) : RecyclerView.ViewHolder(item) {
            val img = item.find<ImageView>(R.id.phoneImage)
            val remove = item.find<ImageView>(R.id.removeImage)
            fun setData() {
                img.imageResource = R.drawable.ic_upload_img_add
                remove.setVisible(false)
                item.clickWithTrigger {
                    if (null != onPhoneAdd) {
                        onPhoneAdd!!.invoke()
                    }
                }
            }
        }
    }
}