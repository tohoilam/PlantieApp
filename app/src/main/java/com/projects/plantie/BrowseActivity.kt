package com.projects.plantie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.plantie.R

class BrowseActivity : AppCompatActivity() {
    private var testAddCardButton: Button? = null
    private var gridLayout: GridLayout? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
//        gridLayout = findViewById(R.id.browse_box)

        testAddCardButton = findViewById(R.id.test_add_card)

//        testAddCardButton!!.setOnClickListener{ addCard("Daffodil", "03 Jan 2022, 03:45") }

        val cardList = arrayListOf<CardModel>(CardModel(R.drawable.lilyvalley, "LilyValley", "03 Jan 2022, 03:45"),
                                                CardModel(R.drawable.bluebell, "Bluebell", "03 Jan 2022, 03:45"),
                                                CardModel(R.drawable.cowslip, "Cowslip", "03 Jan 2022, 03:45"))

        Log.d("browse_final", "Adapter Browse")

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = BrowseAdapter(cardList)

    }

    private fun addCard(plantName: String, dateTime: String) {
        val view: View = layoutInflater.inflate(R.layout.card, null)

        val name: TextView = view.findViewById(R.id.flower_name)
        val date: TextView = view.findViewById(R.id.capture_time)

        view!!.setOnClickListener{ toInfoPage(plantName) }

        name.text = plantName
        date.text = dateTime

        gridLayout!!.addView(view)
    }

    private fun toInfoPage(plantName: String) {
        intent = Intent(this, InfoActivity::class.java)
        intent.putExtra("flowerName", plantName);
        startActivity(intent)
    }

    private fun changeImage() {
        var image: ImageView = findViewById(R.id.flower_image)

//        when (flowerName) {
//            "Buttercup" -> {
//                image.setImageResource(R.drawable.buttercup)
//            }
//            "Colts'Foot" -> {
//                image.setImageResource(R.drawable.coltsfoot)
//            }
//            "Daffodil" -> {
//                image.setImageResource(R.drawable.daffodil)
//            }
//            "Daisy" -> {
//                image.setImageResource(R.drawable.daisy)
//            }
//            "Dandelion" -> {
//                image.setImageResource(R.drawable.dandelion)
//            }
//            "Fritillary" -> {
//                image.setImageResource(R.drawable.fritillary)
//            }
//            "Iris" -> {
//                image.setImageResource(R.drawable.iris)
//            }
//            "Pansy" -> {
//                image.setImageResource(R.drawable.pansy)
//            }
//            "Sunflower" -> {
//                image.setImageResource(R.drawable.sunflower)
//            }
//            "Windflower" -> {
//                image.setImageResource(R.drawable.windflower)
//            }
//            "Snowdrop" -> {
//                image.setImageResource(R.drawable.snowdrop)
//            }
//            "LilyValley" -> {
//                image.setImageResource(R.drawable.lilyvalley)
//            }
//            "Bluebell" -> {
//                image.setImageResource(R.drawable.bluebell)
//            }
//            "Crocus" -> {
//                image.setImageResource(R.drawable.crocus)
//            }
//            "Tigerlily" -> {
//                image.setImageResource(R.drawable.tigerlily)
//            }
//            "Tulip" -> {
//                image.setImageResource(R.drawable.tulip)
//            }
//            "Cowslip" -> {
//                image.setImageResource(R.drawable.cowslip)
//            }
//            else -> {
//                //TODO: Toast of error message for invalid flower name
//
//            }
//        }
    }
}