package com.projects.plantie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet.Layout
import org.tensorflow.lite.examples.plantie.R

class BrowseActivity : AppCompatActivity() {
    private var testAddCardButton: Button? = null
    private var testDelCardButton: Button? = null
    private var gridLayout: GridLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        gridLayout = findViewById(R.id.browse_box)

        testAddCardButton = findViewById<Button>(R.id.test_add_card)

        testAddCardButton!!.setOnClickListener{ addCard("Red Rose", "03 Jan 2022, 03:45") }
    }

    private fun addCard(plantName: String, dateTime: String) {
        val view: View = layoutInflater.inflate(R.layout.card, null)
        val view2: View = layoutInflater.inflate(R.layout.card, null)

        val name: TextView = view.findViewById(R.id.flower_name)
        val date: TextView = view.findViewById(R.id.capture_time)
        val deleteCardButton: Button = view.findViewById(R.id.delete_card)

        name.text = plantName
        date.text = dateTime
        deleteCardButton!!.setOnClickListener{ view2: View ->
            gridLayout!!.removeView(view2)
            Toast.makeText(this@BrowseActivity, "Unsaved Plant", Toast.LENGTH_LONG).show()
        }

        gridLayout!!.addView(view)
    }
}