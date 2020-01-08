package com.example.sendymapdemo

data class PathData(
    val code: Int,
    val currentDateTime: String,
    val message: String,
    val route: Route
)

data class Route(
    val traoptimal: List<Traoptimal>
)

data class Traoptimal(
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
    val tollFare: Int
)

data class Goal(
    val dir: Int,
    val location: List<Double>
)

data class Start(
    val location: List<Double>
)
//data class PathData(
//    val code: Int,
//    val currentDateTime: String,
//    val message: String,
//    val route: Route
//)
//
//data class Route(
//    val trafast: List<Trafast>
//)
//
//data class Trafast(
//    val guide: List<Guide>,
//    val path: List<Any>,
//    val section: List<Section>,
//    val summary: Summary
//)
//
//data class Guide(
//    val distance: Int,
//    val duration: Int,
//    val instructions: String,
//    val pointIndex: Int,
//    val type: Int
//)
//
//data class Section(
//    val congestion: Int,
//    val distance: Int,
//    val name: String,
//    val pointCount: Int,
//    val pointIndex: Int,
//    val speed: Int
//)
//
//data class Summary(
//    val bbox: List<Any>,
//    val distance: Int,
//    val duration: Int,
//    val fuelPrice: Int,
//    val goal: Goal,
//    val start: Start,
//    val taxiFare: Int,
//    val tollFare: Int
//)
//
//data class Goal(
//    val dir: Int,
//    val location: List<Double>
//)
//
//data class Start(
//    val location: List<Double>
//)