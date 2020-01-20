package com.example.sendymapdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sendymapdemo.dataClass.*
import com.example.sendymapdemo.model.retrofit.RetrofitInterface
import com.example.sendymapdemo.model.retrofit.RetrofitNaverAPIManager
import com.naver.maps.geometry.LatLng
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

class RequestActivity : AppCompatActivity() {
    private val NAVER_API_CLIENT = "nx5wmexmtw"
    private val NAVER_API_SECRET = "CS9kPn8fkidEzaDL3dv4tmQ6ymHVkXf2cy2doDZl"
    private var requestList = ArrayList<RequestListData>()
    private var positions = ArrayList<String>()
    private var wayLatLng: LatLng ?= null
    private var goalLatLng: LatLng ?= null
    private lateinit var requestListView: ListView
    private lateinit var adapter: requestListAdapter
    private lateinit var startPosition: String
    private lateinit var context: Context

    private val userData: UserData by inject()
    private val retrofitInterface: RetrofitInterface by inject()

    private fun findPath(currentPoint:String, startPoint:String, goalPoint:String){
        val restClient: RetrofitInterface = RetrofitNaverAPIManager.getRetrofitService(RetrofitInterface::class.java)
        val option = "traoptimal"
        val requestPath = restClient.requestPath(currentPoint, goalPoint, startPoint, option, NAVER_API_CLIENT, NAVER_API_SECRET)

        requestPath.enqueue(object : Callback<PathData> {
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
                            if (distance <= 20) R.drawable.happy
                            else if (distance > 20 && distance <= 40) R.drawable.sad
                            else R.drawable.dead
                    val reward = time.toDouble().pow(2).toInt()
                    val RI = RequestListData(face,
                            getGeoName(wayLatLng.toString()),
                            getGeoName(goalLatLng.toString()),
                            timeStr, distanceStr, reward,
                            goalLatLng.toString(),
                            wayLatLng.toString(),
                            response.body()!!)
                    requestList.add(RI)

                    adapter = requestListAdapter(context, requestList)
                    requestListView.adapter = adapter
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
            val newGeoInfo = GeoData(getLocationDB())
            positions.add(newGeoInfo.src)
            Log.e("출발지", newGeoInfo.src)
            positions.add(newGeoInfo.dst)
            Log.e("도착지", newGeoInfo.dst)
            try {
                findPath(startPosition, positions[i], positions[i + 1])
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                    val newHistory = HistoryData(
                            adapter.getItem(position).source, adapter.getItem(position).destination,
                            adapter.getItem(position).time, adapter.getItem(position).distance, adapter.getItem(position).reward.toString(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                    )
                    retrofitInterface.InsertHistory(userData.ID, newHistory.time, newHistory.source, newHistory.destination, newHistory.distance,
                            newHistory.reward, newHistory.historyTime, newHistory.historyDate)

                    val setPathUI = SetPathUI(requestList[position].responseData, nMap)
                    setPathUI.setUIPath()
                    val arrWay = requestList[position].sourceCode.split(",")
                    val arrGoal = requestList[position].destinationCode.split(",")
                    wayLatLng = LatLng(arrWay[1].toDouble(), arrWay[0].toDouble())
                    goalLatLng = LatLng(arrGoal[1].toDouble(), arrGoal[0].toDouble())

                    val mainIntent = Intent(this, MapsActivity::class.java)
                    mainIntent.putExtra("resultSrc", adapter.getItem(position).source)
                    mainIntent.putExtra("resultDst", adapter.getItem(position).destination)
                    mainIntent.putExtra("resultDistance",adapter.getItem(position).distance)
                    mainIntent.putExtra("wayLatLng[0]", arrWay[1].toDouble())
                    mainIntent.putExtra("wayLatLng[1]", arrWay[0].toDouble())
                    mainIntent.putExtra("goalLatLng[0]", arrGoal[1].toDouble())
                    mainIntent.putExtra("goalLatLng[1]", arrGoal[0].toDouble())
                    setResult(Activity.RESULT_OK, mainIntent)
                    requestList.clear()
                    positions.clear()
                    finish()
                }
                .setCancelable(false).show()
        }
        Log.e("onResume", "onResume")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        requestList.clear()
        positions.clear()
        finish()
    }
}