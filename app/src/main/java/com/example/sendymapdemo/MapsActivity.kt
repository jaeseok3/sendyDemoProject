package com.example.sendymapdemo

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

var pathOverlayStart = PathOverlay()
var pathOverlayGoal = PathOverlay()
var markerStartPoint = Marker()
var markerWayPoint = Marker()
var markerGoalPoint = Marker()


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
//    private lateinit var requestImg: ImageView
////    private lateinit var requestSrc: TextView
////    private lateinit var requestDst: TextView
////    private lateinit var requestTime: TextView
////    private lateinit var requestDuration: TextView
////    private lateinit var requestReward: TextView
    //private lateinit var requestInfoContaner: requestInfo

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private var requestResultStart: PathData ?= null
    private var requestResultGoal: PathData ?= null

    private var resultGoalLatLng: LatLng ?= null
    private var resultWayLatLng: LatLng ?= null

    private lateinit var result1: LatLng
    private lateinit var result2: LatLng
    private var markerStartPoint = Marker()
    private var markerWayPoint = Marker()
    private var markerGoalPoint = Marker()
    private var pathOverlayStart = PathOverlay()
    private var pathOverlayGoal = PathOverlay()

    private lateinit var locationSource: FusedLocationSource
    private lateinit var currentLocation: Location

    private var isFabOpen: Boolean = false
    private var arriveCheck: Boolean = false

    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    private lateinit var startPosition: String
    private lateinit var goalPosition: String
    private lateinit var wayPosition: String

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
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                Log.e("열리는 중","드로워")
            }
        }

        drawer_layout.addDrawerListener(toggle)


        toggle.syncState()

        //리더보드 어댑터 초기화
        boardAdapter = leaderBoardAdapter(userList)
        //리더보드 레이아웃 매니저
        layoutManager = LinearLayoutManager(this)

        recyclerList.adapter = boardAdapter
        recyclerList.layoutManager = layoutManager
        recyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

