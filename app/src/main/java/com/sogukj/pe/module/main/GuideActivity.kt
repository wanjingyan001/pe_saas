package com.sogukj.pe.module.main

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.edit
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.module.register.LoginActivity
import kotlinx.android.synthetic.main.activity_guide.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.onPageChangeListener

class GuideActivity : BaseActivity() {
    companion object {
        private val images = arrayOf(R.mipmap.img_guide_page1, R.mipmap.img_guide_page2)
    }

    private lateinit var pageAdapter: GuidePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        StatusBarUtil.setTransparent(this)
        pageAdapter = GuidePagerAdapter(images)
        guideViewPager.adapter = pageAdapter
        guideViewPager.onPageChangeListener {
            onPageSelected { position ->
                pageAdapter.currentPosition = position
            }
        }
        guideViewPager.toNextPage = {
            sp.edit { putBoolean(Extras.isFirstEnter, false) }
            startActivity<LoginActivity>()
            finish()
        }
    }


    inner class GuidePagerAdapter(val imgs: Array<Int>) : PagerAdapter() {
        var currentPosition: Int = 0
        override fun isViewFromObject(view: View, `object`: Any): Boolean
                = view == `object`

        override fun getCount(): Int = imgs.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = ImageView(this@GuideActivity)
            imageView.imageResource = imgs[position]
            container.addView(imageView)
            return imageView
        }
    }
}
