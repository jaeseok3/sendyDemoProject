package com.example.sendymapdemo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
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

    fun getUserID(): String {
        return userRepository.userID
    }

    fun getMapsRepository(): MapsRepository {
        return mapsRepository
    }

    fun getMapsPathOverlay(): PathOverlay {
        return mapsRepository.pathOverlay
    }

    fun getMapsMarkerStartPoint(): Marker {
        return mapsRepository.markerStartPoint
    }

    fun getMapsMarkerWayPoint(): Marker {
        return mapsRepository.markerWayPoint
    }

    fun getMapsMarkerGoalPoint(): Marker {
        return mapsRepository.markerGoalPoint
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