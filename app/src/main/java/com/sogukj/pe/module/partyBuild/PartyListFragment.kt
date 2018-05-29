package com.sogukj.pe.module.partyBuild


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ArticleBean
import com.sogukj.pe.bean.FileBean
import com.sogukj.pe.module.partyBuild.PartyMainActivity.Companion.ARTICLE
import com.sogukj.pe.module.partyBuild.PartyMainActivity.Companion.FILE
import com.sogukj.pe.service.PartyBuildService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_party_list.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.ctx


/**
 * A simple [Fragment] subclass.
 * Use the [PartyListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PartyListFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_party_list

    private var tid: Int? = null
    private var title: String? = null
    private var bannerUrl: String? = null
    private lateinit var articleAdapter: RecyclerAdapter<ArticleBean>
    private lateinit var fileAdapter: RecyclerAdapter<FileBean>
    private lateinit var articleHead: View
    private lateinit var articleFoot: View
    private lateinit var fileHead: View
    private lateinit var fileFoot: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tid = it.getInt(tabId)
            title = it.getString(tabTitle)
            bannerUrl = it.getString(tabUrl)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (bannerUrl != null) {
            party_banner.visibility = View.VISIBLE
            Glide.with(this)
                    .load(bannerUrl)
                    .apply(RequestOptions().placeholder(R.drawable.bg_party_banner).error(R.drawable.bg_party_banner))
                    .into(party_banner)
        } else {
            party_banner.visibility = View.GONE
        }
        articleAdapter = RecyclerAdapter(ctx, { _adapter, parent, _ ->
            ArticleHolder(_adapter.getView(R.layout.item_party_list, parent))
        })
        fileAdapter = RecyclerAdapter(ctx, { _adapter, parent, _ ->
            FileHolder(_adapter.getView(R.layout.item_party_list, parent))
        })

        latestNews.layoutManager = LinearLayoutManager(ctx)
        latestNews.adapter = articleAdapter
        filesList.layoutManager = LinearLayoutManager(ctx)
        filesList.adapter = fileAdapter
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            latestNews.isNestedScrollingEnabled = false
            filesList.isNestedScrollingEnabled = false
        }

        articleHead = LayoutInflater.from(ctx).inflate(R.layout.layout_party_header, latestNews, false)
        fileHead = LayoutInflater.from(ctx).inflate(R.layout.layout_party_header, filesList, false)
        articleHead.find<TextView>(R.id.headTv).text = "最新动态"
        articleHead.find<ImageView>(R.id.headIcon).imageResource = R.drawable.icon_latest_news
        fileHead.find<TextView>(R.id.headTv).text = "相关文件"
        fileHead.find<ImageView>(R.id.headIcon).imageResource = R.drawable.icon_files
        latestNews.addHeaderView(articleHead)
        filesList.addHeaderView(fileHead)

        articleFoot = LayoutInflater.from(ctx).inflate(R.layout.layout_party_foot, latestNews, false)
        latestNews.addFooterView(articleFoot)
        fileFoot = LayoutInflater.from(ctx).inflate(R.layout.layout_party_foot, filesList, false)
        filesList.addFooterView(fileFoot)
        doRequest()


        articleFoot.setOnClickListener {
            PartyMoreActivity.start(ctx, tid!!, ARTICLE, title!!)
        }
        fileFoot.setOnClickListener {
            PartyMoreActivity.start(ctx, tid!!, FILE, title!!)
        }
    }


    fun doRequest() {
        tid?.let {
            SoguApi.getService(baseActivity!!.application, PartyBuildService::class.java)
                    .categoryInfo(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            Log.d("WJY", Gson().toJson(payload.payload))
                            articleAdapter.dataList.clear()
                            fileAdapter.dataList.clear()
                            payload.payload?.let {
                                it.articles?.let { articles ->
                                    if (articles.size > 10) {
                                        val dropLast = articles.dropLast(articles.size - 10)
                                        articleAdapter.dataList.addAll(dropLast)
                                    } else {
                                        articleAdapter.dataList.addAll(articles)
                                        latestNews.removeFooterView(articleFoot)
                                    }
                                }
                                if (articleAdapter.dataList.isEmpty()) {
                                    latestNews.removeHeaderView(articleHead)
                                }
                                it.files?.let { files ->
                                    if (files.size > 10) {
                                        val dropLast = files.dropLast(files.size - 10)
                                        fileAdapter.dataList.addAll(dropLast)
                                    } else {
                                        fileAdapter.dataList.addAll(files)
                                        filesList.removeFooterView(fileFoot)
                                    }
                                }
                                if (fileAdapter.dataList.isEmpty()) {
                                    filesList.removeHeaderView(fileHead)
                                }
                            }
                        }
                    }, { e ->
                        Trace.e(e)
                    }, {
                        latestNews.smoothScrollToPosition(0)
                        latestNews.adapter = articleAdapter
                        filesList.adapter = fileAdapter
                        if (articleAdapter.dataList.isEmpty() && fileAdapter.dataList.isEmpty()) {
                            contentLayout.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        } else {
                            contentLayout.visibility = View.VISIBLE
                            iv_empty.visibility = View.GONE
                        }
                    })
        }
    }


    companion object {
        private val tabId = "tabId"
        private val tabTitle = "title"
        private val tabUrl = "bannerUrl"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param tabId Parameter 1.
         * @param title Parameter 2.
         * @return A new instance of fragment PartyListFragment.
         */
        fun newInstance(tabId: Int, title: String?, url: String?): PartyListFragment {
            val fragment = PartyListFragment()
            val args = Bundle()
            args.putInt(this.tabId, tabId)
            args.putString(tabTitle, title)
            args.putString(tabUrl, url)
            fragment.arguments = args
            return fragment
        }
    }


    inner class ArticleHolder(convertView: View) : RecyclerHolder<ArticleBean>(convertView) {
        val contentTv = convertView.find<TextView>(R.id.contentTv)
        val timeTv = convertView.find<TextView>(R.id.timeTv)
        val lineLayout = convertView.find<LinearLayout>(R.id.lineLayout)
        override fun setData(view: View, data: ArticleBean, position: Int) {
            contentTv.text = data.title
            timeTv.text = data.time
            if (position != 0 && (position + 1) % 4 == 0 && articleAdapter.dataList.size - 1 != position) {
                lineLayout.visibility = View.VISIBLE
            } else {
                lineLayout.visibility = View.GONE
            }
            view.setOnClickListener {
                PartyDetailActivity.start(ctx, data.id!!, title!!)
            }
        }
    }

    inner class FileHolder(convertView: View) : RecyclerHolder<FileBean>(convertView) {
        val contentTv = convertView.find<TextView>(R.id.contentTv)
        val timeTv = convertView.find<TextView>(R.id.timeTv)
        val lineLayout = convertView.find<LinearLayout>(R.id.lineLayout)
        override fun setData(view: View, data: FileBean, position: Int) {
            contentTv.text = data.file_name
            timeTv.text = data.time
            if (position != 0 && (position + 1) % 4 == 0 && fileAdapter.dataList.size - 1 != position) {
                lineLayout.visibility = View.VISIBLE
            } else {
                lineLayout.visibility = View.GONE
            }
            view.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data.url)
                ctx.startActivity(intent)
            }
        }
    }
}
