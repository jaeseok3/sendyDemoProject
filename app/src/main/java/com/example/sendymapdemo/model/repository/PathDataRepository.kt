package com.example.sendymapdemo.model.repository

import android.app.Application
import android.widget.ListView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataClass.PathData
import com.example.sendymapdemo.dataClass.RequestListData
import com.example.sendymapdemo.dataClass.getGeoName
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.ui.adapters.RequestListAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.pow

class PathDataRepository(private val application: Application, private val retrofitInterface: RetrofitInterface) {
    private val NAVER_API_CLIENT = "nx5wmexmtw"
    private val NAVER_API_SECRET = "CS9kPn8fkidEzaDL3dv4tmQ6ymHVkXf2cy2doDZl"
    private val requestList = ArrayList<RequestListData>()
    private lateinit var adapter : RequestListAdapter
    private lateinit var requestListView: ListView

    fun findPath(currentPoint: String, startPoint: String, goalPoint: String) {
        val option = "traoptimal"
        val requestUserData = retrofitInterface.requestPath(currentPoint, goalPoint, startPoint, option, NAVER_API_CLIENT, NAVER_API_SECRET)
        requestUserData.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }
            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                if(response.isSuccessful){
                    val time = response.body()!!.route.traoptimal[0].summary.duration / 60000
                    val distance = response.body()!!.route.traoptimal[0].summary.distance / 1000.toDouble()
                    val distanceStr = String.format("%.1f Km", distance)
                    val timeStr = "$time" + "Min"

                    val face =
                            if(distance <= 20) R.drawable.happy
                            else if(distance > 20 && distance <= 40) R.drawable.sad
                            else R.drawable.dead

                    val reward = time.toDouble().pow(2).toInt()
                    val requestListItem = RequestListData(face,
                            getGeoName(startPoint), getGeoName(goalPoint), timeStr, distanceStr, reward,
                            goalPoint, startPoint, response.body()!!)
                    requestList.add(requestListItem)
                    adapter = RequestListAdapter(application, requestList)
                    requestListView.adapter = adapter
                }
            }
        })
    }
}