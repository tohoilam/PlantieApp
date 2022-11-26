package com.projects.plantie

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.plantie.R

class BrowseAdapter(private val cardList: List<CardModel>) : RecyclerView.Adapter<BrowseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)

//        val holder : ViewHolder = ViewHolder(itemView);
//
//        itemView.setOnClickListener { toInfoPage(cardList, holder.adapterPosition, parent.context) }

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = cardList[position]

        holder.flowerImage.setImageResource(currentItem.image)
        holder.flowerName.text = currentItem.name
        holder.captureTime.text = currentItem.date
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowerImage: ImageView = itemView.findViewById(R.id.flower_image)
        val flowerName: TextView = itemView.findViewById(R.id.flower_name)
        val captureTime: TextView = itemView.findViewById(R.id.capture_time)
    }

//    private fun toInfoPage(flowerName: String) {
//        var intent = Intent(this, InfoActivity::class.java)
//        intent.putExtra("flowerName", flowerName);
//        context.startActivity(intent)
//    }

}