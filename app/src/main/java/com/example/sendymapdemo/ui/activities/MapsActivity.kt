package com.example.sendymapdemo.ui.activities

import android.app.Activity
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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.content.Intent
import android.os.Handler
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.koinmodule.ApplicationMain
import com.example.sendymapdemo.viewmodel.MapsViewModel
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.requestDst
import kotlinx.android.synthetic.main.activity_maps.requestSrc
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Runnable
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MapsActivity : AppCompatActivity(){
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

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

    private lateinit var startPosition: String
    private lateinit var resultDistance: String
    var progressRate = 0.0
    var resultReward:Double = 0.0

    override fun onBackPressed() {
        onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //네이게이션 뷰의 헤더에 접근하기 위한 코드
        val headerView = nav_view.getHeaderView(0)

        val nMap = mapsViewModel.getNaverMapRepository()
        val latlngList = mapsViewModel.getLatLngList()

        drawerLayout = findViewById(R.id.drawer_layout)
        nav_view.setNavigationItemSelectedListener { menuitem: MenuItem ->
            when (menuitem.itemId) {
                R.id.menu_history -> {
                    val historyIntent = Intent(this, HistoryActivity::class.java)
                    startActivity(historyIntent)
                }
                R.id.menu_ranking -> {
                    val rankIntent = Intent(this, RankingActivity::class.java)
                    startActivity(rankIntent)
                }
                R.id.menu_about -> {
                    val builder = AlertDialog.Builder(this)
                    val dialogView = layoutInflater.inflate(R.layout.about, null)
                    builder.setView(dialogView).show()
                }
                R.id.menu_update -> {

                }
                R.id.menu_logout -> {
                    val logout = Intent(this, LoginActivity::class.java)
                    startActivity(logout)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        val userID = mapsViewModel.getUserID()
        var userData: UserData
        val r = Runnable {
            userData = mapsViewModel.getFromRoom(userID)
            Log.e("유저", "$userID,$userData")
            Runnable {
                headerView.userID.text = userData.id
                headerView.userRank.text = userData.rank
                headerView.userCredit.text = userData.credit
                headerView.userAccum.text = userData.property
            }.run()
        }
        val thread = Thread(r)
        thread.start()

        configureBottomNav()

        setSupportActionBar(toolbar)
        //사이드바 토글 생성
        val toggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle(this,  drawerLayout,toolbar,0,0){
            override fun onDrawerClosed(view:View){
                super.onDrawerClosed(view)
                Log.e("닫힘","드로워")
            }

            override fun onDrawerOpened(drawerView: View){
                super.onDrawerOpened(drawerView)
                Log.e("열림","드로워")
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                Log.e("열리는 중","드로워")
            }
        }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

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

        startDelivery.setOnClickListener {
            //첫번째 버튼 클릭했을때
            val requestIntent = Intent(this, RequestActivity::class.java)
            requestIntent.putExtra("startPoint",startPosition)
            startActivityForResult(requestIntent,100)

            animation()
        }

        nMap.listener = {
            Log.e("메인액티비티","온맵레디")
            nMap.nMap!!.locationSource = locationSource
            nMap.nMap!!.locationTrackingMode = LocationTrackingMode.Follow
            nMap.nMap!!.locationOverlay.isVisible = true
            nMap.nMap!!.uiSettings.isLocationButtonEnabled = true
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
            locationStartBtn.setOnClickListener {
                if(nMap.nMap!!.locationTrackingMode == LocationTrackingMode.None){
                    Log.e("Flag", "${nMap.nMap!!.locationTrackingMode}")
                    Thread(Runnable {
                        for (i in 0 until latlngList.size) {
                            currentLocation.latitude = latlngList[i].latitude
                            currentLocation.longitude = latlngList[i].longitude

                            Log.e("위치변경", "${currentLocation.latitude}, ${currentLocation.longitude}")
                            drawingLocationUI(LatLng(latlngList[i].latitude, latlngList[i].longitude))
                            mapsViewModel.getDangerGrade(latlngList[i].latitude.toString(),
                                    latlngList[i].longitude.toString(),
                                    latlngList[i].latitude.toString(),
                                    latlngList[i].longitude.toString())
                            sleep(2000)

                            if (nMap.nMap!!.locationTrackingMode == LocationTrackingMode.Follow ||
                                    nMap.nMap!!.locationTrackingMode == LocationTrackingMode.NoFollow) {
                                progressRate = 0.0
                                drawingLocationUI(LatLng(currentLocation.latitude, currentLocation.longitude))
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
                remainDuration.text = distanceStr
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
    private fun animation(){
        if(isFabOpen){
            startDelivery.startAnimation(fabClose)
            market.startAnimation(fabClose)
            startDelivery.isClickable = false
            market.isClickable = false
            isFabOpen = false
        }
        else{
            startDelivery.startAnimation(fabOpen)
            market.startAnimation(fabOpen)
            startDelivery.isClickable = true
            market.isClickable = true
            isFabOpen = true
        }
    }
    private fun checkError(location: LatLng, goalLatLng: LatLng): Boolean {
        val currentLat = location.latitude
        val currentLng = location.longitude
        val goalLat = goalLatLng.latitude
        val goalLng = goalLatLng.longitude
        return ((currentLat <= goalLat + 0.0001 && currentLat >= goalLat - 0.0001) ||
                (currentLng <= goalLng + 0.0001 && currentLng >= goalLng - 0.0001))
    }
    private fun configureBottomNav(){
        //하단 슬라이딩 바
        val originalScaleX = fab.scaleX
        val originalScaleY = fab.scaleY
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
       bottomSheet.visibility = View.GONE
       bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //textFull.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //textFull.visibility = View.GONE
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
                transitionBottomSheetBackgroundColor(slideOffset)
                fab.scaleX = interpolateScale(slideOffset, originalScaleX)
                fab.scaleY = interpolateScale(slideOffset, originalScaleY)
            }

            private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
                bottomSheet.setCardBackgroundColor(
                    interpolateColor(slideOffset,
                        Color.argb(255, 223, 221, 255),
                        getColor(R.color.colorPrimaryDark))
                )
                textRemain.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                requestSrc.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                requestDst.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                remainDurationText.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                remainDuration.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dustInfoText.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dustInfo.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dangerInfoText.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dangerInfo.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                arrow.setTextColor(
                    interpolateColor(slideOffset,
                        getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
            }

            private fun interpolateColor(
                fraction: Float,
                startValue: Int,
                endValue: Int
            ): Int {
                val startA = startValue shr 24 and 0xff
                val startR = startValue shr 16 and 0xff
                val startG = startValue shr 8 and 0xff
                val startB = startValue and 0xff
                val endA = endValue shr 24 and 0xff
                val endR = endValue shr 16 and 0xff
                val endG = endValue shr 8 and 0xff
                val endB = endValue and 0xff
                return startA + (fraction * (endA - startA)).toInt() shl 24 or
                        (startR + (fraction * (endR - startR)).toInt() shl 16) or
                        (startG + (fraction * (endG - startG)).toInt() shl 8) or
                        startB + (fraction * (endB - startB)).toInt()
            }

            private fun interpolateScale(
                fraction: Float,
                original : Float
                ): Float{
                return original * (1-fraction)
            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            100 -> {
                when(resultCode){
                    Activity.RESULT_OK ->{
                        resultSrc = data!!.getStringExtra("resultSrc")!!
                        resultDst = data.getStringExtra("resultDst")!!
                        resultDistance = data.getStringExtra("resultDistance")!!
                        wayLatLng=LatLng(data.getDoubleExtra("wayLatLng[0]",0.0),data.getDoubleExtra("wayLatLng[1]",0.0))
                        goalLatLng=LatLng(data.getDoubleExtra("goalLatLng[0]",0.0),data.getDoubleExtra("goalLatLng[1]",0.0))
                        resultReward=data.getDoubleExtra("resultReward",0.0)
                        fullTime=data.getStringExtra("fullTime")!!

                        requestSrc.text = resultSrc
                        requestDst.text = resultDst
                        remainDuration.text = resultDistance
                        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                        bottomSheet.visibility = View.VISIBLE
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        val animation = AlphaAnimation(0.0f,1.0f)
                        animation.setAnimationListener(object : Animation.AnimationListener{
                            override fun onAnimationRepeat(animation: Animation?) {
                            }
                            override fun onAnimationEnd(animation: Animation?) {
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            }

                            override fun onAnimationStart(animation: Animation?) {
                            }
                        })
                        animation.duration = 1500
                        bottomSheet.animation = animation
                    }
                }
            }
        }
    }
    fun fabClickListener(view: View){
        view.bringToFront()
        animation()
    }
}


