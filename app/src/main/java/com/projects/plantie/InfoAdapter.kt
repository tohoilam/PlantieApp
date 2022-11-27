package com.projects.plantie

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import org.tensorflow.lite.examples.plantie.R

class InfoAdapter() : PagerAdapter() {

    private lateinit var infoModels: List<InfoModel>
    private lateinit var layoutInflater: LayoutInflater
    private lateinit var context: Context


    constructor(models: List<InfoModel>, context: Context) : this() {
        this.infoModels = models
        this.context = context
    }

    override fun getCount(): Int {
        return infoModels.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        TODO("Not yet implemented")
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = LayoutInflater.from(context)
        var view: View = layoutInflater.inflate(R.layout.info_item, container)

        var text: TextView = view.findViewById(R.id.text_trial)
        text.text = "1111111111"

        container.addView(view)


        return super.instantiateItem(container, position)
    }
}