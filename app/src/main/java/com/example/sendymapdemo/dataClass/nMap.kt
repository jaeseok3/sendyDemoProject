package com.example.sendymapdemo.dataClass

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker

class nMap{
    var nMap:NaverMap? = null
    var markerStartPoint = Marker()
    var markerWayPoint = Marker()
    var markerGoalPoint = Marker()
}