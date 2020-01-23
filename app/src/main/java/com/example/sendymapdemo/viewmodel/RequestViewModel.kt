package com.example.sendymapdemo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.model.repository.MapsRepository
import com.example.sendymapdemo.model.repository.RequestRepository
import com.naver.maps.geometry.LatLng

class RequestViewModel(var requestRepository: RequestRepository, var nMap:MapsRepository) : ViewModel() {
    var start : String = ""
    var requests:MutableLiveData<ArrayList<RequestListData>> = MutableLiveData()
    var latlngList = requestRepository.latlngList

    fun setStartPoint(s : String){
        start = s
    }

    fun setLatlng(){
        requestRepository.latlngList = latlngList
    }

    fun startFindPath(startPosition: String){
        val findPathThread = Thread( Runnable {
            for(i in 0..4) {
                try {
                    val temp = requestRepository.findPath(startPosition)
                    Log.e("템프",temp.toString())
                    requests.postValue(temp)
                    Log.e("포루프문후","포루프문후")
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