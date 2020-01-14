package com.example.sendymapdemo

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
import com.naver.maps.map.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.content.Intent
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Thread.sleep
import com.google.android.material.navigation.NavigationView as NavigationView

//leaderBoardAdapter에서 드로워를 닫을 때 필요해서 전역으로 선언
lateinit var drawerLayout: DrawerLayout


//의뢰정보를 담은 리스트
var requestList = ArrayList<requestInfo>()
var positions=ArrayList<String>()



var markerStartPoint = Marker()
var markerWayPoint = Marker()
var markerGoalPoint = Marker()

//requestActivity에서 사용
var responseData: PathData ?= null
var responseList = ArrayList<SummaryData>()
lateinit var nMap: NaverMap


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    //리더보드 레이아웃 매니저
    private lateinit var headerName: TextView
    private lateinit var headerDesc: TextView
    private lateinit var headerRank: TextView
    private lateinit var headerCredit: TextView
    private lateinit var headerAccum: TextView
    private lateinit var headerPhoto:ImageView

    var wayLatLng: LatLng ?= null
    var goalLatLng: LatLng ?= null
    private lateinit var locationSource: LocationSource
    private lateinit var currentLocation: Location

    private var isFabOpen: Boolean = false
    private var arriveCheck: Boolean = false

    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    private lateinit var startPosition: String
    var resultReward:Double = 0.0
    override fun onBackPressed() {
        onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //리더보드 어댑터 초기화
        boardAdapter = leaderBoardAdapter(userList)

        //네이게이션 뷰의 헤더에 접근하기 위한 코드
        val navigationHeader = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationHeader.getHeaderView(0)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationHeader.setNavigationItemSelectedListener { menuitem: MenuItem ->
            when (menuitem.itemId) {
                R.id.menu_history -> {
                    val historyIntent = Intent(this, historyActivity::class.java)
                    startActivity(historyIntent)
                }
                R.id.menu_ranking -> {
                    val rankIntent = Intent(this, rankingActivity::class.java)
                    startActivity(rankIntent)
                }
                R.id.menu_about -> {
                    val builder = AlertDialog.Builder(this)
                    val dialogView = layoutInflater.inflate(R.layout.about, null)
                    //val dialog:AlertDialog = builder.create()
                    builder.setView(dialogView)
                        .show()
                }
                R.id.menu_update -> {

                }
                R.id.menu_logout-> {
                    val logout = Intent(this,LoginActivity::class.java)
                    startActivity(logout)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        headerName = headerView.findViewById(R.id.userID)
        headerDesc = headerView.findViewById(R.id.userDescription)
        headerRank = headerView.findViewById(R.id.userRank)
        headerCredit = headerView.findViewById(R.id.userCredit)
        headerAccum = headerView.findViewById(R.id.userAccum)
        headerPhoto = headerView.findViewById(R.id.ivUserProfilePhoto)

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
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                Log.e("열리는 중","드로워")
            }
        }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        val userID=intent.getStringExtra("ID")
        login(userID!!)


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
                Log.e("거리_시간",time.toString())
                val distance=  responseList[i].responseData.route.traoptimal[0].summary.distance / 1000.toDouble()
                Log.e("거리", distance.toString())
                val distanceStr = String.format("%.1f Km", distance)
                val timeStr = "$time"+"Min"
                val face =
                    if(distance <= 20) R.drawable.happy
                    else if(distance > 20 && distance <= 40) R.drawable.sad
                    else R.drawable.dead
                val reward = Math.pow(time.toDouble(),2.0)
                val RI = requestInfo(face,
                        getGeoName(responseList[i].wayPointLatLng),
                        getGeoName(responseList[i].goalLatLng),
                        timeStr, distanceStr,reward,
                        responseList[i].goalLatLng,
                        responseList[i].wayPointLatLng)
                requestList.add(RI)
                Log.e("requestListSize", "${requestList.size}")
            }
            val requestIntent = Intent(this, requestActivity::class.java)
            startActivityForResult(requestIntent,100)
        }

        nMap.locationSource = locationSource
        nMap.locationTrackingMode = LocationTrackingMode.Follow
        nMap.locationOverlay.isVisible = true

        nMap.addOnLocationChangeListener { location ->
            currentLocation = location
            startPosition = "${location.longitude},${location.latitude}"
            Log.e("현재위치", "${location.latitude},${location.longitude}")
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
//                        var abc:Double=(intent.getStringExtra("resultReward"))

                        updateCredit(userIdentity,resultReward)
                        markerWayPoint.map = null
                        markerGoalPoint.map = null
                        arriveCheck = false

                    }
                }
            }
        }
        locationStartBtn.setOnClickListener {
            if(nMap.locationTrackingMode == LocationTrackingMode.None){
                Log.e("Flag", "${nMap.locationTrackingMode}")
                GlobalScope.launch(Dispatchers.Default) {
                    for (i in 0 until latlngList.size) {
                        currentLocation.latitude = latlngList[i].latitude
                        currentLocation.longitude = latlngList[i].longitude
                        Log.e("위치변경", "${currentLocation.latitude}, ${currentLocation.longitude}")
                        drawingLocationUI(currentLocation)
                        sleep(250)
                        if (nMap.locationTrackingMode == LocationTrackingMode.Follow ||
                                nMap.locationTrackingMode == LocationTrackingMode.NoFollow) {
                            break
                        }
                    }
                }
            }
            else{
                Log.e("Flag", "${nMap.locationTrackingMode}")
                nMap.locationTrackingMode = LocationTrackingMode.None
            }
        }
    }
    private fun drawingLocationUI(location: Location) = GlobalScope.launch(Dispatchers.Main){
        nMap.let {
            val locationOverlay = it.locationOverlay
            locationOverlay.isVisible = true
            locationOverlay.position = LatLng(location.latitude, location.longitude)
            Log.e("위치변경", "${locationOverlay.position}")
            it.moveCamera(CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude)))
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
    } //configureBottomNav()

    //fab버튼 클릭 리스너를 따로 구현 -> onMapReady안에서 구현한 클릭리스너가 작동하지 않음 -> activity_maps.xml에 명시
    fun fabClickListener(view: View){
        view.bringToFront()
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            100 -> {
                when(resultCode){
                    Activity.RESULT_OK ->{
                        val resultSrc = data!!.getStringExtra("resultSrc")
                        val resultDst = data.getStringExtra("resultDst")
                        val resultDistance = data.getStringExtra("resultDistance")
                        wayLatLng=LatLng(data.getDoubleExtra("wayLatLng[0]",0.0),data.getDoubleExtra("wayLatLng[1]",0.0))
                        goalLatLng=LatLng(data.getDoubleExtra("goalLatLng[0]",0.0),data.getDoubleExtra("goalLatLng[1]",0.0))
                        resultReward=data.getDoubleExtra("resultReward",0.0)

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

                        requestList.clear()
                        responseList.clear()
                        positions.clear()
                    }
                    Activity.RESULT_CANCELED -> {
                        requestList.clear()
                    }
                }
            }
        }
    }

    fun login(test1:String){ //Login 후 사용자의 정보를 들고오는 함수
        val UserInfo = ArrayList<String>()
        val test = "http://15.164.103.195/login.php?user=$test1"
        val task = URLConnector(test)
        task.start()
        try {
            task.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val result: String? = task.getResult()
        val JO = JSONObject(result)
        val Jrank = JO.getString("rank")

        println(Jrank)
        val JA: JSONArray = JO.getJSONArray("result")

        for(i in 0 until JA.length()){
            val jo = JA.getJSONObject(i)
            UserInfo.add(jo.getString("ID"))
            UserInfo.add(jo.getString("Credit"))
            UserInfo.add(jo.getString("Property"))
            UserInfo.add(jo.getString("Car"))
        }
        headerName.text = UserInfo.get(0)
        headerRank.text = Jrank
        headerCredit.text = UserInfo.get(2)
        headerAccum.text = UserInfo.get(1)
        while(task.isAlive){}
        UserInfo.clear()
        httpConnect()
    }
}


