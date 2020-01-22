package com.example.sendymapdemo.model.repository

import android.app.Application
import android.util.Log
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.dataclass.getGeoName
import com.example.sendymapdemo.model.retrofit.RetrofitNaverInterface
import com.example.sendymapdemo.ui.adapters.RequestListAdapter
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import kotlin.math.pow

class PathDataRepository(private val retrofitInterface: RetrofitNaverInterface) {
    private val NAVER_API_CLIENT = "nx5wmexmtw"
    private val NAVER_API_SECRET = "CS9kPn8fkidEzaDL3dv4tmQ6ymHVkXf2cy2doDZl"
    private val requestList = ArrayList<RequestListData>()
    lateinit var adapter : RequestRecyclerAdapter

    fun findPath(currentPoint: String, startPoint: String, goalPoint: String) {
        val option = "traoptimal"
        val requestUserData = retrofitInterface.requestPath(currentPoint, goalPoint, startPoint, option, NAVER_API_CLIENT, NAVER_API_SECRET)
        try {
            val requestResult = requestUserData.execute().body()!!
            val time = requestResult.route.traoptimal[0].summary.duration / 60000
            val distance = requestResult.route.traoptimal[0].summary.distance / 1000.toDouble()
            val distanceStr = String.format("%.1f Km", distance)
            val timeStr = "$time" + "Min"

            val face =
                    if(distance <= 20) R.drawable.happy
                    else if(distance > 20 && distance <= 40) R.drawable.sad
                    else R.drawable.dead
            val reward = time.toDouble().pow(2).toInt()
            val requestListItem = RequestListData(face,
                    getGeoName(startPoint), getGeoName(goalPoint), timeStr, distanceStr, reward,
                    goalPoint, startPoint, requestResult)
            requestList.add(requestListItem)
            adapter = RequestRecyclerAdapter(requestList)
            Log.e("리스트 사이즈", "${requestList.size},${requestListItem}")
            adapter.notifyDataSetChanged()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun getList(): ArrayList<RequestListData> {
        return requestList
    }

    fun clearList(){
        requestList.clear()
    }
}