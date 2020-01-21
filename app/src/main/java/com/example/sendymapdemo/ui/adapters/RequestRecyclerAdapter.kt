package com.example.sendymapdemo.ui.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.SetPathUI
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.ui.activities.MapsActivity
import com.naver.maps.geometry.LatLng
import kotlinx.android.synthetic.main.request_listview_item.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RequestRecyclerAdapter (private val dataSource: ArrayList<RequestListData>): RecyclerView.Adapter<RequestRecyclerAdapter.ViewHolder>() {
    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val view: View = v
        fun bind(listener: View.OnClickListener, items: ArrayList<RequestListData>) {
            view.requestFeeling.setImageResource(items[position].image)
            view.requestSrc.text = items[position].source
            view.requestDst.text = items[position].destination
            view.requestTime.text = items[position].time
            view.requestDuration.text = items[position].distance
            view.requestReward.text = items[position].reward.toString()
            view.setOnClickListener(listener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.request_listview_item, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = dataSource.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listener = View.OnClickListener {
            val oDialog = AlertDialog.Builder(it.context, android.R.style.Theme_DeviceDefault_Light_Dialog)
            oDialog.setMessage("수락시 의뢰 리스트가 초기화됩니다.").setTitle("해당 의뢰를 수락하시겠습니까?")
                    .setPositiveButton("아니오") { _, _ ->
                        makeText(it.context, "취소", Toast.LENGTH_LONG).show()
                    }
                    .setNeutralButton("예") { _, _ ->
                        Log.e("선택한 출발지", dataSource[position].source)
                        Log.e("선택한 출발지_코드", dataSource[position].sourceCode)
                        Log.e("선택한 도착지", dataSource[position].destination)
                        Log.e("선택한 도착지_코드", dataSource[position].destinationCode)

//                        historyRepository.insertHistory(userRepository.userID, adapter.getItem(position).time, adapter.getItem(position).source,
//                                adapter.getItem(position).destination, adapter.getItem(position).distance, adapter.getItem(position).reward.toString(),
//                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
//                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))

//                        val setPathUI = SetPathUI(requestList[position].responseData, nMap)
//                        setPathUI.setUIPath()
//                        val arrWay = requestList[position].sourceCode.split(",")
//                        val arrGoal = requestList[position].destinationCode.split(",")
//                        wayLatLng = LatLng(arrWay[1].toDouble(), arrWay[0].toDouble())
//                        goalLatLng = LatLng(arrGoal[1].toDouble(), arrGoal[0].toDouble())
//
//                        val mainIntent = Intent(this, MapsActivity::class.java)
//                        mainIntent.putExtra("resultSrc", adapter.getItem(position).source)
//                        mainIntent.putExtra("resultDst", adapter.getItem(position).destination)
//                        mainIntent.putExtra("resultDistance",adapter.getItem(position).distance)
//                        mainIntent.putExtra("wayLatLng[0]", arrWay[1].toDouble())
//                        mainIntent.putExtra("wayLatLng[1]", arrWay[0].toDouble())
//                        mainIntent.putExtra("goalLatLng[0]", arrGoal[1].toDouble())
//                        mainIntent.putExtra("goalLatLng[1]", arrGoal[0].toDouble())
//                        setResult(Activity.RESULT_OK, mainIntent)
//                        requestList.clear()
//                        finish()
//                    }
//                    .setCancelable(false).show()
                    }
        }
    }
}