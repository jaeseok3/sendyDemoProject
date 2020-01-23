package com.example.sendymapdemo.ui.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.PathData
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import com.example.sendymapdemo.viewmodel.RequestViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.android.synthetic.main.request_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RequestActivity : AppCompatActivity() {
    private lateinit var adapter: RequestRecyclerAdapter
    private lateinit var requestLayoutManager: LinearLayoutManager

    private val requestViewModel by viewModel<RequestViewModel>()
    private lateinit var pathData: PathData
    private var newRequestList : ArrayList<RequestListData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_activity)

        val intent = intent
        //liveDataRequestListViewModel = ViewModelProviders.of(this).get(RequestViewModel::class.java)
        //equestViewModel.setStartPoint(intent.getStringExtra("startPoint")!!)
        //adapter = RequestRecyclerAdapter(ArrayList())
        requestViewModel.startFindPath(intent.getStringExtra("startPoint")!!)
        subscribe()
    }

    private fun subscribe(){
        val requestListObserver = Observer<ArrayList<RequestListData>> {
            newRequestList = it
            recyclerViewSetup()
        }
        requestViewModel.requests.observe(this, requestListObserver)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        requestViewModel.clear()
        finish()
    }

    private fun recyclerViewSetup(){
        adapter = RequestRecyclerAdapter(newRequestList!!)
        requestLayoutManager = LinearLayoutManager(this)
        request_recyclerView.adapter = adapter
        request_recyclerView.layoutManager = requestLayoutManager
        request_recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        Log.e("리사이클러뷰 셋업", "리사이클러뷰 셋업")
        adapter.notifyDataSetChanged()
        if(newRequestList!!.size == 5){
            adapter.itemClick = object : RequestRecyclerAdapter.OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int) {
                    //val currentList = requestRepository.getList()
                    val oDialog = AlertDialog.Builder(view.context, android.R.style.Theme_DeviceDefault_Light_Dialog)
                    oDialog.setMessage("수락시 의뢰 리스트가 초기화됩니다.").setTitle("해당 의뢰를 수락하시겠습니까?")
                        .setPositiveButton("아니오") { _, _ ->
                            makeText(view.context, "취소", LENGTH_LONG).show()
                        }
                        .setNeutralButton("예") { _, _ ->
                            Log.e("선택한 출발지", newRequestList!![position].source)
                            Log.e("선택한 출발지_코드", newRequestList!![position].sourceCode)
                            Log.e("선택한 도착지", newRequestList!![position].destination)
                            Log.e("선택한 도착지_코드", newRequestList!![position].destinationCode)

                            pathData = newRequestList!![position].responseData
                            setUIPath()

                            val arrWay = newRequestList!![position].sourceCode.split(",")
                            val arrGoal = newRequestList!![position].destinationCode.split(",")

                            intent.putExtra("resultSrc", newRequestList!![position].source)
                            intent.putExtra("resultDst", newRequestList!![position].destination)
                            intent.putExtra("resultDistance", newRequestList!![position].distance)
                            intent.putExtra("fullTime",newRequestList!![position].time)
                            intent.putExtra("wayLatLng[0]", arrWay[1].toDouble())
                            intent.putExtra("wayLatLng[1]", arrWay[0].toDouble())
                            intent.putExtra("goalLatLng[0]", arrGoal[1].toDouble())
                            intent.putExtra("goalLatLng[1]", arrGoal[0].toDouble())

                            setResult(Activity.RESULT_OK, intent)
                            requestViewModel.clear()
                            finish()
                        }
                        .setCancelable(false).show()
                }
            }
        }
    }
    fun setUIPath(){
        val pathArr = pathData.route.traoptimal[0].path
        val startLng = pathData.route.traoptimal[0].summary.start.location[0]
        val startLat = pathData.route.traoptimal[0].summary.start.location[1]
        val wayPointLng = pathData.route.traoptimal[0].summary.waypoints[0].location[0]
        val wayPointLat = pathData.route.traoptimal[0].summary.waypoints[0].location[1]
        val goalLng = pathData.route.traoptimal[0].summary.goal.location[0]
        val goalLat = pathData.route.traoptimal[0].summary.goal.location[1]

        for(i in pathArr.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            requestViewModel.latlngList.add(pathLatLng)
            requestViewModel.setLatlng()
        }

        requestViewModel.nMap.pathOverlay.coords = requestViewModel.latlngList
        requestViewModel.nMap.pathOverlay.width = 30
        requestViewModel.nMap.pathOverlay.color = Color.BLUE
        requestViewModel.nMap.pathOverlay.patternImage = OverlayImage.fromResource(R.drawable.path_pattern)
        requestViewModel.nMap.pathOverlay.patternInterval = 50
        requestViewModel.nMap.pathOverlay.passedColor = Color.GRAY
        requestViewModel.nMap.markerStartPoint.position = LatLng(startLat, startLng)
        requestViewModel.nMap.markerWayPoint.position = LatLng(wayPointLat, wayPointLng)
        requestViewModel.nMap.markerGoalPoint.position = LatLng(goalLat, goalLng)
        requestViewModel.nMap.markerStartPoint.iconTintColor = Color.BLUE
        requestViewModel.nMap.markerWayPoint.iconTintColor = Color.GREEN
        requestViewModel.nMap.markerGoalPoint.iconTintColor = Color.RED
        requestViewModel.nMap.markerStartPoint.map = requestViewModel.nMap.nMap!!
        requestViewModel.nMap.markerWayPoint.map = requestViewModel.nMap.nMap!!
        requestViewModel.nMap.markerGoalPoint.map = requestViewModel.nMap.nMap!!
        requestViewModel.nMap.pathOverlay.map = requestViewModel.nMap.nMap!!
    }

    private fun parsingPath(rawPathData: String): LatLng {
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
    }
}