package com.example.sendymapdemo

import android.app.Activity
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

var responseList = ArrayList<SummaryData>()
var responseData: PathData ?= null

class RequestActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var requestListView: ListView
    lateinit var adapter: requestListAdapter
    lateinit var animationDialog: loadingActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_activity)

        mSwipeRefreshLayout = findViewById(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(this)
        requestListView = findViewById(R.id.listview_requestdialog_list)

        Log.e("onCreate", "animationDialog Show")
    }

    override fun onResume() {
        super.onResume()
        adapter = requestListAdapter(this, requestList)
        requestListView.adapter = adapter
        requestListView.setOnItemClickListener { parent, view, position, id ->
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
                                adapter.getItem(position).time, adapter.getItem(position).distance, adapter.getItem(position).reward,
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                        )
                        InsertHistory(newHistory) //히스토리 리스트에 추가

                        val setPathUI = SetPathUI(responseList[position].responseData, nMap)
                        setPathUI.setUIPath()
                        val arrWay = responseList[position].wayPointLatLng.split(",")
                        val arrGoal = responseList[position].goalLatLng.split(",")
                        wayLatLng = LatLng(arrWay[1].toDouble(), arrWay[0].toDouble())
                        goalLatLng = LatLng(arrGoal[1].toDouble(), arrGoal[0].toDouble())

                        var mainIntent = Intent(this, MapsActivity::class.java)
                        mainIntent.putExtra("resultSrc", adapter.getItem(position).source)
                        mainIntent.putExtra("resultDst", adapter.getItem(position).destination)
//                       mainIntent.putExtra("resultDistance",adapter.getItem(position).distance)
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
        responseList.clear()
        positions.clear()
        finish()
    }

    override fun onRefresh() {
        adapter = requestListAdapter(this, requestList)
        requestListView.adapter = adapter
        requestListView.setOnItemClickListener { parent, view, position, id ->
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
                                adapter.getItem(position).time, adapter.getItem(position).distance, adapter.getItem(position).reward,
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                        )
                        InsertHistory(newHistory) //히스토리 리스트에 추가

                        val setPathUI = SetPathUI(responseList[position].responseData, nMap)
                        setPathUI.setUIPath()
                        val arrWay = responseList[position].wayPointLatLng.split(",")
                        val arrGoal = responseList[position].goalLatLng.split(",")
                        wayLatLng = LatLng(arrWay[1].toDouble(), arrWay[0].toDouble())
                        goalLatLng = LatLng(arrGoal[1].toDouble(), arrGoal[0].toDouble())

                        var mainIntent = Intent(this, MapsActivity::class.java)
                        mainIntent.putExtra("resultSrc", adapter.getItem(position).source)
                        mainIntent.putExtra("resultDst", adapter.getItem(position).destination)
//                       mainIntent.putExtra("resultDistance",adapter.getItem(position).distance)
                        setResult(Activity.RESULT_OK, mainIntent)
                        finish()
                    }
                    .setCancelable(false).show()
        }
        mSwipeRefreshLayout.isRefreshing = false
    }
}