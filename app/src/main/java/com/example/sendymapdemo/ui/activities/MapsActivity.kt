package com.example.sendymapdemo.ui.activities

import android.app.AlertDialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.service.autofill.CustomDescription
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.PathData
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.koinmodule.ApplicationMain
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import com.example.sendymapdemo.viewmodel.MapsViewModel
import com.example.sendymapdemo.viewmodel.RequestViewModel
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.android.synthetic.main.new_activity_main.*
import kotlinx.android.synthetic.main.new_activity_maps.*
import kotlinx.android.synthetic.main.new_activity_maps.request_recyclerView
import kotlinx.android.synthetic.main.new_nav_header.view.*
import kotlinx.android.synthetic.main.request_activity.*
import kotlinx.android.synthetic.main.request_dialog.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Runnable
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MapsActivity : AppCompatActivity(){
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    private lateinit var adapter: RequestRecyclerAdapter
    private lateinit var requestLayoutManager: LinearLayoutManager

    private val requestViewModel by viewModel<RequestViewModel>()

    private lateinit var pathData: PathData
    private var newRequestList : ArrayList<RequestListData>? = null




    private val mapsViewModel by viewModel<MapsViewModel>()

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var resultSrc : String
    private lateinit var resultDst : String
    private lateinit var fullTime : String
    var wayLatLng: LatLng ?= null
    var goalLatLng: LatLng ?= null
    private lateinit var locationSource: LocationSource
    private lateinit var currentLocation: Location

    private var isFabOpen: Boolean = false
    private var arriveCheck: Boolean = false

    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    private var startPosition: String? = null
    private lateinit var resultDistance: String
    var progressRate = 0.0
    var resultReward:Double = 0.0

    override fun onBackPressed() {
        onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_main)

        //네이게이션 뷰의 헤더에 접근하기 위한 코드
        val headerView = new_nav_view.getHeaderView(0)

        val nMap = mapsViewModel.getNaverMapRepository()
        val latlngList = mapsViewModel.getLatLngList()

//        drawerLayout = findViewById(R.id.new_drawer_layout)
//        nav_view.setNavigationItemSelectedListener { menuitem: MenuItem ->
//            when (menuitem.itemId) {
//                R.id.menu_history -> {
//                    val historyIntent = Intent(this, HistoryActivity::class.java)
//                    startActivity(historyIntent)
//                }
//                R.id.menu_ranking -> {
//                    val rankIntent = Intent(this, RankingActivity::class.java)
//                    startActivity(rankIntent)
//                }
//                R.id.menu_about -> {
//                    val builder = AlertDialog.Builder(this)
//                    val dialogView = layoutInflater.inflate(R.layout.about, null)
//                    builder.setView(dialogView).show()
//                }
//                R.id.menu_update -> {
//
//                }
//                R.id.menu_logout -> {
//                    val logout = Intent(this, LoginActivity::class.java)
//                    startActivity(logout)
//                }
//            }
//            drawerLayout.closeDrawer(GravityCompat.START)
//            true
//        }
        val userID = mapsViewModel.getUserID()
        var userData: UserData
        val r = Runnable {
            userData = mapsViewModel.getFromRoom(userID)
            Log.e("유저", "$userID,$userData")
            Runnable {
                headerView.userName.text = userData.id
                headerView.userRanking.text = userData.rank + "등"
                headerView.userCredit_new.text = userData.credit
                headerView.userAccum_new.text = userData.property
            }.run()
        }
        val thread = Thread(r)
        thread.start()

        configureBottomNav()

//        setSupportActionBar(toolbar)
//        //사이드바 토글 생성
//        val toggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle(this,  drawerLayout,toolbar,0,0){
//            override fun onDrawerClosed(view:View){
//                super.onDrawerClosed(view)
//                Log.e("닫힘","드로워")
//            }
//
//            override fun onDrawerOpened(drawerView: View){
//                super.onDrawerOpened(drawerView)
//                Log.e("열림","드로워")
//            }
//
//            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//                super.onDrawerSlide(drawerView, slideOffset)
//                Log.e("열리는 중","드로워")
//            }
//        }
//        drawer_layout.addDrawerListener(toggle)
//        toggle.syncState()

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        fabOpen = AnimationUtils.loadAnimation(ApplicationMain.instance.Context(), R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(ApplicationMain.instance.Context(), R.anim.fab_close)

        val fragmentManager = supportFragmentManager
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment?
                ?: MapFragment.newInstance((NaverMapOptions().locationButtonEnabled(true))
                        .also {
                            fragmentManager.beginTransaction().add(R.id.map, map).commit()
                        })
        mapFragment.getMapAsync(nMap)

//        startDelivery.setOnClickListener {
//            //첫번째 버튼 클릭했을때
//            val requestIntent = Intent(this, RequestActivity::class.java)
//            requestIntent.putExtra("startPoint",startPosition)
//            startActivityForResult(requestIntent,100)
//            animation()
//        }


        nMap.listener = {
            locationBtn.map = nMap.nMap
            Log.e("메인액티비티","온맵레디")
            nMap.nMap!!.locationSource = locationSource
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
                            makeText(this, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                            nMap.markerStartPoint.map = null
                            arriveCheck = true
                        }
                        checkError(currentLatLng, goalLatLng!!) && arriveCheck -> {
                            makeText(this, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                            mapsViewModel.insertHistory(mapsViewModel.getUserID(),
                                fullTime,
                                resultSrc,
                                resultDst,
                                resultDistance,
                                resultReward.toString(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
                            mapsViewModel.updateCredit(mapsViewModel.getUserID(), resultReward)
                            nMap.markerWayPoint.map = null
                            nMap.markerGoalPoint.map = null
                            arriveCheck = false

                        }
                    }
                }
            }
//            locationStartBtn.setOnClickListener {
//                if(nMap.nMap!!.locationTrackingMode == LocationTrackingMode.None){
//                    Log.e("Flag", "${nMap.nMap!!.locationTrackingMode}")
//                    Thread(Runnable {
//                        for (i in 0 until latlngList.size) {
//                            currentLocation.latitude = latlngList[i].latitude
//                            currentLocation.longitude = latlngList[i].longitude
//
//                            Log.e("위치변경", "${currentLocation.latitude}, ${currentLocation.longitude}")
//                            drawingLocationUI(LatLng(latlngList[i].latitude, latlngList[i].longitude))
//                            mapsViewModel.getDangerGrade(latlngList[i].latitude.toString(),
//                                    latlngList[i].longitude.toString(),
//                                    latlngList[i].latitude.toString(),
//                                    latlngList[i].longitude.toString())
//                            sleep(2000)
//
//                            if (nMap.nMap!!.locationTrackingMode == LocationTrackingMode.Follow ||
//                                    nMap.nMap!!.locationTrackingMode == LocationTrackingMode.NoFollow) {
//                                progressRate = 0.0
//                                drawingLocationUI(LatLng(currentLocation.latitude, currentLocation.longitude))
//                                break
//                            }
//                        }
//                    }).start()
//                }
//                else{
//                    Log.e("Flag", "${nMap.nMap!!.locationTrackingMode}")
//                    nMap.nMap!!.locationTrackingMode = LocationTrackingMode.None
//                }
//                val dangerGradeObserver = Observer<String> {
//                    dangerInfo.text = it
//                }
//                mapsViewModel.dangerGrade!!.observe(this, dangerGradeObserver)
//            }
        }
    }
    private fun drawingLocationUI(latLng: LatLng) = Thread(Runnable{
        val nMap = mapsViewModel.getNaverMapRepository()
        val latlngList = mapsViewModel.getLatLngList()
        nMap.nMap!!.let {
            val locationOverlay = it.locationOverlay
            progressRate += 1.0 / latlngList.size
            locationOverlay.isVisible = true
            locationOverlay.position = latLng
            Log.e("위치변경", "${locationOverlay.position}")
            it.moveCamera(CameraUpdate.scrollTo(latLng))
            val handler = Handler()
            handler.postDelayed({
                val arrStr = resultDistance.split(" Km")
                val distanceDouble = arrStr[0].toDouble() * (1 - progressRate)
                val distanceStr = String.format("%.1f", distanceDouble) + " Km"
                Log.e("남은거리", distanceStr)
                top_remaining.text = distanceStr
                nMap.pathOverlay.progress = progressRate
                Log.e("progress", "$progressRate")
                when{
                    checkError(latLng, wayLatLng!!) && !arriveCheck -> {
                        makeText(applicationContext, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                        nMap.markerStartPoint.map = null
                        nMap.markerWayPoint.map = null
                        arriveCheck = true
                    }
                    checkError(latLng, goalLatLng!!) && arriveCheck -> {
                        makeText(applicationContext, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                        mapsViewModel.insertHistory(mapsViewModel.getUserID(),
                                fullTime,
                                resultSrc,
                                resultDst,
                                resultDistance,
                                resultReward.toString(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("h시 mm분 ss초")),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")))
                        mapsViewModel.updateCredit(mapsViewModel.getUserID(), resultReward)
                        nMap.markerGoalPoint.map = null
                        nMap.pathOverlay.map = null
                        arriveCheck = false
                    }}
            },1000)
        }
    }).start()
//    private fun animation(){
//        if(isFabOpen){
//            startDelivery.startAnimation(fabClose)
//            market.startAnimation(fabClose)
//            startDelivery.isClickable = false
//            market.isClickable = false
//            isFabOpen = false
//        }
//        else{
//            startDelivery.startAnimation(fabOpen)
//            market.startAnimation(fabOpen)
//            startDelivery.isClickable = true
//            market.isClickable = true
//            isFabOpen = true
//        }
//    }
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
//        val originalScaleX = fab.scaleX
//        val originalScaleY = fab.scaleY
        val bottomSheetBehavior_before = BottomSheetBehavior.from(bottomSheet_before)
        val bottomSheetBehavior_after = BottomSheetBehavior.from(bottomSheet_after)
        bottomSheet_before.visibility = View.VISIBLE
        bottomSheet_after.visibility = View.GONE
        bottomSheetBehavior_before.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior_after.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior_before.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //textFull.visibility = View.
                        if(isRequested == 0 && startPosition != null) {
                            requestViewModel.startFindPath(startPosition!!)
                            subscribe()
                            isRequested++
                        }
                        draw_up_and_refresh.setImageResource(R.drawable.ic_refresh_24)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //textFull.visibility = View.GONE
                        draw_up_and_refresh.setImageResource(R.drawable.ic_up_24)
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
                requestViewModel.requests.observe(LifecycleOwner{lifecycle}, requestListObserver)
            }
            fun recyclerViewSetup(){
                adapter = RequestRecyclerAdapter(newRequestList!!)
                requestLayoutManager = LinearLayoutManager(applicationContext)
                request_recyclerView.adapter = adapter
                request_recyclerView.layoutManager = requestLayoutManager
                //request_recyclerView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
                Log.e("리사이클러뷰 셋업", "리사이클러뷰 셋업")
                adapter.notifyDataSetChanged()
                if(newRequestList!!.size == 5){
                    adapter.itemClick = object : RequestRecyclerAdapter.OnItemClickListener {
                        override fun onItemClickListener(view: View, position: Int) {
                            val builder = AlertDialog.Builder(this@MapsActivity)
                            val dialogView = layoutInflater.inflate(R.layout.request_dialog, null)
                            builder.setView(dialogView)
                            val yesorno = builder.create()
                            yesorno.show()
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

                                requestViewModel.clear()
                                isRequested = 0
                                yesorno.dismiss()
                                bottomSheetBehavior_before.state = BottomSheetBehavior.STATE_COLLAPSED
                                bottomSheet_before.visibility = View.GONE
                                bottomSheet_after.visibility = View.VISIBLE
                            }
                            dialogView.request_cancle_button.setOnClickListener{
                                yesorno.dismiss()
                            }
                            //val currentList = requestRepository.getList()
//                            val oDialog = AlertDialog.Builder(view.context, android.R.style.Theme_DeviceDefault_Light_Dialog)
//                            oDialog.setMessage("수락시 의뢰 리스트가 초기화됩니다.").setTitle("해당 의뢰를 수락하시겠습니까?")
//                                .setPositiveButton("아니오") { _, _ ->
//                                    makeText(view.context, "취소", Toast.LENGTH_LONG).show()
//                                }
//                                .setNeutralButton("예") { _, _ ->
//                                    Log.e("선택한 출발지", newRequestList!![position].source)
//                                    Log.e("선택한 출발지_코드", newRequestList!![position].sourceCode)
//                                    Log.e("선택한 도착지", newRequestList!![position].destination)
//                                    Log.e("선택한 도착지_코드", newRequestList!![position].destinationCode)
//
//                                    pathData = newRequestList!![position].responseData
//                                    setUIPath()
//
//                                    val arrWay = newRequestList!![position].sourceCode.split(",")
//                                    val arrGoal = newRequestList!![position].destinationCode.split(",")
//
//                                    intent.putExtra("resultSrc", newRequestList!![position].source)
//                                    intent.putExtra("resultDst", newRequestList!![position].destination)
//                                    intent.putExtra("resultDistance", newRequestList!![position].distance)
//                                    intent.putExtra("fullTime",newRequestList!![position].time)
//                                    intent.putExtra("wayLatLng[0]", arrWay[1].toDouble())
//                                    intent.putExtra("wayLatLng[1]", arrWay[0].toDouble())
//                                    intent.putExtra("goalLatLng[0]", arrGoal[1].toDouble())
//                                    intent.putExtra("goalLatLng[1]", arrGoal[0].toDouble())
//
//                                    setResult(Activity.RESULT_OK, intent)
//                                    requestViewModel.clear()
//                                    finish()
//                                }
//                                .setCancelable(false).show()
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

                requestViewModel.mapsRepository.pathOverlay.coords = requestViewModel.latlngList
                requestViewModel.mapsRepository.pathOverlay.width = 30
                requestViewModel.mapsRepository.pathOverlay.color = Color.BLUE
                requestViewModel.mapsRepository.pathOverlay.patternImage = OverlayImage.fromResource(R.drawable.path_pattern)
                requestViewModel.mapsRepository.pathOverlay.patternInterval = 50
                requestViewModel.mapsRepository.pathOverlay.passedColor = Color.GRAY
                requestViewModel.mapsRepository.markerStartPoint.position = LatLng(startLat, startLng)
                requestViewModel.mapsRepository.markerWayPoint.position = LatLng(wayPointLat, wayPointLng)
                requestViewModel.mapsRepository.markerGoalPoint.position = LatLng(goalLat, goalLng)
                requestViewModel.mapsRepository.markerStartPoint.iconTintColor = Color.BLUE
                requestViewModel.mapsRepository.markerWayPoint.iconTintColor = Color.GREEN
                requestViewModel.mapsRepository.markerGoalPoint.iconTintColor = Color.RED
                requestViewModel.mapsRepository.markerStartPoint.map = requestViewModel.mapsRepository.nMap!!
                requestViewModel.mapsRepository.markerWayPoint.map = requestViewModel.mapsRepository.nMap!!
                requestViewModel.mapsRepository.markerGoalPoint.map = requestViewModel.mapsRepository.nMap!!
                requestViewModel.mapsRepository.pathOverlay.map = requestViewModel.mapsRepository.nMap!!
            }

            private fun parsingPath(rawPathData: String): LatLng {
                val arr = rawPathData.split(",")
                val lng: Double = arr[0].substring(1).toDouble()
                val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

                return LatLng(lat, lng)
            }
//            private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
//                bottomSheet.setCardBackgroundColor(
//                    interpolateColor(slideOffset,
//                        Color.argb(255, 223, 221, 255),
//                        getColor(R.color.colorPrimaryDark))
//                )
//                textRemain.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                requestSrc.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                requestDst.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                remainDurationText.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                remainDuration.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                dustInfoText.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                dustInfo.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                dangerInfoText.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                dangerInfo.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//                arrow.setTextColor(
//                    interpolateColor(slideOffset,
//                        getColor(R.color.colorPrimaryDark),
//                        Color.argb(255, 223, 221, 255))
//                )
//            }
//
//            private fun interpolateColor(
//                fraction: Float,
//                startValue: Int,
//                endValue: Int
//            ): Int {
//                val startA = startValue shr 24 and 0xff
//                val startR = startValue shr 16 and 0xff
//                val startG = startValue shr 8 and 0xff
//                val startB = startValue and 0xff
//                val endA = endValue shr 24 and 0xff
//                val endR = endValue shr 16 and 0xff
//                val endG = endValue shr 8 and 0xff
//                val endB = endValue and 0xff
//                return startA + (fraction * (endA - startA)).toInt() shl 24 or
//                        (startR + (fraction * (endR - startR)).toInt() shl 16) or
//                        (startG + (fraction * (endG - startG)).toInt() shl 8) or
//                        startB + (fraction * (endB - startB)).toInt()
//            }
//
//            private fun interpolateScale(
//                fraction: Float,
//                original : Float
//                ): Float{
//                return original * (1-fraction)
//            }
//        })
    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when(requestCode){
//            100 -> {
//                when(resultCode){
//                    Activity.RESULT_OK ->{
//                        resultSrc = data!!.getStringExtra("resultSrc")!!
//                        resultDst = data.getStringExtra("resultDst")!!
//                        resultDistance = data.getStringExtra("resultDistance")!!
//                        wayLatLng=LatLng(data.getDoubleExtra("wayLatLng[0]",0.0),data.getDoubleExtra("wayLatLng[1]",0.0))
//                        goalLatLng=LatLng(data.getDoubleExtra("goalLatLng[0]",0.0),data.getDoubleExtra("goalLatLng[1]",0.0))
//                        resultReward=data.getDoubleExtra("resultReward",0.0)
//                        fullTime=data.getStringExtra("fullTime")!!
//
//                        requestSrc.text = resultSrc
//                        requestDst.text = resultDst
//                        remainDuration.text = resultDistance
//                        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
//                        bottomSheet.visibility = View.VISIBLE
//                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//                        val animation = AlphaAnimation(0.0f,1.0f)
//                        animation.setAnimationListener(object : Animation.AnimationListener{
//                            override fun onAnimationRepeat(animation: Animation?) {
//                            }
//                            override fun onAnimationEnd(animation: Animation?) {
//                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                            }
//
//                            override fun onAnimationStart(animation: Animation?) {
//                            }
//                        })
//                        animation.duration = 1500
//                        bottomSheet.animation = animation
//                    }
//                }
//            }
//        }
//    }
//    fun fabClickListener(view: View){
//        view.bringToFront()
//        animation()
//    }
        )
    }
}


