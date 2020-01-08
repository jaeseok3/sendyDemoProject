package com.example.sendymapdemo

import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*

//leaderBoardAdapter에서 드로워를 닫을 때 필요해서 전역으로 선언
lateinit var drawerLayout: DrawerLayout
//리더보드 어댑터
lateinit var boardAdapter:leaderBoardAdapter
//유저들의 정보를 담은 리스트
var userList = ArrayList<userInfo>()
//리더보드 레이아웃 매니저
lateinit var layoutManager: LinearLayoutManager

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var result1: LatLng
    private lateinit var result2: LatLng
    private var markerStartPoint = Marker()
    private var markerWayPoint = Marker()
    private var markerGoalPoint = Marker()
    private var pathOverlayStart = PathOverlay()
    private var pathOverlayGoal = PathOverlay()
    private lateinit var locationSource: FusedLocationSource
    private lateinit var currentLocation: Location
    private lateinit var naverMap: NaverMap

    private var isFabOpen: Boolean = false
    private var isFinishFind1: Boolean = false
    private var isFinishFind2: Boolean = false
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

        drawerLayout = findViewById(R.id.drawer_layout)

        configureBottomNav()

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        //사이드바 토글 생성
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, 0, 0
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        //리더보드 어댑터 초기화
        boardAdapter = leaderBoardAdapter(userList)
        //리더보드 레이아웃 매니저
        layoutManager = LinearLayoutManager(this)
        //어댑터 생성
        boardAdapter = leaderBoardAdapter(userList)

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
                ?: MapFragment.newInstance().also {
                    fragmentManager.beginTransaction().add(R.id.map, it).commit()
                }

        mapFragment.getMapAsync(this)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        nMap = naverMap
        val fab: View = findViewById(R.id.fab)
        val fab1: View = findViewById(R.id.fab1)
        val fab2: View = findViewById(R.id.fab2)
        fab.bringToFront()

        fab.setOnClickListener {
            animation()
        }
        fab2.setOnClickListener { //두번째 버튼 눌렀을때 동작
            animation()
        }
        fab1.setOnClickListener {  //첫번째 버튼 클릭했을때
            animation()

            goalPosition = "${129.082287},${35.231028}"
            wayPosition = "${129.118666},${35.153028}"
            try {
                findPath(startPosition, goalPosition, wayPosition)
            } catch(e:Exception){
                Toast.makeText(this,"위치 수신 동의해주세요!",Toast.LENGTH_SHORT).show()
//                finish()
            }
        }

        nMap.locationSource = locationSource
        nMap.locationTrackingMode = LocationTrackingMode.Follow
        nMap.locationOverlay.isVisible = true
        nMap.uiSettings.isLocationButtonEnabled = true

        nMap.addOnLocationChangeListener { location ->
            currentLocation = location
            startPosition = "${location.longitude},${location.latitude}"
//            Log.e("현재위치", "${currentLocation.latitude},${currentLocation.longitude}")

            if(isFinishFind1 && isFinishFind2){
                when {
                    checkError(result1) && !arriveCheck -> {
                        pathOverlayStart.map = null
                        makeText(this, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                        markerStartPoint.map = null
                        arriveCheck = true
                    }
                    checkError(result2) && arriveCheck -> {
                        pathOverlayGoal.map = null
                        makeText(this, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
                        markerWayPoint.map = null
                        markerGoalPoint.map = null
                        arriveCheck = false
                    }
                    checkError(result2) && !arriveCheck -> {
                        makeText(this, "출발지부터 가야합니다.", LENGTH_SHORT).show()
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
    private fun findPath(startPoint:String, goalPoint:String, wayPoints:String){
        val restClient: RetrofitInterface = Http3RetrofitManager.getRetrofitService(RetrofitInterface::class.java)
        val option = "trafast"
        val requestPathStartToWay = restClient.requestPath(startPoint, wayPoints, option)
        val requestPathWayToGoal = restClient.requestPath(wayPoints, goalPoint, option)

        requestPathStartToWay.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }
            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                if(response != null && response.isSuccessful) {
                    setUIPathStart(response.body())
                }
            }
        })
        requestPathWayToGoal.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }
            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                setUIPathGoal(response.body())
            }
        })
    }

    private fun setUIPathGoal(data: PathData?){
        val latlngListGoal = ArrayList<LatLng>()
        val pathArr = data?.route?.trafast?.get(0)?.path
        val goalLng = data?.route?.trafast?.get(0)?.summary?.goal?.location?.get(0)
        val goalLat = data?.route?.trafast?.get(0)?.summary?.goal?.location?.get(1)
        result2 = LatLng(goalLat!!, goalLng!!)
        for(i in pathArr!!.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngListGoal.add(pathLatLng)
        }

        pathOverlayGoal.coords = latlngListGoal
        pathOverlayGoal.outlineWidth = 5
        pathOverlayGoal.color = Color.RED
        markerGoalPoint.position = latlngListGoal[latlngListGoal.size - 1]
        markerGoalPoint.iconTintColor = Color.RED
        markerGoalPoint.map = nMap
        pathOverlayGoal.map = nMap

        isFinishFind2 = true
    }

    private fun setUIPathStart(data: PathData?){
        val latlngListStart = ArrayList<LatLng>()
        val pathArr = data?.route?.trafast?.get(0)?.path
        val goalLng = data?.route?.trafast?.get(0)?.summary?.goal?.location?.get(0)
        val goalLat = data?.route?.trafast?.get(0)?.summary?.goal?.location?.get(1)
        result1 = LatLng(goalLat!!, goalLng!!)

        for(i in pathArr!!.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngListStart.add(pathLatLng)
        }

        pathOverlayStart.coords = latlngListStart
        pathOverlayStart.outlineWidth = 5
        pathOverlayStart.color = Color.BLUE
        markerStartPoint.position = latlngListStart[0]
        markerWayPoint.position = latlngListStart[latlngListStart.size - 1]
        markerStartPoint.map = nMap
        markerStartPoint.iconTintColor = Color.BLUE
        markerWayPoint.map = nMap
        pathOverlayStart.map = nMap

        isFinishFind1 = true
    }

    private fun parsingPath(rawPathData: String): LatLng{
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
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
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
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
                bottomSheet.setBackgroundColor(
                    interpolateColor(slideOffset,
                        Color.argb(255, 223, 221, 255),
                        resources.getColor(R.color.colorPrimaryDark))
                )
                textRemain.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                textDust.setTextColor(
                    interpolateColor(slideOffset,
                        resources.getColor(R.color.colorPrimaryDark),
                        Color.argb(255, 223, 221, 255))
                )
                textAccident.setTextColor(
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

}

