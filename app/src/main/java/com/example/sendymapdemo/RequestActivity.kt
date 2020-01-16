package com.example.sendymapdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import android.widget.BaseAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.naver.maps.geometry.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Thread.sleep
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

class RequestActivity : AppCompatActivity() {
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var requestListView: ListView
    lateinit var adapter: requestListAdapter
    lateinit var startPosition: String
    lateinit var animationDialog: loadingActivity
    lateinit var context: Context

    private fun findPath(currentPoint:String, startPoint:String, goalPoint:String){
        val restClient: RetrofitInterface = Http3RetrofitManager.getRetrofitService(RetrofitInterface::class.java)
        val option = "traoptimal"
        val requestPath = restClient.requestPath(currentPoint, goalPoint, startPoint, option)

        requestPath.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }
            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                if(response.isSuccessful){
//                    responseData = response.body()
                    val data = SummaryData(currentPoint, startPoint, goalPoint, response.body()!!)
                    val time = data.responseData.route.traoptimal[0].summary.duration / 60000
                    Log.e("거리_시간", time.toString())
                    val distance = data.responseData.route.traoptimal[0].summary.distance / 1000.toDouble()
                    Log.e("거리", distance.toString())

                    val distanceStr = String.format("%.1f Km", distance)
                    val timeStr = "$time" + "Min"
                    val face =
                            if (distance <= 20) R.drawable.happy
                            else if (distance > 20 && distance <= 40) R.drawable.sad
                            else R.drawable.dead
                    val reward = time.toDouble().pow(2).toInt()
                    val RI = requestInfo(face,
                            getGeoName(data.wayPointLatLng),
                            getGeoName(data.goalLatLng),
                            timeStr, distanceStr, reward,
                            data.goalLatLng,
                            data.wayPointLatLng,
                            data.responseData)
                    requestList.add(RI)

                    adapter = requestListAdapter(context, requestList)
                    requestListView.adapter = adapter
                    Log.e("requestListSize", "${requestList.size}")
                }
            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_activity)

        requestListView = findViewById(R.id.listview_requestdialog_list)
        startPosition = intent.getStringExtra("startPoint")!!
        context = this

        for(i in 0..8 step 2) {
            val newGeoInfo = geoInfo(getLocationDB())
            positions.add(newGeoInfo.src)
            Log.e("출발지", newGeoInfo.src)
            positions.add(newGeoInfo.dst)
            Log.e("도착지", newGeoInfo.dst)
            try {
                findPath(startPosition, positions[i], positions[i + 1])
            } catch (e: Exception) {
                Log.e("e", "${e.printStackTrace()}")
            }
        }
        Log.e("onCreate", "animationDialog Show")
    }

    override fun onResume() {
        super.onResume()

        requestListView.setOnItemClickListener { _, _, position, _ ->
            val oDialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
            oDialog.setMessage("수락시 의뢰 리스트가 초기화됩니다.").setTitle("해당 의뢰를 수락하시겠습니까?")
                .setPositiveButton("아니오") { _, _ ->
                    makeText(this, "취소", LENGTH_LONG).show()
                }
                .setNeutralButton("예") { _, _ ->
                    Log.e("선택한 출발지", adapter.getItem(position).source)
                    Log.e("선택한 출발지_코드", adapter.getItem(position).sourceCode)
                    Log.e("선택한 도착지", adapter.getItem(position).destination)
                    Log.e("선택한 도착지_코드", adapter.getItem(position).destinationCode)
                    //새로운 히스토리추가
                    var newHistory = historyInfo(
                        adapter.getItem(position).source, adapter.getItem(position).destination,
                        adapter.getItem(position).time, adapter.getItem(position).distance, adapter.getItem(position).reward.toString(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                    )
                    InsertHistory(newHistory) //히스토리 리스트에 추가

                    val setPathUI = SetPathUI(requestList[position].responseData, nMap)
                    setPathUI.setUIPath()
                    val arrWay = requestList[position].sourceCode.split(",")
                    val arrGoal = requestList[position].destinationCode.split(",")
                    wayLatLng = LatLng(arrWay[1].toDouble(), arrWay[0].toDouble())
                    goalLatLng = LatLng(arrGoal[1].toDouble(), arrGoal[0].toDouble())

                    var mainIntent = Intent(this, MapsActivity::class.java)
                    mainIntent.putExtra("resultSrc", adapter.getItem(position).source)
                    mainIntent.putExtra("resultDst", adapter.getItem(position).destination)
                    mainIntent.putExtra("resultDistance",adapter.getItem(position).distance)
                    mainIntent.putExtra("wayLatLng[0]", arrWay[1].toDouble())
                    mainIntent.putExtra("wayLatLng[1]", arrWay[0].toDouble())
                    mainIntent.putExtra("goalLatLng[0]", arrGoal[1].toDouble())
                    mainIntent.putExtra("goalLatLng[1]", arrGoal[0].toDouble())
                    setResult(Activity.RESULT_OK, mainIntent)
                    finish()
                }
                .setCancelable(false).show()
        }
        Log.e("onResume", "onResume")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        requestList.clear()
        positions.clear()
        finish()
    }
}