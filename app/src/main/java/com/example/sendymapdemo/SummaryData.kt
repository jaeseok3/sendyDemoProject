package com.example.sendymapdemo

data class SummaryData(
        val startLatLng: String,
        val wayPointLatLng: String,
        val goalLatLng: String,
        val responseData: PathData
)