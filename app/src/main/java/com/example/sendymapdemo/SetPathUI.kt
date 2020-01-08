package com.example.sendymapdemo

import android.graphics.Color
import android.location.Location
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay

class SetPathUI(data: PathData, naverMap: NaverMap) {
    private val pathData: PathData = data
    private val nMap: NaverMap = naverMap

    fun setUIPathGoal(): LatLng {
        val latlngListGoal = ArrayList<LatLng>()
        val pathArr = pathData.route.traoptimal[0].path
        val goalLng = pathData.route.traoptimal[0].summary.goal.location[0]
        val goalLat = pathData.route.traoptimal[0].summary.goal.location[1]

        for(i in pathArr.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngListGoal.add(pathLatLng)
        }

        pathOverlayGoal.coords = latlngListGoal
        pathOverlayGoal.width = 30
        pathOverlayGoal.color = Color.RED
        pathOverlayGoal.patternImage = OverlayImage.fromResource(R.drawable.path_pattern)
        pathOverlayGoal.patternInterval = 50
        markerGoalPoint.position = latlngListGoal[latlngListGoal.size - 1]
        markerGoalPoint.iconTintColor = Color.RED
        markerGoalPoint.map = nMap
        pathOverlayGoal.map = nMap

        return LatLng(goalLat, goalLng)
    }

    fun setUIPathStart(): LatLng{
        val latlngListStart = ArrayList<LatLng>()
        val pathArr = pathData.route.traoptimal[0].path
        val goalLng = pathData.route.traoptimal[0].summary.goal.location[0]
        val goalLat = pathData.route.traoptimal[0].summary.goal.location[1]

        for(i in pathArr!!.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngListStart.add(pathLatLng)
        }

        pathOverlayStart.coords = latlngListStart
        pathOverlayStart.width = 30
        pathOverlayStart.color = Color.BLUE
        pathOverlayStart.patternImage = OverlayImage.fromResource(R.drawable.path_pattern)
        pathOverlayStart.patternInterval = 50
        markerStartPoint.position = latlngListStart[0]
        markerWayPoint.position = latlngListStart[latlngListStart.size - 1]
        markerStartPoint.map = nMap
        markerStartPoint.iconTintColor = Color.BLUE
        markerWayPoint.map = nMap
        pathOverlayStart.map = nMap

        return LatLng(goalLat, goalLng)
    }

    private fun parsingPath(rawPathData: String): LatLng{
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
    }
}