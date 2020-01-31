package com.example.sendymapdemo.ui.activities

import android.app.Dialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.PathData
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import com.example.sendymapdemo.viewmodel.MapsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.new_activity_main.*
import kotlinx.android.synthetic.main.new_activity_main.view.*
import kotlinx.android.synthetic.main.new_activity_maps.*
import kotlinx.android.synthetic.main.new_activity_maps.view.*
import kotlinx.android.synthetic.main.new_nav_header.view.*
import kotlinx.android.synthetic.main.new_request_item.view.clockImage
import kotlinx.android.synthetic.main.new_request_item.view.distance
import kotlinx.android.synthetic.main.new_request_item.view.dstText
import kotlinx.android.synthetic.main.new_request_item.view.srcText
import kotlinx.android.synthetic.main.new_request_item.view.time
import kotlinx.android.synthetic.main.request_dialog.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MapsActivity : Fragment(){
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    private val mapsViewModel by viewModel<MapsViewModel>()
    private lateinit var adapter: RequestRecyclerAdapter
    private lateinit var requestLayoutManager: LinearLayoutManager

    private lateinit var pathData: PathData
    private var newRequestList : ArrayList<RequestListData>? = null

    private lateinit var resultSrc : String
    private lateinit var resultDst : String
    private lateinit var fullTime : String

    var wayLatLng: LatLng ?= null
    var goalLatLng: LatLng ?= null
    private lateinit var locationSource: LocationSource
    private lateinit var currentLocation: Location

    private var arriveCheck: Boolean = false

    private var startPosition: String? = null
    private lateinit var resultDistance: String
    var progressRate = 0.0
    var resultReward:Double = 0.0
    private var userData: UserData ?= null
    override fun onDestroy() {
        super.onDestroy()
        //android.os.Process.killProcess(android.os.Process.myPid())
    }
    private fun subscribeUserData(){
        val userDataObserver = Observer<UserData> {
            userData = it
            Log.e("userDATA", "$it, userData")
            setUserDataInNav()
        }
        mapsViewModel.userData?.observe(this, userDataObserver)
    }

    private fun setUserDataInNav(){
        Log.e("유저", "$userData")
        new_nav_view.getHeaderView(0).userName.text = userData!!.id
        new_nav_view.getHeaderView(0).userRanking.text = "${userData!!.rank} 등"
        new_nav_view.getHeaderView(0).userCredit_new.text = userData!!.credit
        new_nav_view.getHeaderView(0).userAccum_new.text = userData!!.property
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nMap = mapsViewModel.getMapsRepository()
        var fragmentManager = childFragmentManager
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance((NaverMapOptions().locationButtonEnabled(true))
                .also {
                    fragmentManager.beginTransaction().add(R.id.map, map).commit()
                })
        mapFragment.getMapAsync(nMap)
        val userID = mapsViewModel.getUserID()
        var userData: UserData
        val headerView = view.new_nav_view.getHeaderView(0)

        new_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        headerView.historyButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_mapsActivity_to_historyActivity)
            new_drawer_layout.closeDrawer(GravityCompat.START)
        }
        headerView.rankingButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_mapsActivity_to_rankingActivity)
            new_drawer_layout.closeDrawer(GravityCompat.START)
        }

        locationStartBtn.setOnClickListener {
            val latlngList = mapsViewModel.getLatLngList()
            if(nMap.nMap!!.locationTrackingMode == LocationTrackingMode.None) {
                Log.e("Flag", "${nMap.nMap!!.locationTrackingMode}")
                val bottomSheetBehavior_after = BottomSheetBehavior.from(view.activity_main.bottomSheet_after)
                bottomSheetBehavior_after.state = BottomSheetBehavior.STATE_COLLAPSED
                Thread(Runnable{
                    for (i in 0 until latlngList.size) {
                        currentLocation.latitude = latlngList[i].latitude
                        currentLocation.longitude = latlngList[i].longitude
                        progressRate = i / latlngList.size.toDouble()

                        Log.e("위치변경", "${currentLocation.latitude}, ${currentLocation.longitude}")

                           drawingLocationUI(LatLng(latlngList[i].latitude, latlngList[i].longitude), progressRate)
//                        mapsViewModel.getDangerGrade(latlngList[i].latitude.toString(),
//                                latlngList[i].longitude.toString(),
//                                latlngList[i].latitude.toString(),
//                                latlngList[i].longitude.toString())
                        sleep(300)

                        if (nMap.nMap!!.locationTrackingMode == LocationTrackingMode.Follow ||
                            nMap.nMap!!.locationTrackingMode == LocationTrackingMode.NoFollow) {
                            progressRate = 0.0
                            drawingLocationUI(LatLng(currentLocation.latitude, currentLocation.longitude), progressRate)
                            break
                        }
                    }
                }).start()
            }
            else{
                Log.e("Flag", "${nMap.nMap!!.locationTrackingMode}")
                nMap.nMap!!.locationTrackingMode = LocationTrackingMode.None
            }
            val dangerGradeObserver = Observer<String> {
                dangerInfo.text = it
            }
            mapsViewModel.dangerGrade!!.observe(this, dangerGradeObserver)
        }

        sideNavButton.setOnClickListener {
            new_drawer_layout.openDrawer(Gravity.LEFT)
            mapsViewModel.getUserDataFromServer(mapsViewModel.getUserID())
            subscribeUserData()
        }

        nMap.listener = {
            locationBtn.map = nMap.nMap
            Log.e("메인액티비티","온맵레디")
            nMap.nMap!!.locationSource = locationSource
            nMap.nMap!!.uiSettings.isZoomControlEnabled = false
            nMap.nMap!!.locationTrackingMode = LocationTrackingMode.Follow
            nMap.nMap!!.locationOverlay.isVisible = true
            nMap.nMap!!.addOnLocationChangeListener { location ->
                currentLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                startPosition = "${location.longitude},${location.latitude}"
                Log.e("현재위치", "${location.latitude},${location.longitude}")
                if(goalLatLng != null && wayLatLng != null){
                    Log.e("e", "${goalLatLng},${wayLatLng},${arriveCheck}")
                    when {
                        checkError(currentLatLng, wayLatLng!!) && !arriveCheck -> {
                            makeText(this.context, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                            nMap.markerStartPoint.map = null
                            arriveCheck = true
                        }
                        checkError(currentLatLng, goalLatLng!!) && arriveCheck -> {
                            makeText(this.context, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                            mapsViewModel.insertHistory(mapsViewModel.getUserID(),
                                fullTime,
                                resultSrc,
                                resultDst,
                                resultDistance,
                                resultReward.toString(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                            mapsViewModel.updateCredit(mapsViewModel.getUserID(), resultReward)
                            mapsViewModel.getUserDataFromServer(mapsViewModel.getUserID())
                            nMap.markerWayPoint.map = null
                            nMap.markerGoalPoint.map = null
                            arriveCheck = false

                        }
                    }
                }
            }
        }

        configureBottomNav()
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.new_activity_main,container,false)
        return view
    }


    private fun drawingLocationUI(latLng: LatLng, progressRate: Double) = Thread( Runnable {
        val nMap = mapsViewModel.getMapsRepository()
        val arrStr = resultDistance.split(" Km")
        val distanceDouble = arrStr[0].toDouble() * (1 - progressRate)
        val distanceStr = String.format("%.1f", distanceDouble) + " Km"
        activity!!.runOnUiThread {
            val locationOverlay = nMap.nMap!!.locationOverlay
            locationOverlay.isVisible = true
            locationOverlay.position = latLng
            Log.e("위치변경", "${locationOverlay.position}")
            nMap.nMap!!.moveCamera(CameraUpdate.scrollTo(latLng))

            top_remaining.text = distanceStr
            Log.e("남은거리", distanceStr)
            nMap.pathOverlay.progress = progressRate
            Log.e("progress", "$progressRate")

            when{
                checkError(latLng, wayLatLng!!) && !arriveCheck -> {
                    makeText(context, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                    nMap.markerStartPoint.map = null
                    nMap.markerWayPoint.map = null
                    arriveCheck = true
                }
                checkError(latLng, goalLatLng!!) && arriveCheck -> {
                    makeText(context, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                    mapsViewModel.insertHistory(mapsViewModel.getUserID(),
                            fullTime,
                            resultSrc,
                            resultDst,
                            resultDistance,
                            resultReward.toString(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
                    mapsViewModel.updateCredit(mapsViewModel.getUserID(), resultReward)
                    mapsViewModel.getUserDataFromServer(mapsViewModel.getUserID())
                    nMap.markerGoalPoint.map = null
                    nMap.pathOverlay.map = null
                    arriveCheck = false
                    bottomSheet_before.visibility = View.GONE
                    bottomSheet_after.visibility = View.VISIBLE
                }
            }
        }
    }).start()
    private fun checkError(location: LatLng, goalLatLng: LatLng): Boolean {
        val currentLat = location.latitude
        val currentLng = location.longitude
        val goalLat = goalLatLng.latitude
        val goalLng = goalLatLng.longitude
        return ((currentLat <= goalLat + 0.0001 && currentLat >= goalLat - 0.0001) ||
                (currentLng <= goalLng + 0.0001 && currentLng >= goalLng - 0.0001))
    }
    private fun configureBottomNav(){
        var isRequested = 0
        //하단 슬라이딩 바
        val bottomSheetBehavior_before = BottomSheetBehavior.from(bottomSheet_before)
        val bottomSheetBehavior_after = BottomSheetBehavior.from(bottomSheet_after)
        bottomSheet_before.visibility = View.VISIBLE
        bottomSheet_after.visibility = View.GONE
        bottomSheetBehavior_before.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior_after.state = BottomSheetBehavior.STATE_COLLAPSED
        draw_up_and_refresh.setOnClickListener {
            bottomSheetBehavior_before.state = BottomSheetBehavior.STATE_EXPANDED
        }
        bottomSheetBehavior_before.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //textFull.visibility = View.
                        if(isRequested == 0 && startPosition != null) {
                            mapsViewModel.startFindPath(startPosition!!)
                            subscribe()
                            isRequested++
                        }
                        draw_up_and_refresh.setImageResource(R.drawable.ic_refresh_24)
                        draw_up_and_refresh.setOnClickListener {

                            makeText(context,"의뢰 목록을 새로고침합니다.", LENGTH_SHORT).show()
                            mapsViewModel.clear()
                            mapsViewModel.startFindPath(startPosition!!)
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //textFull.visibility = View.GONE
                        draw_up_and_refresh.setImageResource(R.drawable.ic_up_24)
                        draw_up_and_refresh.setOnClickListener {
                            bottomSheetBehavior_before.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                transitionBottomSheetBackgroundColor(slideOffset)
//                fab.scaleX = interpolateScale(slideOffset, originalScaleX)
//                fab.scaleY = interpolateScale(slideOffset, originalScaleY)
            }
            fun subscribe(){
                val requestListObserver = Observer<ArrayList<RequestListData>> {
                    newRequestList = it
                    recyclerViewSetup()
                }
                mapsViewModel.requests.observe(LifecycleOwner{lifecycle}, requestListObserver)
            }
            fun recyclerViewSetup(){
                adapter = RequestRecyclerAdapter(newRequestList!!)
                requestLayoutManager = LinearLayoutManager(this@MapsActivity.context)
                request_recyclerView.adapter = adapter
                request_recyclerView.layoutManager = requestLayoutManager
                //request_recyclerView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
                Log.e("리사이클러뷰 셋업", "리사이클러뷰 셋업")
                adapter.notifyDataSetChanged()
                if(newRequestList!!.size == 5){
                    adapter.itemClick = object : RequestRecyclerAdapter.OnItemClickListener {
                        override fun onItemClickListener(view: View, position: Int) {
                            //val builder = AlertDialog.Builder(this@MapsActivity)
                            val f = Dialog(this@MapsActivity.context!!)
                            val dialogView = layoutInflater.inflate(R.layout.request_dialog, null)
                            f.setContentView(dialogView)
                            //builder.setView(dialogView)
                            //val yesorno = builder.create()
                            //yesorno.show()
                            f.window!!.setBackgroundDrawableResource(R.drawable.bg_dialog_radius)
                            f.show()
                            dialogView.clockImage.setImageResource(newRequestList!![position].image)
                            dialogView.srcText.text = newRequestList!![position].source
                            dialogView.dstText.text = newRequestList!![position].destination
                            dialogView.creditTextDialog.text = newRequestList!![position].reward.toString()
                            dialogView.time.text = newRequestList!![position].time
                            dialogView.distance.text = newRequestList!![position].distance
                            dialogView.setBackgroundResource(R.color.transparent)
                            dialogView.request_accept_button.setOnClickListener {
                                Log.e("선택한 출발지", newRequestList!![position].source)
                                Log.e("선택한 출발지_코드", newRequestList!![position].sourceCode)
                                Log.e("선택한 도착지", newRequestList!![position].destination)
                                Log.e("선택한 도착지_코드", newRequestList!![position].destinationCode)

                                topSrcBox.text = newRequestList!![position].source
                                topDstBox.text = newRequestList!![position].destination
                                top_remaining.text = newRequestList!![position].distance
                                pathData = newRequestList!![position].responseData
                                setUIPath()

                                isRequested = 0
                                //yesorno.dismiss()
                                f.dismiss()
                                bottomSheetBehavior_before.state = BottomSheetBehavior.STATE_COLLAPSED
                                bottomSheet_before.visibility = View.GONE
                                bottomSheet_after.visibility = View.VISIBLE

                                fullTime = newRequestList!![position].time
                                resultSrc = newRequestList!![position].source
                                resultDst = newRequestList!![position].destination
                                resultDistance = newRequestList!![position].distance
                                resultReward = newRequestList!![position].reward.toDouble()
                                wayLatLng = LatLng(newRequestList!![position].responseData.route.traoptimal[0].summary.waypoints[0].location[1],
                                    newRequestList!![position].responseData.route.traoptimal[0].summary.waypoints[0].location[0])
                                goalLatLng = LatLng(newRequestList!![position].responseData.route.traoptimal[0].summary.goal.location[1],
                                    newRequestList!![position].responseData.route.traoptimal[0].summary.goal.location[0])
                                mapsViewModel.clear()
                            }
                            dialogView.request_cancle_button.setOnClickListener{
                                //yesorno.dismiss()
                                f.dismiss()
                            }
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

                val pathOverlay = mapsViewModel.getMapsPathOverlay()
                val markerStartPoint = mapsViewModel.getMapsMarkerStartPoint()
                val markerWayPoint = mapsViewModel.getMapsMarkerWayPoint()
                val markerGoalPoint = mapsViewModel.getMapsMarkerGoalPoint()

                for(i in pathArr.indices){
                    val path = pathArr[i].toString()
                    val pathLatLng = parsingPath(path)
                    mapsViewModel.latlngList.add(pathLatLng)
                    mapsViewModel.setLatlng()
                }

                pathOverlay.coords = mapsViewModel.latlngList
                pathOverlay.width = 10
                pathOverlay.color = Color.parseColor("#2e58ec")
                pathOverlay.passedColor = Color.GRAY
                pathOverlay.map = mapsViewModel.getMapsRepository().nMap!!
                markerStartPoint.position = LatLng(startLat, startLng)
                markerStartPoint.icon = OverlayImage.fromResource(R.drawable.ic_pin_ar_blue)
                markerStartPoint.map = mapsViewModel.getMapsRepository().nMap!!
                markerWayPoint.position = LatLng(wayPointLat, wayPointLng)
                markerWayPoint.icon = OverlayImage.fromResource(R.drawable.ic_pin_wp_purple)
                markerWayPoint.map = mapsViewModel.getMapsRepository().nMap!!
                markerGoalPoint.position = LatLng(goalLat, goalLng)
                markerGoalPoint.icon = OverlayImage.fromResource(R.drawable.ic_pin_dp_cyan)
                markerGoalPoint.map = mapsViewModel.getMapsRepository().nMap!!
            }

            private fun parsingPath(rawPathData: String): LatLng {
                val arr = rawPathData.split(",")
                val lng: Double = arr[0].substring(1).toDouble()
                val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

                return LatLng(lat, lng)
            }
        })
    }
}


