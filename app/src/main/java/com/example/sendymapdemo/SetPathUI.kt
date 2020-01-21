package com.example.sendymapdemo

import android.graphics.Color
import com.example.sendymapdemo.dataClass.PathData
import com.example.sendymapdemo.model.repository.MapsRepository
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.OverlayImage

val latlngList = ArrayList<LatLng>()

class SetPathUI(data: PathData, private val nMap: MapsRepository) {
    private val pathData: PathData = data

    fun setUIPath(){
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

        nMap.pathOverlay.coords = latlngList
        nMap.pathOverlay.width = 30
        nMap.pathOverlay.color = Color.BLUE
        nMap.pathOverlay.patternImage = OverlayImage.fromResource(R.drawable.path_pattern)
        nMap.pathOverlay.patternInterval = 50
        nMap.pathOverlay.passedColor = Color.GRAY
        nMap.markerStartPoint.position = LatLng(startLat, startLng)
        nMap.markerWayPoint.position = LatLng(wayPointLat, wayPointLng)
        nMap.markerGoalPoint.position = LatLng(goalLat, goalLng)
        nMap.markerStartPoint.iconTintColor = Color.BLUE
        nMap.markerWayPoint.iconTintColor = Color.GREEN
        nMap.markerGoalPoint.iconTintColor = Color.RED
        nMap.markerStartPoint.map = nMap.nMap!!
        nMap.markerWayPoint.map = nMap.nMap!!
        nMap.markerGoalPoint.map = nMap.nMap!!
        nMap.pathOverlay.map = nMap.nMap!!
    }

    private fun parsingPath(rawPathData: String): LatLng{
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
    }
}