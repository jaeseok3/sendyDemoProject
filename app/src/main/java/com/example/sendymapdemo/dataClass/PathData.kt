package com.example.sendymapdemo.dataClass

import com.google.gson.annotations.SerializedName

data class PathData(
        @SerializedName("code")
        val code: Int,
        @SerializedName("currentDateTime")
        val currentDateTime: String,
        @SerializedName("message")
        val message: String,
        @SerializedName("route")
        val route: Route
){
    data class Route(
            val traoptimal: List<Traoptimal>,
            val trafast: List<Trafast>,
            val traavoidcaronly: List<Traavoidcaronly>
    )
    data class Traoptimal(
            val guide: List<Guide>,
            val path: List<Any>,
            val section: List<Section>,
            val summary: Summary
    )
    data class Trafast(
            val guide: List<Guide>,
            val path: List<Any>,
            val section: List<Section>,
            val summary: Summary
    )
    data class Traavoidcaronly(
            val guide: List<Guide>,
            val path: List<Any>,
            val section: List<Section>,
            val summary: Summary
    )
    data class Guide(
            val distance: Int,
            val duration: Int,
            val instructions: String,
            val pointIndex: Int,
            val type: Int
    )
    data class Section(
            val congestion: Int,
            val distance: Int,
            val name: String,
            val pointCount: Int,
            val pointIndex: Int,
            val speed: Int
    )
    data class Summary(
            val bbox: List<Any>,
            val distance: Int,
            val duration: Int,
            val fuelPrice: Int,
            val goal: Goal,
            val start: Start,
            val taxiFare: Int,
            val tollFare: Int,
            val waypoints: List<Waypoint>
    )
    data class Goal(
            val dir: Int,
            val distance: Int,
            val duration: Int,
            val location: List<Double>,
            val pointIndex: Int
    )
    data class Start(
            val location: List<Double>
    )
    data class Waypoint(
            val dir: Int,
            val distance: Int,
            val duration: Int,
            val location: List<Double>,
            val pointIndex: Int
    )
}