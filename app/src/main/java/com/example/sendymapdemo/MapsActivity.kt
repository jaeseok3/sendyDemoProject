package com.example.sendymapdemo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
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
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.requestDst
import kotlinx.android.synthetic.main.activity_maps.requestSrc

//leaderBoardAdapter에서 드로워를 닫을 때 필요해서 전역으로 선언
lateinit var drawerLayout: DrawerLayout
//리더보드 어댑터
lateinit var boardAdapter:leaderBoardAdapter
//유저들의 정보를 담은 리스트
var userList = ArrayList<userInfo>()
//의뢰정보를 담은 리스트
var requestList = ArrayList<requestInfo>()
var positions=ArrayList<String>()

//리더보드 레이아웃 매니저
lateinit var layoutManager: LinearLayoutManager
lateinit var headerName: TextView
lateinit var headerDesc: TextView
lateinit var headerRank: TextView
lateinit var headerCredit: TextView
lateinit var headerAccum: TextView

var pathOverlay = PathOverlay()
var markerStartPoint = Marker()
var markerWayPoint = Marker()
var markerGoalPoint = Marker()

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    private var responseData: PathData ?= null
    private var responseList = ArrayList<SummaryData>()

    private var wayLatLng: LatLng ?= null
    private var goalLatLng: LatLng ?= null

    private lateinit var locationSource: FusedLocationSource
    private lateinit var currentLocation: Location

    private var isFabOpen: Boolean = false
    private var arriveCheck: Boolean = false

    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    private lateinit var startPosition: String

    private lateinit var nMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //네이게이션 뷰의 헤더에 접근하기 위한 코드
        val navigationHeader = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationHeader.getHeaderView(0)
        drawerLayout = findViewById(R.id.drawer_layout)
        headerName = headerView.findViewById(R.id.userID)
        headerDesc = headerView.findViewById(R.id.userDescription)
        headerRank = headerView.findViewById(R.id.userRank)
        headerCredit = headerView.findViewById(R.id.userCredit)
        headerAccum = headerView.findViewById(R.id.userAccum)

        configureBottomNav()

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        //사이드바 토글 생성
//        var toggle:ActionBarDrawerToggle
        val toggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle(this,  drawerLayout,toolbar,0,0){
            override fun onDrawerClosed(view:View){
                super.onDrawerClosed(view)
                Log.e("닫힘","드로워")

            }

            override fun onDrawerOpened(drawerView: View){
                super.onDrawerOpened(drawerView)
                Log.e("열림","드로워")
                login(userIdentity)
                recyclerList.adapter = boardAdapter
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                Log.e("열리는 중","드로워")
                //리더보드 어댑터 초기화

            }
        }

        boardAdapter = leaderBoardAdapter(userList)
        //리더보드 레이아웃 매니저
        layoutManager = LinearLayoutManager(applicationContext)

        recyclerList.adapter = boardAdapter
        recyclerList.layoutManager = layoutManager
        recyclerList.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()



        val intent = Intent(applicationContext,LoginActivity::class.java)
        startActivity(intent)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        fabOpen = AnimationUtils.loadAnimation(App.instance.Context(), R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(App.instance.Context(), R.anim.fab_close)

        val fragmentManager = supportFragmentManager
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment?

                ?: MapFragment.newInstance((NaverMapOptions().locationButtonEnabled(false))
                        .also {
                            fragmentManager.beginTransaction().add(R.id.map, map).commit()
                        })
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        val startDelivery: View = findViewById(R.id.fab1)
        val market: View = findViewById(R.id.fab2)
        nMap = naverMap
        val locationButtonView = findViewById<LocationButtonView>(R.id.locationBtn)
        locationButtonView.map = nMap
        market.setOnClickListener { //두번째 버튼 눌렀을때 동작
            animation()
        }
        startDelivery.setOnClickListener {
            //첫번째 버튼 클릭했을때
            animation()
            for(i in 0..4){
                val time = responseList[i].responseData.route.traoptimal[0].summary.duration / 60000
                val distance=  responseList[i].responseData.route.traoptimal[0].summary.distance / 1000.toDouble()
                val distanceStr = String.format("%.1f Km", distance)
                val timeStr = "$time Min"
                val RI = requestInfo(R.drawable.sad,
                        getGeoName(responseList[i].wayPointLatLng),
                        getGeoName(responseList[i].goalLatLng),
                        timeStr, distanceStr,5000,
                        responseList[i].goalLatLng,
                        responseList[i].wayPointLatLng)
                requestList.add(RI)
                Log.e("requestListSize", "${requestList.size}")
            }
            showRequestDialog()
        }

        nMap.locationSource = locationSource
        nMap.locationTrackingMode = LocationTrackingMode.Follow
        nMap.locationOverlay.isVisible = true

        nMap.addOnLocationChangeListener { location ->
            currentLocation = location
            startPosition = "${location.longitude},${location.latitude}"
            Log.e("현재위치", "${currentLocation.latitude},${currentLocation.longitude}")
            if(goalLatLng != null && wayLatLng != null){
                Log.e("e", "${goalLatLng},${wayLatLng},${arriveCheck}")
                when {
                    checkError(wayLatLng!!) && !arriveCheck -> {
                        makeText(this, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                        markerStartPoint.map = null
                        arriveCheck = true
                    }
                    checkError(goalLatLng!!) && arriveCheck -> {
                        makeText(this, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                        markerWayPoint.map = null
                        markerGoalPoint.map = null
                        arriveCheck = false
                    }
                }
            }
        }
    }
    private fun animation(){
        val currentLocation: View = findViewById(R.id.fab1)
        val selectLocation: View = findViewById(R.id.fab2)

        if(isFabOpen){
            currentLocation.startAnimation(fabClose)
            selectLocation.startAnimation(fabClose)
            currentLocation.isClickable = false
            selectLocation.isClickable = false
            isFabOpen = false
        }
        else{
            currentLocation.startAnimation(fabOpen)
            selectLocation.startAnimation(fabOpen)
            currentLocation.isClickable = true
            selectLocation.isClickable = true
            isFabOpen = true
        }
    }
    private fun findPath(currentPoint:String, startPoint:String, goalPoint:String){
        val restClient: RetrofitInterface = Http3RetrofitManager.getRetrofitService(RetrofitInterface::class.java)
        val option = "traoptimal"
        val requestPath = restClient.requestPath(currentPoint, goalPoint, startPoint, option)

        requestPath.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }
            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                if(response.isSuccessful){
                    responseData = response.body()
                    val data = SummaryData(currentPoint, startPoint, goalPoint, response.body()!!)
                    responseList.add(data)
                }
            }
        })
    }
    private fun checkError(goalLatLng: LatLng): Boolean {
        val currentLat = currentLocation.latitude
        val currentLng = currentLocation.longitude
        val goalLat = goalLatLng.latitude
        val goalLng = goalLatLng.longitude
        return ((currentLat <= goalLat + 0.001 && currentLat >= goalLat - 0.001) ||
                (currentLng <= goalLng + 0.001 && currentLng >= goalLng - 0.001))
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
                        resources.getColor(R.color.colorPrimaryDark))
                )
                textRemain.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                requestSrc.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                requestDst.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                remainDurationText.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                remainDuration.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dustInfoText.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dustInfo.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dangerInfoText.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                dangerInfo.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                arrow.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
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
    } //configureBottomNav()

    //fab버튼 클릭 리스너를 따로 구현 -> onMapReady안에서 구현한 클릭리스너가 작동하지 않음 -> activity_maps.xml에 명시
    fun fabClickListener(v:View){
        animation()
        for(i in 0..8 step 2){
            val newGeoInfo = geoInfo(getLocationDB())
            positions.add(newGeoInfo.src)
            Log.e("출발지", newGeoInfo.src)
            positions.add(newGeoInfo.dst)
            Log.e("도착지",newGeoInfo.dst)
            try {
                findPath(startPosition, positions[i], positions[i+1])
            } catch (e: Exception) {
                makeText(this, "위치 수신을 동의해주세요!", LENGTH_SHORT).show()
            }
        }
    }

    //의뢰 리스트뷰 어댑터
    @SuppressLint("InflateParams")
    private fun showRequestDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater:LayoutInflater = layoutInflater
        val alertView:View = inflater.inflate(R.layout.request_dialog, null)
        builder.setView(alertView)

        val requestListView: ListView = alertView.findViewById(R.id.listview_requestdialog_list)
        val dialog:AlertDialog = builder.create()

        val adapter = requestListAdapter(this, requestList)
        requestListView.adapter  = adapter
        requestListView.setOnItemClickListener{parent, view, position, id ->
            val oDialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
            oDialog.setMessage("수락시 의뢰 리스트가 초기화됩니다.").setTitle("해당 의뢰를 수락하시겠습니까?")
                .setPositiveButton("아니오") {_, _ ->
                    makeText(this, "취소", Toast.LENGTH_LONG).show()
                }
                .setNeutralButton("예") {_, _ ->
                    Log.e("선택한 출발지", adapter.getItem(position).source)
                    Log.e("선택한 출발지_코드", adapter.getItem(position).sourceCode)
                    Log.e("선택한 도착지", adapter.getItem(position).destination)
                    Log.e("선택한 도착지_코드", adapter.getItem(position).destinationCode)

                    val setPathUI = SetPathUI(responseList[position].responseData, nMap)
                    setPathUI.setUIPath()
                    val arrWay = responseList[position].wayPointLatLng.split(",")
                    val arrGoal = responseList[position].goalLatLng.split(",")
                    wayLatLng = LatLng(arrWay[1].toDouble(), arrWay[0].toDouble())
                    goalLatLng = LatLng(arrGoal[1].toDouble(), arrGoal[0].toDouble())

                    requestSrc.text = adapter.getItem(position).source
                    requestDst.text = adapter.getItem(position).destination

                    dialog.dismiss()

                    var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
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

                    requestList.clear()
                    responseList.clear()
                    positions.clear()
                }
                .setCancelable(false).show()
        }
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}


