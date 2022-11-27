package com.projects.plantie

data class InfoModel(
    val flower_info: List<FlowerInfo>
)

data class FlowerInfo(
    val name: String,
    val scientific_name: String,
    val description: String,
    val max_height: String,
    val max_spread: String,
    val time_to_max_height: String,
    val conditions: List<String>,
    val moisture: String,
    val ph: String
)