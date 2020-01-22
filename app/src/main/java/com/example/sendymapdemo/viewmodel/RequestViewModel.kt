package com.example.sendymapdemo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.model.repository.RequestRepository

class RequestViewModel(private var requestRepository: RequestRepository) : ViewModel() {
    var start : String = ""
    var requests:MutableLiveData<ArrayList<RequestListData>>? = MutableLiveData()

    fun setStartPoint(s : String){
        start = s
    }

//    fun fetchRequest(){
//        Log.e("패치리퀘스트","패치리퀘스트")
//        startFindPath(start)
//        //requests!!.setValue(requestRepository.getList())
//    }

    fun startFindPath(startPosition: String){
        val findPathThread = Thread( Runnable {
            for(i in 0..4) {
//                val newGeoInfo = GeoData(getLocationFromDB())
//                Log.e("출발지", newGeoInfo.src)
//                Log.e("도착지", newGeoInfo.dst)
                try {
                    val temp = requestRepository.findPath(startPosition)
                    Log.e("템프",temp.toString())
                    requests!!.postValue(temp)
                    Log.e("포루프문후","포루프문후")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        findPathThread.start()
//        for(i in 0..4){
//            try {
//                    requests!!.setValue(requestRepository.findPath(startPosition))
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//        Log.e("로푸르문","로푸르문")
//        }
    }

    fun clear(){
        requestRepository.clearList()
    }

}