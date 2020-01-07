package com.example.sendymapdemo

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    private lateinit var locationSource: FusedLocationSource

    private lateinit var currentLocation: Location
    private var latlngList = ArrayList<LatLng>()

    private var isFabOpen: Boolean = false
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    private lateinit var startPosition: String
    private lateinit var goalPosition: String
    private lateinit var wayPosition: String

    private lateinit var nMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

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

        fab.setOnClickListener {
            animation()
        }
        fab2.setOnClickListener { //두번째 버튼 눌렀을때 동작
            animation()
        }
        fab1.setOnClickListener {  //첫번째 버튼 클릭했을때
            animation()
            goalPosition = "${129.08},${35.23}"
            wayPosition = "${129.12},${35.15}"
            findPath(startPosition, goalPosition, wayPosition)
            val arr = wayPosition.split(",")
            val marker = Marker()
            marker.position = LatLng(arr[1].toDouble(), arr[0].toDouble())
            marker.map = nMap
        }

        nMap.locationSource = locationSource
        nMap.locationTrackingMode = LocationTrackingMode.Follow
        nMap.locationOverlay.isVisible = true
        nMap.uiSettings.isLocationButtonEnabled = true

        nMap.addOnLocationChangeListener { location ->
            currentLocation = location
            startPosition = "${location.longitude},${location.latitude}"
            Log.e("현재위치", "${currentLocation.latitude},${currentLocation.longitude}")
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
        val option = "trafast"

        val restClient: RetrofitInterface = Http3RetrofitManager.getRetrofitService(RetrofitInterface::class.java)

        val requestPath = restClient.requestPath(startPoint, goalPoint, wayPoints, option)
        requestPath.enqueue(object : Callback<PathData> {
            override fun onFailure(call: Call<PathData>, t: Throwable) {
                error(message = t.toString())
            }

            override fun onResponse(call: Call<PathData>, response: Response<PathData>) {
                if(response != null && response.isSuccessful)
                    setUIPath(response.body())
            }
        })
    }

    private fun setUIPath(data: PathData?){
        val pathArr = data?.route?.trafast?.get(0)?.path
        val pathOverlay = PathOverlay()

        for(i in pathArr!!.indices){
            val path = pathArr[i].toString()
            val pathLatLng = parsingPath(path)
            latlngList.add(pathLatLng)
        }
        pathOverlay.coords = latlngList
        pathOverlay.outlineWidth = 5
        pathOverlay.color = Color.BLUE
        pathOverlay.passedColor = Color.GRAY
        pathOverlay.map = nMap
    }

    private fun parsingPath(rawPathData: String): LatLng{
        val arr = rawPathData.split(",")
        val lng: Double = arr[0].substring(1).toDouble()
        val lat: Double = arr[1].substring(0, arr[1].indexOf("]")).toDouble()

        return LatLng(lat, lng)
    }
}