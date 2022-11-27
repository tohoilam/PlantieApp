package com.projects.plantie

data class CardModel(
    val image: Int,
    val name: String,
    val date: String,
    val imagePath: String? = "",
    val lat: Double = 22.3193,
    val long: Double = 114.1694
)