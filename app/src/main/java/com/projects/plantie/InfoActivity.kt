package com.projects.plantie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.tensorflow.lite.examples.plantie.R

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
    }
}