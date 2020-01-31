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
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.PathData
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.model.repository.MapsRepository
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import com.example.sendymapdemo.viewmodel.MapsViewModel
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_drawer_menu.*
import kotlinx.android.synthetic.main.new_activity_maps.*
import kotlinx.android.synthetic.main.new_nav_header.view.*
import kotlinx.android.synthetic.main.new_request_item.view.clockImage
import kotlinx.android.synthetic.main.new_request_item.view.distance
import kotlinx.android.synthetic.main.new_request_item.view.dstText
import kotlinx.android.synthetic.main.new_request_item.view.srcText
import kotlinx.android.synthetic.main.new_request_item.view.time
import kotlinx.android.synthetic.main.request_dialog.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MapsFragment : Fragment() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    private val mapsViewModel by viewModel<MapsViewModel>()

    private var wayLatLng: LatLng ?= null
    private var goalLatLng: LatLng ?= null
    private var isAccepted: Boolean = false
    private var arriveCheck: Boolean = false
    private var progressRate = 0.0
    private var resultReward:Double = 0.0
    private var isRequested: Int = 0
    private var userData: UserData ?= null
    private lateinit var currentLocation: Location
    private lateinit var nMap: MapsRepository
    private var startPosition: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerView = new_nav_view.getHeaderView(0)
        val locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        val fragmentManager = childFragmentManager
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment?
                ?: MapFragment.newInstance((NaverMapOptions().locationButtonEnabled(true))
                        .also {
                            fragmentManager.beginTransaction().add(R.id.map, map).commit()
                        })

        mapFragment.getMapAsync(nMap)
        configureBottomNav()

        new_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        headerView.historyButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mapsActivity_to_historyActivity)
            new_drawer_layout.closeDrawer(GravityCompat.START)
        }
        headerView.rankingButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mapsActivity_to_rankingActivity)
            new_drawer_layout.closeDrawer(GravityCompat.START)
        }
        headerView.logout_Button.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mapsActivity_to_loginActivity)
            new_drawer_layout.closeDrawer(GravityCompat.START)
        }
        sideNavButton.setOnClickListener {
            new_drawer_layout.openDrawer(Gravity.LEFT)
            mapsViewModel.getUserDataFromServer(userData!!.id)
            subscribeUserData()
        }

        nMap.listener = {
            locationBtn.map = nMap.nMap
            nMap.nMap!!.locationSource = locationSource
            nMap.nMap!!.uiSettings.isZoomControlEnabled = false
            nMap.nMap!!.locationTrackingMode = LocationTrackingMode.Follow
            nMap.nMap!!.locationOverlay.isVisible = true

            nMap.nMap!!.addOnLocationChangeListener { location -> locationChangeListner(location) }
        }
        locationStartBtn.setOnClickListener { locationStartButtonOnClick() }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        nMap = mapsViewModel.getMapsRepository()
        if(isAccepted){
            bottomSheet_before.visibility = View.GONE
            bottomSheet_after.visibility = View.VISIBLE
        }
        return inflater.inflate(R.layout.activity_drawer_menu, container, false)
    }
    private fun locationChangeListner(location: Location){
        currentLocation = location
        val currentLatLng = LatLng(location.latitude, location.longitude)
        startPosition = "${location.longitude},${location.latitude}"
        Log.e("현재위치", "${location.latitude},${location.longitude}")
        if (goalLatLng != null && wayLatLng != null) {
            Log.e("e", "${goalLatLng},${wayLatLng},${arriveCheck}")
            when {
                mapsViewModel.checkError(currentLatLng, wayLatLng!!) && !arriveCheck -> {
                    makeText(this.context, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                    nMap.markerStartPoint.map = null
                    arriveCheck = true
                }
                mapsViewModel.checkError(currentLatLng, goalLatLng!!) && arriveCheck -> {
                    makeText(this.context, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                    mapsViewModel.insertHistory(userData!!.id,
                            fullTime,
                            resultSrc,
                            resultDst,
                            resultDistance,
                            resultReward.toString(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    mapsViewModel.updateCredit(userData!!.id, resultReward)
                    mapsViewModel.getUserDataFromServer(userData!!.id)
                    nMap.markerWayPoint.map = null
                    nMap.markerGoalPoint.map = null
                    arriveCheck = false
                    isAccepted = false
                }
            }
        }
    }
    private fun locationStartButtonOnClick() {
        val latlngList = mapsViewModel.getLatLngList()
        if(nMap.nMap!!.locationTrackingMode == LocationTrackingMode.None) {
            BottomSheetBehavior.from(bottomSheet_after).state = BottomSheetBehavior.STATE_COLLAPSED
            Thread(Runnable{
                for (i in 0 until latlngList.size) {
                    currentLocation.latitude = latlngList[i].latitude
                    currentLocation.longitude = latlngList[i].longitude
                    progressRate = i / latlngList.size.toDouble()

                    activity!!.runOnUiThread { drawingLocationUI(LatLng(latlngList[i].latitude, latlngList[i].longitude), progressRate) }

                    if (nMap.nMap!!.locationTrackingMode == LocationTrackingMode.Follow ||
                            nMap.nMap!!.locationTrackingMode == LocationTrackingMode.NoFollow) {
                        progressRate = 0.0
                        activity!!.runOnUiThread { drawingLocationUI(LatLng(currentLocation.latitude, currentLocation.longitude), progressRate) }
                        break
                    }
                }
            }).start()
        }
        else{
            nMap.nMap!!.locationTrackingMode = LocationTrackingMode.None
        }
        val dangerGradeObserver = Observer<String> {
            dangerInfo.text = it
        }
        mapsViewModel.dangerGrade!!.observe(this, dangerGradeObserver)
    }
    private fun drawingLocationUI(latLng: LatLng, progressRate: Double) {
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
                mapsViewModel.checkError(latLng, wayLatLng!!) && !arriveCheck -> {
                    makeText(this.context, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                    nMap.markerStartPoint.map = null
                    nMap.markerWayPoint.map = null
                    arriveCheck = true
                }
                mapsViewModel.checkError(latLng, goalLatLng!!) && arriveCheck -> {
                    makeText(this.context, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                    mapsViewModel.insertHistory(userData!!.id,
                            fullTime,
                            resultSrc,
                            resultDst,
                            resultDistance,
                            resultReward.toString(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
                    mapsViewModel.updateCredit(userData!!.id, resultReward)
                    mapsViewModel.getUserDataFromServer(userData!!.id)
                    nMap.markerGoalPoint.map = null
                    nMap.pathOverlay.map = null
                    arriveCheck = false
                    isAccepted = false
                    bottomSheet_after.visibility = View.GONE
                    bottomSheet_before.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun configureBottomNav(){
        bottomSheet_before.visibility = View.VISIBLE
        bottomSheet_after.visibility = View.GONE
        BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_COLLAPSED
        BottomSheetBehavior.from(bottomSheet_after).state = BottomSheetBehavior.STATE_COLLAPSED
        draw_up_and_refresh.setOnClickListener {
            BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_EXPANDED
        }
        BottomSheetBehavior.from(bottomSheet_before).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        subscribeRequestList()
                        if(isRequested == 0 && startPosition != null && !isAccepted) {
                            mapsViewModel.startFindPath(startPosition!!)
                            subscribeRequestList()
                            isRequested++
                        }
                        draw_up_and_refresh.setImageResource(R.drawable.ic_refresh_24)
                        draw_up_and_refresh.setOnClickListener {
                            makeText(this@MapsFragment.context,"의뢰 목록을 새로고침합니다.", LENGTH_SHORT).show()
                            mapsViewModel.clear()
                            mapsViewModel.startFindPath(startPosition!!)
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //textFull.visibility = View.GONE
                        draw_up_and_refresh.setImageResource(R.drawable.ic_up_24)
                        draw_up_and_refresh.setOnClickListener {
                            BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_EXPANDED
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
        })
    }
    private fun recyclerViewSetup(requestList : ArrayList<RequestListData>){
        val adapter = RequestRecyclerAdapter(requestList)
        val requestLayoutManager = LinearLayoutManager(this.context)
        request_recyclerView.adapter = adapter
        request_recyclerView.layoutManager = requestLayoutManager
        adapter.notifyDataSetChanged()
        if(requestList.size == 5){
            adapter.itemClick = object : RequestRecyclerAdapter.OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int) {
                    val f = Dialog(this@MapsFragment.context!!)
                    val dialogView = layoutInflater.inflate(R.layout.request_dialog, null)

                    f.setContentView(dialogView)
                    f.window!!.setBackgroundDrawableResource(R.drawable.bg_dialog_radius)
                    f.show()

                    dialogView.clockImage.setImageResource(requestList[position].image)
                    dialogView.srcText.text = requestList[position].source
                    dialogView.dstText.text = requestList[position].destination
                    dialogView.creditTextDialog.text = requestList[position].reward.toString()
                    dialogView.time.text = requestList[position].time
                    dialogView.distance.text = requestList[position].distance
                    dialogView.setBackgroundResource(R.color.transparent)

                    dialogView.request_accept_button.setOnClickListener {
                        topSrcBox.text = requestList[position].source
                        topDstBox.text = requestList[position].destination
                        top_remaining.text = requestList[position].distance
                        pathData = requestList[position].responseData
                        mapsViewModel.setUIPath(position)

                        f.dismiss()
                        BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_COLLAPSED
                        bottomSheet_before.visibility = View.GONE
                        bottomSheet_after.visibility = View.VISIBLE

                        isRequested = 0
                        isAccepted = true

                        fullTime = requestList[position].time
                        resultSrc = requestList[position].source
                        resultDst = requestList[position].destination
                        resultDistance = requestList[position].distance
                        resultReward = requestList[position].reward.toDouble()
                        wayLatLng = LatLng(requestList[position].responseData.route.traoptimal[0].summary.waypoints[0].location[1],
                                requestList[position].responseData.route.traoptimal[0].summary.waypoints[0].location[0])
                        goalLatLng = LatLng(requestList[position].responseData.route.traoptimal[0].summary.goal.location[1],
                                requestList[position].responseData.route.traoptimal[0].summary.goal.location[0])
                        mapsViewModel.clear()
                    }
                    dialogView.request_cancle_button.setOnClickListener{
                        f.dismiss()
                    }
                }
            }
        }
    }

    private fun subscribeUserData(){
        val userDataObserver = Observer<UserData> {
            userData = it
            setUserDataInNav()
        }
        mapsViewModel.userData?.observe(this, userDataObserver)
    }
    private fun subscribeRequestList(){
        val requestListObserver = Observer<ArrayList<RequestListData>> {
            recyclerViewSetup(it)
        }
        mapsViewModel.requests.observe(LifecycleOwner{lifecycle}, requestListObserver)
    }
    private fun setUserDataInNav(){
        Log.e("유저", "$userData")
        new_nav_view.getHeaderView(0).userName.text = userData!!.id
        new_nav_view.getHeaderView(0).userRanking.text = "${userData!!.rank} 등"
        new_nav_view.getHeaderView(0).userCredit_new.text = userData!!.credit
        new_nav_view.getHeaderView(0).userAccum_new.text = userData!!.property
    }
}


