package com.example.sendymapdemo

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import org.json.JSONArray
import org.json.JSONObject
import android.content.Intent
import android.widget.Toast


var userID:String?=null

var httpArray = ArrayList<ArrayList<String>>() //http 커넥션으로 받은 JSON 데이터를 모은 ArrayList

fun login(test1:String){ //Login 후 사용자의 정보를 들고오는 함수
    var UserInfo = ArrayList<String>()
    val test = "http://15.164.103.195/login.php?user=$test1"
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
//            println("first ID : "+ (httpArray?.get(i)))
    }

    println("first ID : " + httpArray[0][0] + " First Property " + httpArray[0][2])
    println("second ID : " + httpArray[1][0] + " Second Property " + httpArray[1][2])
    println("third ID : " + httpArray[2][0] + " Third Property " + httpArray[2][2])

    var a:Int=Integer.parseInt(httpArray[0][1])
}

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var locationSource: FusedLocationSource

    private var isFabOpen: Boolean = false
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        val intent = Intent(applicationContext,LoginActivity::class.java)
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
        httpConnect()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        val fab: View = findViewById(R.id.fab)
        val currentLocation: View = findViewById(R.id.fab1)
        val selectLocation: View = findViewById(R.id.fab2)

        fab.setOnClickListener {
            animation()
        }
        currentLocation.setOnClickListener { //두번째 버튼 눌렀을때 동작
            animation()
        }
        selectLocation.setOnClickListener {  //첫번째 버튼 클릭했을때
            animation()
        }

        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.locationOverlay.isVisible = true
        naverMap.uiSettings.isLocationButtonEnabled = true

        val startPosition = "35.179792,129.074997" //부산시청
        val goalPosition = "35.231028,129.082287"    //부산대
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
}


