package com.sogukj.pe.module.fileSelector

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.base.BasePageFragment
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.user.UserFragment
import kotlinx.android.synthetic.main.fragment_common_documents.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onPageChangeListener

class CommonDocumentsFragment : BasePageFragment() {
    lateinit var pagerAdapter: DocPageAdapter
    val titles = listOf("本应用", "微信", "QQ", "钉钉", "全部")
    val peFragment by lazy { DocumentsFragment.newInstance(DocumentsFragment.PE_LOCAL) }
    val wxFragment by lazy { DocumentsFragment.newInstance(DocumentsFragment.WX_DOC) }
    val qqFragment by lazy { DocumentsFragment.newInstance(DocumentsFragment.QQ_DOC) }
    val dtFragment by lazy { DocumentsFragment.newInstance(DocumentsFragment.DING_TALK) }
    val allFragment by lazy { DocumentsFragment.newInstance(DocumentsFragment.ALL_DOC) }
    private lateinit var fragments: List<Fragment>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        AnkoLogger("WJY").info { "文件管理器时间2.1:${System.currentTimeMillis() - UserFragment.startTime}" }
        return inflater.inflate(R.layout.fragment_common_documents, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnkoLogger("WJY").info { "文件管理器时间2.2:${System.currentTimeMillis() - UserFragment.startTime}" }
        fragments = listOf<Fragment>(peFragment, wxFragment, qqFragment, dtFragment, allFragment)
        pagerAdapter = DocPageAdapter(childFragmentManager, fragments)
        documentList.adapter = pagerAdapter
        tab.setupWithViewPager(documentList)
        documentList.onPageChangeListener {
            onPageSelected {
                search_edt.setText("")
                search_edt.clearFocus()
                Utils.closeInput(ctx, search_edt)
            }
        }
        doSearch()
    }


    private fun doSearch() {
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus || search_edt.textStr.isNotEmpty()) {
                search_hint.setVisible(false)
            } else {
                search_hint.setVisible(true)
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val editable = search_edt.textStr
                val str = Utils.stringFilter(editable)
                Utils.closeInput(ctx, search_edt)
                pagerAdapter.getCurrentItem().search(str)
                true
            }
            false
        }
    }

    inner class DocPageAdapter(fm: FragmentManager, val fragments: List<Fragment>) : FragmentStatePagerAdapter(fm) {
        private lateinit var currentFragment: DocumentsFragment
        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            currentFragment = obj as DocumentsFragment
            super.setPrimaryItem(container, position, obj)
        }

        fun getCurrentItem() = currentFragment
    }
}
