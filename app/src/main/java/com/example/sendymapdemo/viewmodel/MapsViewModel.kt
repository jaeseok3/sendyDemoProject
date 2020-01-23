package com.example.sendymapdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import kotlin.Exception

class MapsViewModel (private val dangerRepository: DangerRepository, private val historyRepository: HistoryRepository,
                     private val mapsRepository: MapsRepository, private val requestRepository: RequestRepository,
                     private val userRepository: UserRepository) : ViewModel() {
    var dangerGrade: MutableLiveData<String> ?= MutableLiveData()

    fun getUserID(): String {
        return userRepository.userID
    }

    fun insertHistory(userID: String, time: String, source: String, destination: String, distance: String, reward: String, htime: String, hdate: String){
        historyRepository.insertHistory(userID, time, source, destination, distance, reward, htime, hdate)
    }

    fun getLatLngList(): ArrayList<LatLng> {
        return requestRepository.latlngList
    }

    fun getFromRoom(userID: String): UserData {
        return userRepository.getFromRoom(userID)
    }

    fun getNaverMapRepository(): MapsRepository {
        return mapsRepository
    }

    fun getDangerGrade(startLat:String, startLng:String, endLat:String, endLng:String){
        val lineString = "LineString%($startLng $startLat, $endLng $endLat%)"
        var grade = ""
        Thread(Runnable {
            try{
                val requestDangerGrade = dangerRepository.getDangerGrade(lineString)
                grade = if(requestDangerGrade.resultCode != "10") {
                    when(requestDangerGrade.items.item[0].anals_grd){
                        "01" -> "1등급"
                        "02" -> "2등급"
                        "03" -> "3등급"
                        "04" -> "4등급"
                        "05" -> "5등급"
                        else -> "none"
                    }
                } else {
                    "측정불가지역"
                }
            }catch (e: Exception){
                e.printStackTrace()
            }finally {
                dangerGrade!!.postValue(grade)
            }
        }).start()
    }
}