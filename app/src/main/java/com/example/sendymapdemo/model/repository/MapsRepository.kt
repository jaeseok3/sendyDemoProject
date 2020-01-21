package com.example.sendymapdemo.model.repository

import android.util.Log
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay

class MapsRepository:OnMapReadyCallback{
    var nMap:NaverMap? = null
    var markerStartPoint = Marker()
    var markerWayPoint = Marker()
    var markerGoalPoint = Marker()
    var pathOverlay:PathOverlay = PathOverlay()
    var listener: (()->Unit)? = null

    override fun onMapReady(naverMap: NaverMap) {
        nMap = naverMap
        listener?.invoke()
    }
}