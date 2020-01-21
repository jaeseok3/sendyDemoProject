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
import com.naver.maps.map.overlay.Marker
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.example.sendymapdemo.*
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataClass.AllUserData
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.koinModule.ApplicationMain
import com.example.sendymapdemo.model.repository.MapsRepository
import com.example.sendymapdemo.model.repository.UserRepository
import com.example.sendymapdemo.model.roomDB.UserRoomDataBase
import com.example.sendymapdemo.ui.adapters.LeaderBoardAdapter
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.requestDst
import kotlinx.android.synthetic.main.activity_maps.requestSrc
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Runnable
import java.net.URL
import com.google.android.material.navigation.NavigationView as NavigationView

class MapsActivity : AppCompatActivity(){
    lateinit var startDelivery: View
    lateinit var market: View
    lateinit var locationButtonView: LocationButtonView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private val userRepository: UserRepository by inject()
    private val allUserData: AllUserData by inject()
    private val nMap: MapsRepository by inject()
    private val historyRepository: HistoryRepository by inject()

    //리더보드 레이아웃 매니저
    private lateinit var drawerLayout: DrawerLayout
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
    private lateinit var resultDistance: String
    var progressRate = 0.0
    var resultReward:Double = 0.0

    override fun onBackPressed() {
        onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userID = userRepository.userID
        var userData: UserData
        val r = Runnable {
            userData = userRepository.getFromRoom(userID)
            Log.e("유저", "$userID,$userData")
            Runnable {
                headerName.text = userData.id
                headerRank.text = userData.rank
                headerCredit.text = userData.credit
                headerAccum.text = userData.property
            }.run()
        }
        val thread = Thread(r)
        thread.start()

        //네이게이션 뷰의 헤더에 접근하기 위한 코드
        val navigationHeader = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationHeader.getHeaderView(0)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationHeader.setNavigationItemSelectedListener { menuitem: MenuItem ->
            when (menuitem.itemId) {
                R.id.menu_history -> {
                    historyRepository.getHistory(userRepository.userID)
                    val historyIntent = Intent(this, HistoryActivity::class.java)
                    startActivity(historyIntent)
                }
                R.id.menu_ranking -> {
                    userRepository.getAllUsers()
                    val rankIntent = Intent(this, rankingActivity::class.java)
                    startActivity(rankIntent)
                }
                R.id.menu_about -> {
                    val builder = AlertDialog.Builder(this)
                    val dialogView = layoutInflater.inflate(R.layout.about, null)
                    val dialog:AlertDialog = builder.create()
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
                ?: MapFragment.newInstance((NaverMapOptions().locationButtonEnabled(false))
                        .also {
                            fragmentManager.beginTransaction().add(R.id.map, map).commit()
                        })
        mapFragment.getMapAsync(nMap)

        startDelivery = findViewById(R.id.fab1)
        market = findViewById(R.id.fab2)
        locationButtonView = findViewById<LocationButtonView>(R.id.locationBtn)

        nMap.listener = {
            Log.e("메인액티비티","온맵레디")
            //nMap.nMap = naverMap
            nMap.nMap!!.locationSource = locationSource
            nMap.nMap!!.locationTrackingMode = LocationTrackingMode.Follow
            nMap.nMap!!.locationOverlay.isVisible = true
            nMap.nMap!!.addOnLocationChangeListener { location ->
                currentLocation = location
                startPosition = "${location.longitude},${location.latitude}"
                Log.e("현재위치", "${location.latitude},${location.longitude}")
                if(goalLatLng != null && wayLatLng != null){
                    Log.e("e", "${goalLatLng},${wayLatLng},${arriveCheck}")
                    when {
                        checkError(wayLatLng!!) && !arriveCheck -> {
                            makeText(this, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                            nMap.markerStartPoint.map = null
                            arriveCheck = true
                        }
                        checkError(goalLatLng!!) && arriveCheck -> {
                            makeText(this, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
//                        var abc:Double=(intent.getStringExtra("resultReward"))

//                            updateCredit(login.ID,resultReward)
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
                    GlobalScope.async {
                        for (i in 0 until latlngList.size) {
                            currentLocation.latitude = latlngList[i].latitude
                            currentLocation.longitude = latlngList[i].longitude

                            Log.e("위치변경", "${currentLocation.latitude}, ${currentLocation.longitude}")
                            runBlocking {
                                drawingLocationUI(LatLng(latlngList[i].latitude, latlngList[i].longitude))
                                getDangerGrade(latlngList[i].latitude.toString(), latlngList[i].longitude.toString(),
                                    latlngList[i].latitude.toString(), latlngList[i].longitude.toString())
                            }
                            delay(2000)
                            if (nMap.nMap!!.locationTrackingMode == LocationTrackingMode.Follow ||
                                nMap.nMap!!.locationTrackingMode == LocationTrackingMode.NoFollow) {
                                progressRate = 0.0
                                drawingLocationUI(LatLng(currentLocation.latitude, currentLocation.longitude))
                                break
                            }
                        }
                    }
                }
                else{
                    Log.e("Flag", "${nMap.nMap!!.locationTrackingMode}")
                    nMap.nMap!!.locationTrackingMode = LocationTrackingMode.None
                }
            }
        }
    }
    private fun drawingLocationUI(latLng: LatLng) = GlobalScope.launch(Dispatchers.Main){
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
                    checkError(wayLatLng!!) && !arriveCheck -> {
                        makeText(applicationContext, "출발지에 도착하였습니다.", LENGTH_SHORT).show()
                        nMap.markerStartPoint.map = null
                        nMap.markerWayPoint.map = null
                        arriveCheck = true
                    }
                    checkError(goalLatLng!!) && arriveCheck -> {
                        makeText(applicationContext, "도착지에 도착하였습니다.", LENGTH_SHORT).show()
//                        userRepository.updateCredit(userData.id, resultReward)
                        nMap.markerGoalPoint.map = null
                        nMap.pathOverlay.map = null
                        arriveCheck = false
                    }}
            },1000)
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
    private fun getDangerGrade(startLat:String, startLng:String, endLat:String, endLng:String) {
        lateinit var temp : String
        class getDangerGrade : AsyncTask<Void, Void, Void>(){
            override fun doInBackground(vararg params: Void?): Void? {
                val stream = URL("http://apis.data.go.kr/B552061/roadDgdgrLink/getRestRoadDgdgrLink?serviceKey=%2BwvPpNobnpO%2BxNDsB3NdwZqjZYg4C8JqEy7NhZxXof%2F2Owy9Vu2eYP1pZVtIw%2FcPEVTx8nKQ1ph%2F4ppRNxKBLA%3D%3D&" +
                        "searchLineString=LineString("+
                        startLng + " " +
                        startLat + ", " +
                        endLng + " " +
                        endLat + ")&vhctyCd=1&type=json&numOfRows=10&pageNo=1").openStream()
                val read = BufferedReader(InputStreamReader(stream,"UTF-8"))
                temp  = read.readLine()
                Log.e("파싱 진행중", temp)

                return null
            }

            override fun onPostExecute(result: Void?) {
                var grade : String
                super.onPostExecute(result)
                val json = JSONObject(temp)
                if(json.get("resultCode") != "10") {
                    val chiefObject = (json["items"] as JSONObject)
                    val upperArray : JSONArray = chiefObject.getJSONArray("item")
                    val upperObject = upperArray.getJSONObject(0)
                    grade = upperObject.getString("anals_grd")
                    grade =  when(grade){
                        "01" -> "1등급"
                        "02" -> "2등급"
                        "03" -> "3등급"
                        "04" -> "4등급"
                        "05" -> "5등급"
                        else -> "none"
                    }
                }
                else
                    grade = "none"
                dangerInfo.text = grade
                Log.e("파싱결과",grade)

            }
        }
        getDangerGrade().execute()
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
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            100 -> {
                when(resultCode){
                    Activity.RESULT_OK ->{
                        val resultSrc = data!!.getStringExtra("resultSrc")
                        val resultDst = data.getStringExtra("resultDst")
                        resultDistance = data.getStringExtra("resultDistance")!!
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
                    }
                }
            }
        }
    }
}


