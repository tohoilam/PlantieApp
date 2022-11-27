package com.projects.plantie

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.plantie.R
import java.io.File

class BrowseAdapter(private val cardList: List<CardModel>) : RecyclerView.Adapter<BrowseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = cardList[position]

        if (currentItem.imagePath != "") {
            var imgFile =  File(currentItem.imagePath)

            if(imgFile.exists()){
                var myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                holder.flowerImage.setImageBitmap(myBitmap)
            }
        }
        else {
            holder.flowerImage.setImageResource(currentItem.image)
        }

        holder.flowerName.text = currentItem.name.replace("_", " ").replaceFirstChar(Char::titlecase)
        holder.captureTime.text = currentItem.date

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, InfoActivity::class.java)
            intent.putExtra("flowerName", currentItem.name)
            if (currentItem.imagePath != "") {
                intent.putExtra("localImage", currentItem.imagePath)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowerImage: ImageView = itemView.findViewById(R.id.flower_image)
        val flowerName: TextView = itemView.findViewById(R.id.flower_name)
        val captureTime: TextView = itemView.findViewById(R.id.capture_time)
    }
}