package com.example.sendymapdemo

import android.graphics.Color
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay

val latlngList = ArrayList<LatLng>()

class SetPathUI(data: PathData, naverMap: NaverMap) {
    private val pathData: PathData = data
    private val nMap: NaverMap = naverMap

    fun setUIPath(){
        val pathOverlay = PathOverlay()
        val pathArr = pathData.route.traoptimal[0].path
        val startLng = pathData.route.traoptimal[0].summary.start.location[0]
        val startLat = pathData.route.traoptimal[0].summary.start.location[1]
        val wayPointLng = pathData.route.traoptimal[0].summary.waypoints[0].location[0]
        val wayPointLat = pathData.route.traoptimal[0].summary.waypoints[0].location[1]
        val goalLng = pathData.route.traoptimal[0].summary.goal.location[0]
        val goalLat = pathData.route.traoptimal[0].summary.goal.location[1]

        for(i in pathArr.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngList.add(pathLatLng)
        }

        pathOverlay.coords = latlngList
        pathOverlay.width = 30
        pathOverlay.color = Color.BLUE
        pathOverlay.patternImage = OverlayImage.fromResource(R.drawable.path_pattern)
        pathOverlay.patternInterval = 50
        pathOverlay.passedColor = Color.GRAY
        markerStartPoint.position = LatLng(startLat, startLng)
        markerWayPoint.position = LatLng(wayPointLat, wayPointLng)
        markerGoalPoint.position = LatLng(goalLat, goalLng)
        markerStartPoint.iconTintColor = Color.BLUE
        markerWayPoint.iconTintColor = Color.GREEN
        markerGoalPoint.iconTintColor = Color.RED
        markerStartPoint.map = nMap
        markerWayPoint.map = nMap
        markerGoalPoint.map = nMap
        pathOverlay.map = nMap
    }

    private fun parsingPath(rawPathData: String): LatLng{
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
    }
}