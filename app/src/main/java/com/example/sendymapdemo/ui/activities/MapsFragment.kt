package com.example.sendymapdemo.ui.activities

import android.app.Dialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import java.lang.Thread.sleep

class MapsFragment : Fragment() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    private val mapsViewModel by viewModel<MapsViewModel>()

    private var startLatLng: LatLng ?= null
    private var wayLatLng: LatLng ?= null
    private var goalLatLng: LatLng ?= null

    private var isAccepted: Boolean = false
    private var arriveCheck: Boolean = false

    private var progressRate = 0.0
    private var isRequested: Int = 0

    private var userData: UserData ?= null
    private lateinit var currentLocation: Location
    private lateinit var mockLocation: Location
    private lateinit var nMap: MapsRepository
    private var startPosition: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        nMap = mapsViewModel.getMapsRepository()
        return inflater.inflate(R.layout.activity_drawer_menu, container, false)
    }
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
        userData = mapsViewModel.getUserDataFromRepository()
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
            new_drawer_layout.openDrawer(GravityCompat.START)
            Log.e("userData", "$userData")
            setUserDataInNav()
        }

        nMap.listener = {
            if(isAccepted){
                setTextInDrivingBox(mapsViewModel.requestListData!!)
                subscribeLatLngData()
                drawingPathUI()
            }
            locationBtn.map = nMap.nMap
            nMap.nMap!!.locationSource = locationSource
            nMap.nMap!!.uiSettings.isZoomControlEnabled = false
            nMap.nMap!!.locationTrackingMode = LocationTrackingMode.Follow
            nMap.nMap!!.locationOverlay.isVisible = true

            nMap.nMap!!.addOnLocationChangeListener { location -> locationChangeListener(location) }
        }
        locationStartBtn.setOnClickListener { locationStartButtonOnClick() }
    }
    private fun locationChangeListener(location: Location){
        currentLocation = location
        startPosition = "${location.longitude},${location.latitude}"
        Log.e("현재위치", "$currentLocation")
        if (goalLatLng != null && wayLatLng != null) {
            when {
                mapsViewModel.checkError(location, wayLatLng!!) && !arriveCheck -> {
                    makeText(this.context, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                    mapsViewModel.getStartMarker().map = null
                    arriveCheck = true
                }
                mapsViewModel.checkError(location, goalLatLng!!) && arriveCheck -> {
                    makeText(this.context, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                    mapsViewModel.insertHistory(userData!!.id)
                    mapsViewModel.updateCredit(userData!!.id)
                    mapsViewModel.getUserDataFromRepository()
                    nMap.markerWayPoint.map = null
                    nMap.markerGoalPoint.map = null
                    arriveCheck = false
                    isAccepted = false
                }
            }
        }
    }
    private fun locationStartButtonOnClick() {
        if(nMap.nMap!!.locationTrackingMode == LocationTrackingMode.None) {
            mockLocation = currentLocation
            BottomSheetBehavior.from(bottomSheet_after).state = BottomSheetBehavior.STATE_COLLAPSED
            Thread(Runnable{
                for (i in 0 until mapsViewModel.latlngList.size) {
                    mockLocation.latitude = mapsViewModel.latlngList[i].latitude
                    mockLocation.longitude = mapsViewModel.latlngList[i].longitude
                    progressRate = i / mapsViewModel.latlngList.size.toDouble()

                    activity!!.runOnUiThread { drawingLocationUI(mockLocation, progressRate) }
                    sleep(300)
                    if (nMap.nMap!!.locationTrackingMode == LocationTrackingMode.Follow ||
                            nMap.nMap!!.locationTrackingMode == LocationTrackingMode.NoFollow) {
                        progressRate = 0.0
                        activity!!.runOnUiThread { drawingLocationUI(mockLocation, progressRate) }
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
        mapsViewModel.dangerGrade!!.observe(viewLifecycleOwner, dangerGradeObserver)
    }
    private fun drawingLocationUI(mockLocation: Location, progressRate: Double) {
        val nMap = mapsViewModel.getMapsRepository()
        activity!!.runOnUiThread {
            val locationOverlay = nMap.nMap!!.locationOverlay
            locationOverlay.isVisible = true
            locationOverlay.position = LatLng(mockLocation.latitude, mockLocation.longitude)
            Log.e("위치변경", "${locationOverlay.position}")
            nMap.nMap!!.moveCamera(CameraUpdate.scrollTo(locationOverlay.position))

            top_remaining.text = mapsViewModel.setDistanceInfo(progressRate)
            Log.e("남은거리", mapsViewModel.setDistanceInfo(progressRate))
            nMap.pathOverlay.progress = progressRate
            Log.e("progress", "$progressRate")

            when{
                mapsViewModel.checkError(mockLocation, wayLatLng!!) && !arriveCheck -> {
                    makeText(this.context, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                    nMap.markerStartPoint.map = null
                    nMap.markerWayPoint.map = null
                    arriveCheck = true
                }
                mapsViewModel.checkError(mockLocation, goalLatLng!!) && arriveCheck -> {
                    makeText(this.context, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                    mapsViewModel.insertHistory(userData!!.id)
                    mapsViewModel.updateCredit(userData!!.id)
                    mapsViewModel.getUserDataFromRepository()
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
        if(isAccepted){
            bottomSheet_before.visibility = View.GONE
            bottomSheet_after.visibility = View.VISIBLE
        }
        else {
            bottomSheet_before.visibility = View.VISIBLE
            bottomSheet_after.visibility = View.GONE
        }
        BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_COLLAPSED
        BottomSheetBehavior.from(bottomSheet_after).state = BottomSheetBehavior.STATE_COLLAPSED
        draw_up_and_refresh.setOnClickListener {
            BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_EXPANDED
        }
        BottomSheetBehavior.from(bottomSheet_before).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED) {
                    subscribeRequestListSize()
                    if(isRequested == 0 && !isAccepted) {
                        mapsViewModel.startFindPath("${currentLocation.longitude},${currentLocation.latitude}")
                        subscribeRequestList()
                    }
                    draw_up_and_refresh.setImageResource(R.drawable.ic_refresh_24)
                }
                else if(newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    //textFull.visibility = View.GONE
                    draw_up_and_refresh.setImageResource(R.drawable.ic_up_24)
                    draw_up_and_refresh.setOnClickListener {
                        BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
        })
    }
    private fun recyclerViewSetup(requestList : ArrayList<RequestListData>){
        val adapter = RequestRecyclerAdapter(requestList)
        request_recyclerView.adapter = adapter
        request_recyclerView.layoutManager = LinearLayoutManager(this.context)
        adapter.notifyDataSetChanged()
        if(requestList.size == 5){
            adapter.itemClick = object : RequestRecyclerAdapter.OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int) {
                    Log.e("itemClickListener", "$position")
                    val dialog = Dialog(this@MapsFragment.context!!)
                    val dialogView = layoutInflater.inflate(R.layout.request_dialog, null)

                    dialog.setContentView(dialogView)
                    dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_dialog_radius)
                    dialog.show()

                    dialogView.clockImage.setImageResource(requestList[position].image)
                    dialogView.srcText.text = requestList[position].source
                    dialogView.dstText.text = requestList[position].destination
                    dialogView.creditTextDialog.text = requestList[position].reward.toString()
                    dialogView.time.text = requestList[position].time
                    dialogView.distance.text = requestList[position].distance
                    dialogView.setBackgroundResource(R.color.transparent)

                    dialogView.request_accept_button.setOnClickListener {
                        mapsViewModel.setPathData(position)
                        setTextInDrivingBox(requestList[position])
                        subscribeLatLngData()
                        drawingPathUI()

                        dialog.dismiss()
                        BottomSheetBehavior.from(bottomSheet_before).state = BottomSheetBehavior.STATE_COLLAPSED
                        bottomSheet_before.visibility = View.GONE
                        bottomSheet_after.visibility = View.VISIBLE

                        isRequested = 0
                        isAccepted = true

                        mapsViewModel.setRequestData(position)
                        mapsViewModel.requestListClear()
                    }
                    dialogView.request_cancle_button.setOnClickListener{
                        dialog.dismiss()
                    }
                }
            }
        }
    }
    private fun setTextInDrivingBox(requestListData: RequestListData) {
        topSrcBox.text = requestListData.source
        topDstBox.text = requestListData.destination
        top_remaining.text = requestListData.distance
    }
    private fun drawingPathUI() {
        Log.e("길그리기", "drawing")
        mapsViewModel.getPathOverlay().coords = mapsViewModel.latlngList
        mapsViewModel.getPathOverlay().width = 10
        mapsViewModel.getPathOverlay().color = Color.parseColor("#2e58ec")
        mapsViewModel.getPathOverlay().passedColor = Color.GRAY
        mapsViewModel.getPathOverlay().map = mapsViewModel.getMapsRepository().nMap!!
    }
    private fun subscribeLatLngData() {
        val startLatLngObserver = Observer<LatLng> {
            startLatLng = it
            mapsViewModel.getStartMarker().position = it
            mapsViewModel.getStartMarker().icon = OverlayImage.fromResource(R.drawable.ic_pin_ar_blue)
            mapsViewModel.getStartMarker().map = mapsViewModel.getMapsRepository().nMap
        }
        val wayLatLngObserver = Observer<LatLng> {
            wayLatLng = it
            mapsViewModel.getWayMarker().position = it
            mapsViewModel.getWayMarker().icon = OverlayImage.fromResource(R.drawable.ic_pin_wp_purple)
            mapsViewModel.getWayMarker().map = mapsViewModel.getMapsRepository().nMap
        }
        val goalLatLngObserver = Observer<LatLng> {
            goalLatLng = it
            mapsViewModel.getGoalMarker().position = it
            mapsViewModel.getGoalMarker().icon = OverlayImage.fromResource(R.drawable.ic_pin_dp_cyan)
            mapsViewModel.getGoalMarker().map = mapsViewModel.getMapsRepository().nMap
        }
        mapsViewModel.liveStartLatLng?.observe(viewLifecycleOwner, startLatLngObserver)
        mapsViewModel.liveWayLatLng?.observe(viewLifecycleOwner, wayLatLngObserver)
        mapsViewModel.liveGoalLatLng?.observe(viewLifecycleOwner, goalLatLngObserver)
    }
    private fun subscribeRequestList() {
        mapsViewModel.requests.observe(LifecycleOwner{lifecycle}, Observer { requestListObserver ->
            recyclerViewSetup(requestListObserver)
        })
    }
    private fun subscribeRequestListSize() {
        val startPosition = "${currentLocation.longitude},${currentLocation.latitude}"
        mapsViewModel.requestListSize.observe(this, Observer { requestListSize ->
            isRequested = requestListSize
            if(isRequested == 4) draw_up_and_refresh.setOnClickListener { mapsViewModel.startFindPath(startPosition) }
            else draw_up_and_refresh.setOnClickListener(null)
        })
    }
    private fun setUserDataInNav() {
        Log.e("유저", "$userData")
        new_nav_view.getHeaderView(0).userName.text = userData!!.id
        new_nav_view.getHeaderView(0).userRanking.text = "${userData!!.rank} 등"
        new_nav_view.getHeaderView(0).userCredit_new.text = userData!!.credit
        new_nav_view.getHeaderView(0).userAccum_new.text = userData!!.property
    }
}


