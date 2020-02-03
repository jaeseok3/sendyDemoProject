package com.example.sendymapdemo.viewmodel

import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.Exception

class MapsViewModel (private val dangerRepository: DangerRepository, private val historyRepository: HistoryRepository,
                     private val mapsRepository: MapsRepository, private val requestRepository: RequestRepository,
                     private val userRepository: UserRepository) : ViewModel() {

    var dangerGrade: MutableLiveData<String> ?= MutableLiveData()
    var liveStartLatLng: MutableLiveData<LatLng> ?= MutableLiveData()
    var liveWayLatLng: MutableLiveData<LatLng> ?= MutableLiveData()
    var liveGoalLatLng: MutableLiveData<LatLng> ?= MutableLiveData()
    var requests: MutableLiveData<ArrayList<RequestListData>> = MutableLiveData()
    var requestListSize: MutableLiveData<Int> = MutableLiveData()
    var latlngList = ArrayList<LatLng>()
    var requestListData: RequestListData ?= null

    private var startLatLng: LatLng ?= null
    private var wayLatLng: LatLng ?= null
    private var goalLatLng: LatLng ?= null
    private var requestList = ArrayList<RequestListData>()

    fun setDistanceInfo(progressRate: Double): String {
        val distanceStrArr = requestListData!!.distance.split(" Km")
        val distanceDouble = distanceStrArr[0].toDouble() * (1 - progressRate)
        return String.format("%.1f", distanceDouble) + " Km"
    }

    fun getPathOverlay(): PathOverlay {
        return mapsRepository.pathOverlay
    }

    fun getStartMarker(): Marker {
        return mapsRepository.markerStartPoint
    }

    fun getWayMarker(): Marker {
        return mapsRepository.markerWayPoint
    }

    fun getGoalMarker(): Marker {
        return mapsRepository.markerGoalPoint
    }

    fun getMapsRepository(): MapsRepository {
        return mapsRepository
    }

    fun setPathData(listPosition: Int){
        val pathArr = requestList[listPosition].responseData.route.traoptimal[0].path
        startLatLng = LatLng(requestList[listPosition].responseData.route.traoptimal[0].summary.start.location[1],
                requestList[listPosition].responseData.route.traoptimal[0].summary.start.location[0])
        wayLatLng = LatLng(requestList[listPosition].responseData.route.traoptimal[0].summary.waypoints[0].location[1],
                requestList[listPosition].responseData.route.traoptimal[0].summary.waypoints[0].location[0])
        goalLatLng = LatLng(requestList[listPosition].responseData.route.traoptimal[0].summary.goal.location[1],
                requestList[listPosition].responseData.route.traoptimal[0].summary.goal.location[0])

        liveStartLatLng!!.postValue(startLatLng)
        liveWayLatLng!!.postValue(wayLatLng)
        liveGoalLatLng!!.postValue(goalLatLng)

        for(i in pathArr.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngList.add(pathLatLng)
        }
    }

    fun checkError(location: Location, goalLatLng: LatLng): Boolean {
        val currentLat = location.latitude
        val currentLng = location.longitude
        val goalLat = goalLatLng.latitude
        val goalLng = goalLatLng.longitude
        return ((currentLat <= goalLat + 0.0001 && currentLat >= goalLat - 0.0001) ||
                (currentLng <= goalLng + 0.0001 && currentLng >= goalLng - 0.0001))
    }

    private fun parsingPath(rawPathData: String): LatLng {
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
    }

    fun insertHistory(userID: String){
        val time = requestListData!!.time
        val source = requestListData!!.source
        val destination = requestListData!!.destination
        val distance = requestListData!!.distance
        val reward = requestListData!!.reward.toString()
        val hTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초"))
        val hDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        historyRepository.insertHistory(userID, time, source, destination, distance, reward, hTime, hDate)
    }

    fun setRequestData(position: Int){
        requestListData = requestList[position]
    }

    fun updateCredit(userID: String) {
        userRepository.updateCredit(userID, requestListData!!.reward)
    }

    fun getUserDataFromRepository(): UserData {
        return userRepository.userData
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

    fun startFindPath(startPosition: String){
        requestList.clear()
        val findPathThread = Thread( Runnable {
            for(i in 0..4) {
                try {
                    requestList = requestRepository.findPath(startPosition)
                    requests.postValue(requestList)
                    requestListSize.postValue(i)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        findPathThread.start()
    }

    fun requestListClear(){
        requestList.clear()
    }
}