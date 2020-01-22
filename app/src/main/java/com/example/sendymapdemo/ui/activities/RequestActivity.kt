package com.example.sendymapdemo.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.SetPathUI
import com.example.sendymapdemo.model.repository.*
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.history_activiry.*
import kotlinx.android.synthetic.main.request_activity.*
import org.koin.android.ext.android.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RequestActivity : AppCompatActivity() {
    private lateinit var adapter: RequestRecyclerAdapter
    private lateinit var requestLayoutManager: LinearLayoutManager

    private val nMap: MapsRepository by inject()
    private val userRepository: UserRepository by inject()
    private val pathDataRepository: PathDataRepository by inject()
    private val historyRepository: HistoryRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_activity)

        adapter = pathDataRepository.adapter
    }

    override fun onResume() {
        super.onResume()
        requestLayoutManager = LinearLayoutManager(this)
        request_recyclerView.adapter = adapter
        request_recyclerView.layoutManager = requestLayoutManager
        request_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        Log.e("onResume", "onResume")

        adapter.itemClick = object : RequestRecyclerAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int) {
                val currentList = pathDataRepository.getList()
                val oDialog = AlertDialog.Builder(view.context, android.R.style.Theme_DeviceDefault_Light_Dialog)
                oDialog.setMessage("수락시 의뢰 리스트가 초기화됩니다.").setTitle("해당 의뢰를 수락하시겠습니까?")
                        .setPositiveButton("아니오") { _, _ ->
                            makeText(view.context, "취소", LENGTH_LONG).show()
                        }
                        .setNeutralButton("예") { _, _ ->
                            Log.e("선택한 출발지", currentList[position].source)
                            Log.e("선택한 출발지_코드", currentList[position].sourceCode)
                            Log.e("선택한 도착지", currentList[position].destination)
                            Log.e("선택한 도착지_코드", currentList[position].destinationCode)

                            historyRepository.insertHistory(userRepository.userID, currentList[position].time,
                                    currentList[position].source,
                                    currentList[position].destination,
                                    currentList[position].distance,
                                    currentList[position].reward.toString(),
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))

                            val setPathUI = SetPathUI(currentList[position].responseData, nMap)
                            setPathUI.setUIPath()
                            val arrWay = currentList[position].sourceCode.split(",")
                            val arrGoal = currentList[position].destinationCode.split(",")

                            val intent = Intent(this@RequestActivity, MapsActivity::class.java)

                            intent.putExtra("resultSrc", currentList[position].source)
                            intent.putExtra("resultDst", currentList[position].destination)
                            intent.putExtra("resultDistance", currentList[position].distance)
                            intent.putExtra("wayLatLng[0]", arrWay[1].toDouble())
                            intent.putExtra("wayLatLng[1]", arrWay[0].toDouble())
                            intent.putExtra("goalLatLng[0]", arrGoal[1].toDouble())
                            intent.putExtra("goalLatLng[1]", arrGoal[0].toDouble())

                            setResult(Activity.RESULT_OK, intent)
                            pathDataRepository.clearList()
                            finish()
                        }
                        .setCancelable(false).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        pathDataRepository.clearList()
        finish()
    }
}