//        while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
//        }

        var intent = Intent(applicationContext,LoginActivity::class.java)
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
//     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//         if(locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
//             return
//         }
//         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        startDelivery.setOnClickListener {  //첫번째 버튼 클릭했을때
            animation()

            val getPosition1:ArrayList<String> = getLocationDB() //DB로부터 랜덤 2개를 불러옴
            //goalPosition = "${129.082287},${35.231028}"
            //wayPosition = "${129.118666},${35.153028}"
            for(i in 0..5){
                var newGeoInfo = geoInfo(getLocationDB())
                positions.add(newGeoInfo.src)
                Log.e("출발지", newGeoInfo.src)
                positions.add(newGeoInfo.dst)
                Log.e("도착지",newGeoInfo.dst)
            }

            Log.e("Locationfind",getPosition1[0])
            showRequestDialog()
        }

        nMap.locationSource = locationSource
        nMap.locationTrackingMode = LocationTrackingMode.Follow
        nMap.locationOverlay.isVisible = true

        nMap.addOnLocationChangeListener { location ->
            currentLocation = location
            startPosition = "${location.longitude},${location.latitude}"
            Log.e("현재위치", "${currentLocation.latitude},${currentLocation.longitude}")
            Log.e("경유지", "$resultWayLatLng")
            Log.e("도착지", "$resultGoalLatLng")
            val setPathUIStart = requestResultStart?.let { it1 -> SetPathUI(it1, nMap) }
            val setPathUIGoal = requestResultGoal?.let { it1 -> SetPathUI(it1, nMap) }
            resultWayLatLng = setPathUIStart?.setUIPathStart()
            resultGoalLatLng = setPathUIGoal?.setUIPathGoal()
            if(resultWayLatLng != null && resultGoalLatLng != null){
                Log.e("e", "${resultWayLatLng},${resultGoalLatLng},${arriveCheck}")
                when {
                    checkError(resultWayLatLng!!) && !arriveCheck -> {
                        makeText(this, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                        pathOverlayStart.map = null
                        markerStartPoint.map = null
                        arriveCheck = true
                    }
                    checkError(resultGoalLatLng!!) && arriveCheck -> {
                        pathOverlayGoal.map = null
                        makeText(this, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                        markerWayPoint.map = null
                        markerGoalPoint.map = null
                        arriveCheck = false
                    }
                }
            }
            if(requestResultGoal != null && requestResultStart != null){
                val startGuideList = requestResultStart?.route?.traoptimal!![0].guide
                val goalGuideList = requestResultGoal?.route?.traoptimal!![0].guide

                Log.e("인덱스", "${startGuideList[0].pointIndex}")
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
    private fun findPath(startPoint:String, goalPoint:String, wayPoints:String){
        val restClient: RetrofitInterface = Http3RetrofitManager.getRetrofitService(RetrofitInterface::class.java)
        val option = "traoptimal"
        val requestPathStartToWay = restClient.requestPath(startPoint, wayPoints, option)
        val requestPathWayToGoal = restClient.requestPath(wayPoints, goalPoint, option)

        requestPathStartToWay.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }
            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                if(response != null && response.isSuccessful) {
                    requestResultStart = response.body()!!
                }
            }
        })
        requestPathWayToGoal.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }
            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                if(response != null && response.isSuccessful){
                    requestResultGoal = response.body()!!
                }
            }
        })
    }
    private fun checkError(goalLatLng: LatLng): Boolean {
        val currentLat = currentLocation.latitude
        val currentLng = currentLocation.longitude
        val goalLat = goalLatLng.latitude
        val goalLng = goalLatLng.longitude
        return ((currentLat <= goalLat + 0.0001 && currentLat >= goalLat - 0.0001) ||
                (currentLng <= goalLng + 0.0001 && currentLng >= goalLng - 0.0001))
    }

   private fun configureBottomNav(){
        //하단 슬라이딩 바
        val originalScaleX = fab.scaleX
        val originalScaleY = fab.scaleY
        var isExpanded = 0
       var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
       bottomSheet.visibility = View.GONE
       bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        isExpanded = 1
                        //textFull.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        isExpanded = 0
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
    }

    //의뢰 리스트뷰 어댑터
    fun showRequestDialog(){
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater:LayoutInflater = layoutInflater
        var alertView:View = inflater.inflate(R.layout.request_dialog, null)
        builder.setView(alertView)

        var requestListView: ListView = alertView.findViewById(R.id.listview_requestdialog_list)
        var dialog:AlertDialog = builder.create()
        for(i in 0..8 step 2){
            var RI = requestInfo(R.drawable.sad,
                getGeoName(positions[i]),getGeoName(positions[i+1]),1000,23000,5000,positions[i+1],positions[i])
            requestList.add(RI)
        }

        val adapter = requestListAdapter(this, requestList)
        requestListView.adapter  = adapter
        requestListView.setOnItemClickListener{parent, view, position, id ->
            var oDialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
            oDialog.setMessage("수락시 의뢰 리스트가 초기화됩니다.").setTitle("해당 의뢰를 수락하시겠습니까?")
                .setPositiveButton("아니오") {_, _ ->
                    makeText(this, "취소", Toast.LENGTH_LONG).show()
                }
                .setNeutralButton("예") {_, _ ->
                    Log.e("선택한 출발지", adapter.getItem(position).source )
                    Log.e("선택한 출발지_코드", adapter.getItem(position).sourceCode )
                    Log.e("선택한 도착지", adapter.getItem(position).destination)
                    Log.e("선택한 도착지_코드", adapter.getItem(position).destinationCode )
                    try {
                        findPath(startPosition, adapter.getItem(position).sourceCode, adapter.getItem(position).destinationCode)
                    } catch (e: Exception) {
                        makeText(this, "위치 수신을 동의해주세요!", Toast.LENGTH_SHORT).show()
//                finish()
                    }
                    requestSrc.text = adapter.getItem(position).source
                    requestDst.text = adapter.getItem(position).destination
                    remainDuration.text = adapter.getItem(position).duration.toString()
                    dialog.dismiss()

                    var bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    val animation = AlphaAnimation(0.0f,1.0f)
                    animation.duration = 1000
                    bottomSheet.animation = animation
                    bottomSheet.visibility = View.VISIBLE

                    requestList.clear()
                    positions.clear()
                }
                .setCancelable(false).show()
        }
        dialog.setCancelable(false)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

}


