package com.example.sendymapdemo

import android.content.pm.PackageManager
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
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import org.json.JSONArray
import org.json.JSONObject
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import com.naver.maps.map.widget.ZoomControlView
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

var pathOverlayStart = PathOverlay()
var pathOverlayGoal = PathOverlay()
var markerStartPoint = Marker()
var markerWayPoint = Marker()
var markerGoalPoint = Marker()

var userID:String?=null

var httpArray = ArrayList<ArrayList<String>>() //http 커넥션으로 받은 JSON 데이터를 모은 ArrayList

fun login(test1:String){ //Login 후 사용자의 정보를 들고오는 함수
    var UserInfo = ArrayList<String>()
    val test = "http://15.164.103.195/login.php?user$test1"
    var task = URLConnector(test)
    task.start()
    try {
        task.join()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    var result: String? = task.getResult()
    var JO = JSONObject(result)
    var JA: JSONArray = JO.getJSONArray("result")
    for(i in 0 until JA.length()){
        val jo = JA.getJSONObject(i)
        UserInfo.add(jo.getString("ID"))
        UserInfo.add(jo.getString("Credit"))
        UserInfo.add(jo.getString("Property"))
        UserInfo.add(jo.getString("Car"))
    }
    UserInfo.get(0)
    println(UserInfo.get(0) + "  " + UserInfo.get(1) + "  " + UserInfo.get(2) + "  " + UserInfo.get(3) )
}
fun httpConnect(){ //Login 후에 Http connection을 통해 리더보드에 들어갈 데이터 호출
    val test = "http://15.164.103.195/httpConnection.php"
    var task = URLConnector(test)
    task.start()
    try {
        task.join()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    var result: String? = task.getResult()
    var JO: JSONObject = JSONObject(result)
    var JA: JSONArray = JO.getJSONArray("result")
    println(JA.getJSONObject(0))
    for (i in 0 until JA.length()) {
        val jo = JA.getJSONObject(i)
        var httpUser = ArrayList<String>()
        httpUser?.add(jo.getString("ID"))
        httpUser?.add(jo.getString("Credit"))
        httpUser?.add(jo.getString("Property"))
        httpUser?.add(jo.getString("Car"))
        httpArray?.add(httpUser)
        val newUser = userInfo(httpArray[i][0],Integer.parseInt(httpArray[i][2]),Integer.parseInt(httpArray[i][1]))
        userList.add(newUser)

//            println("first ID : "+ (httpArray?.get(i)))
    }
    boardAdapter.notifyDataSetChanged()
//    println("first ID : " + httpArray[0][0] + " First Property " + httpArray[0][2])
//    println("second ID : " + httpArray[1][0] + " Second Property " + httpArray[1][2])
//    println("third ID : " + httpArray[2][0] + " Third Property " + httpArray[2][2])
//
//    var a:Int=Integer.parseInt(httpArray[0][1])

}

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    private var requestResultStart: PathData ?= null
    private var requestResultGoal: PathData ?= null

    private var resultGoalLatLng: LatLng ?= null
    private var resultWayLatLng: LatLng ?= null

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

        drawerLayout = findViewById(R.id.drawer_layout)

        configureBottomNav()

        var toolbar : Toolbar = findViewById(R.id.toolbar)
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

//        val newUser = userList(123123,123,123)
//        userList.add(newUser)
//        boardAdapter.notifyDataSetChanged()
        recyclerList.adapter = boardAdapter
        recyclerList.layoutManager = layoutManager
        recyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        val intent = Intent(applicationContext,LoginActivity::class.java)
        startActivity(intent)

        httpConnect()

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
        nMap = naverMap

        val locationButtonView = findViewById<LocationButtonView>(R.id.locationBtn)
        locationButtonView.map = nMap

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
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }

            val setPathUIStart = SetPathUI(requestResultStart!!, nMap)
            val setPathUIGoal = SetPathUI(requestResultGoal!!, nMap)
            resultWayLatLng = setPathUIStart.setUIPathStart()
            resultGoalLatLng = setPathUIGoal.setUIPathGoal()
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

