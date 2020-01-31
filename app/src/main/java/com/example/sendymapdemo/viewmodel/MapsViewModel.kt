package com.example.sendymapdemo.viewmodel

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.PathData
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.Exception

class MapsViewModel (private val dangerRepository: DangerRepository, private val historyRepository: HistoryRepository,
                     private val mapsRepository: MapsRepository, private val requestRepository: RequestRepository,
                     private val userRepository: UserRepository) : ViewModel() {
    var dangerGrade: MutableLiveData<String> ?= MutableLiveData()
    var userData: MutableLiveData<UserData> ?= MutableLiveData()

    fun getMapsRepository(): MapsRepository {
        return mapsRepository
    }

    fun setUIPath(listPosition: Int){
        val pathArr = requestRepository.getList()[listPosition].responseData.route.traoptimal[0].path
        val startLng = requestRepository.getList()[listPosition].responseData.route.traoptimal[0].summary.start.location[0]
        val startLat = requestRepository.getList()[listPosition].responseData.route.traoptimal[0].summary.start.location[1]
        val wayPointLng = requestRepository.getList()[listPosition].responseData.route.traoptimal[0].summary.waypoints[0].location[0]
        val wayPointLat = requestRepository.getList()[listPosition].responseData.route.traoptimal[0].summary.waypoints[0].location[1]
        val goalLng = requestRepository.getList()[listPosition].responseData.route.traoptimal[0].summary.goal.location[0]
        val goalLat = requestRepository.getList()[listPosition].responseData.route.traoptimal[0].summary.goal.location[1]

        for(i in pathArr.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngList.add(pathLatLng)
            setLatlng()
        }

        mapsRepository.pathOverlay.coords = latlngList
        mapsRepository.pathOverlay.width = 10
        mapsRepository.pathOverlay.color = Color.parseColor("#2e58ec")
        mapsRepository.pathOverlay.passedColor = Color.GRAY
        mapsRepository.pathOverlay.map = mapsRepository.nMap!!
        mapsRepository.markerStartPoint.position = LatLng(startLat, startLng)
        mapsRepository.markerStartPoint.icon = OverlayImage.fromResource(R.drawable.ic_pin_ar_blue)
        mapsRepository.markerStartPoint.map = mapsRepository.nMap!!
        mapsRepository.markerWayPoint.position = LatLng(wayPointLat, wayPointLng)
        mapsRepository.markerWayPoint.icon = OverlayImage.fromResource(R.drawable.ic_pin_wp_purple)
        mapsRepository.markerWayPoint.map = mapsRepository.nMap!!
        mapsRepository.markerGoalPoint.position = LatLng(goalLat, goalLng)
        mapsRepository.markerGoalPoint.icon = OverlayImage.fromResource(R.drawable.ic_pin_dp_cyan)
        mapsRepository.markerGoalPoint.map = mapsRepository.nMap!!
    }

    fun checkError(location: LatLng, goalLatLng: LatLng): Boolean {
        val currentLat = location.latitude
        val currentLng = location.longitude
        val goalLat = goalLatLng.latitude
        val goalLng = goalLatLng.longitude
        return ((currentLat <= goalLat + 0.0001 && currentLat >= goalLat - 0.0001) ||
                (currentLng <= goalLng + 0.0001 && currentLng >= goalLng - 0.0001))
    }

    fun parsingPath(rawPathData: String): LatLng {
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
    }

    fun insertHistory(userID: String, time: String, source: String, destination: String, distance: String, reward: String, htime: String, hdate: String){
        historyRepository.insertHistory(userID, time, source, destination, distance, reward, htime, hdate)
    }

    fun getLatLngList(): ArrayList<LatLng> {
        return requestRepository.latlngList
    }

    fun updateCredit(userID: String, credit: Double) {
        userRepository.updateCredit(userID, credit)
    }

    fun getUserDataFromServer(userID: String) {
        userRepository.getData(userID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {userData!!.postValue(it)
                        Log.e("userDATA", "$it")},
                        {userData!!.postValue(null)},
                        { Log.e("user data condition", "$userData")}
                )
    }

    fun getDangerGrade(startLat:String, startLng:String, endLat:String, endLng:String){
        val lineString = "LineString($startLng $startLat, $endLng $endLat)"
        var grade = ""
        Thread(Runnable {
            try{
                val requestDangerGrade = dangerRepository.getDangerGrade(lineString)
                grade = if(requestDangerGrade.resultCode != "10") {
                    when(requestDangerGrade.items.item[0].anals_grd){
                        "01","02" -> "안전"
                        "03","04" -> "주의"
                        "05" -> "위험"
                        else -> "측정불가"
                    }
                } else {
                    "측정불가"
                }
            }catch (e: Exception){
                e.printStackTrace()
            }finally {
                dangerGrade!!.postValue(grade)
            }
        }).start()
    }

    var requests:MutableLiveData<ArrayList<RequestListData>> = MutableLiveData()
    var latlngList = requestRepository.latlngList

    fun setLatlng(){
        requestRepository.latlngList = latlngList
    }

    fun startFindPath(startPosition: String){
        val findPathThread = Thread( Runnable {
            for(i in 0..4) {
                try {
                    val requestList = requestRepository.findPath(startPosition)
                    requests.postValue(requestList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        findPathThread.start()
    }

    fun clear(){
        requestRepository.clearList()
    }
}