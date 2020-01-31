package com.example.sendymapdemo.model.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.*
import com.example.sendymapdemo.model.retrofit.RetrofitNaverInterface
import com.example.sendymapdemo.model.retrofit.RetrofitServerInterface
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import com.naver.maps.geometry.LatLng
import io.reactivex.Observable
import kotlin.math.pow

class RequestRepository(private val retrofitServerInterface: RetrofitServerInterface,
                        private val retrofitNaverInterface: RetrofitNaverInterface) {

    private val NAVER_API_CLIENT = "nx5wmexmtw"
    private val NAVER_API_SECRET = "CS9kPn8fkidEzaDL3dv4tmQ6ymHVkXf2cy2doDZl"
    private var requestList = ArrayList<RequestListData>()
    lateinit var pathData:PathData
    var latlngList = ArrayList<LatLng>()

    fun findPath(currentPoint: String): ArrayList<RequestListData> {
        val option = "traoptimal"
            try {
                val newGeoInfo = GeoData(getLocationFromDB())
                val requestUserData = retrofitNaverInterface.requestPath(currentPoint, newGeoInfo.dst, newGeoInfo.src, option,
                        NAVER_API_CLIENT, NAVER_API_SECRET)
                val requestResult = requestUserData.execute().body()!!
                pathData = requestResult
                val time = requestResult.route.traoptimal[0].summary.duration / 60000
                val distance = requestResult.route.traoptimal[0].summary.distance / 1000.toDouble()
                val distanceStr = String.format("%.1f Km", distance)
                val timeStr = "$time" + "분"

                val face =
                        if(distance <= 20) R.drawable.ic_time_short
                        else if(distance > 20 && distance <= 40) R.drawable.ic_time_medium
                        else R.drawable.ic_time_long
                val reward = time.toDouble().pow(2).toInt()
                val requestListItem = RequestListData(face,
                        getGeoName(newGeoInfo.src), getGeoName(newGeoInfo.dst), timeStr, distanceStr, reward,
                        newGeoInfo.dst, newGeoInfo.src, requestResult)
                requestList.add(requestListItem)
            }catch (e: Exception){
                e.printStackTrace()
            }
        return requestList
    }
    private fun getLocationFromDB(): LocationData {
        val requestLocationData = retrofitServerInterface.getLocationDB()
        return requestLocationData.execute().body()!!
    }

    fun getList(): ArrayList<RequestListData> {
        return requestList
    }

    fun clearList(){
        requestList.clear()
    }
}