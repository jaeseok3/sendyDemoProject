package com.example.sendymapdemo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.model.repository.MapsRepository
import com.example.sendymapdemo.model.repository.RequestRepository
import com.naver.maps.geometry.LatLng

class RequestViewModel(private var requestRepository: RequestRepository, var mapsRepository: MapsRepository) : ViewModel() {
    var requests:MutableLiveData<ArrayList<RequestListData>> = MutableLiveData()
    var latlngList = requestRepository.latlngList
    var start : String = ""

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