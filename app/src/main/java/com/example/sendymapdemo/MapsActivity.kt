package com.example.sendymapdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Switch
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*

lateinit var adapter:NavAdapter
lateinit var layoutManager: LinearLayoutManager
lateinit var recyclerView : RecyclerView
val Markerlist = ArrayList<markerData>()
lateinit var mMap: GoogleMap
lateinit var drawerLayout: DrawerLayout
//라인 객체 선언
var markerLine : Polyline? = null
//마커의 위치를 담을 리스트 선언 -> 라인을 그리는데에 사용
lateinit var LineList : MutableList<LatLng>
lateinit var mCurrentLocation: Location

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var toolbar: Toolbar

    private lateinit var db:LocationDB

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var lastLocation: Location
    private lateinit var location: Location


    private lateinit var fab_open: Animation
    private lateinit var fab_close: Animation

    private var isSelect: Boolean = false
    private var isFabOpen: Boolean = false
    private val locationCallback: LocationCallback = object: LocationCallback(){ //Location Request를 통해 나온 Location Callback
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
//            LineList = mutableListOf()
            val locationList: List<Location> = locationResult!!.locations
            if(locationList.isNotEmpty()){
                location = locationList[locationList.size - 1]
//                setCurrentLocation(location)
                mCurrentLocation = location //현재 위도, 경도를 반환한다.

            }
            if(locationList.isNotEmpty() && findPath.isChecked){ //스위치 켜졌을 때
                db.InsertLocation(mCurrentLocation)
                drawPolyLine(LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude))
            }
            else if(!findPath.isChecked){ //스위치 껏을 때
//                markerLine?.remove()
//                LineList.clear()
//                mMap.clear()
//                db.listMarker(mMap)
//                db.DeleteLocation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
    }

    override fun onStop() {
        super.onStop()
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /* 스위치 상태 저장 */
    override fun onResume() {
        super.onResume()
        restoreState()
    }
    override fun onPause() {
        super.onPause()
        saveState()
    }

    private fun restoreState(){ //onCreate, onResume에서 동작
        val pref : SharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        findPath.isChecked = pref.getBoolean("pref_bool",true)
    }

    private fun saveState(){ //onPause, onDestory에서 동작
        val pref : SharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = pref.edit()
            editor.putBoolean("pref_bool",findPath.isChecked)
        editor.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //리스트 초기화
        LineList = mutableListOf()
        while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(1000)
            .setFastestInterval(500)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        db = LocationDB(this)

        fab_open = AnimationUtils.loadAnimation(App.instance.Context(), R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(App.instance.Context(), R.anim.fab_close)

        recyclerView = findViewById(R.id.recyclerList)
        drawerLayout = findViewById(R.id.drawer_layout)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //navigation drawer 구현
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //navView = findViewById(R.id.nav_view)
        layoutManager = LinearLayoutManager(this)
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, 0, 0
        )

        drawer_layout.addDrawerListener(toggle)

        //삭제버튼 구현
        deleteButton.setOnClickListener{
            Log.e("온클릭리스너","작동")
//            markerLine?.remove()
//            LineList.clear()
            db.deleteMarker()}
        toggle.syncState()
        //navView.setNavigationItemSelectedListener(this.)

        //어댑터 생성
        adapter = NavAdapter(Markerlist)
        recyclerList.adapter = adapter
        recyclerList.layoutManager = layoutManager
        recyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun animation(){
        val currentLocation: View = findViewById(R.id.fab1) //지도상 원하는 위치 마커 찍기
        val selectLocation: View = findViewById(R.id.fab2) //현재 위치 마커 찍기

        if(isFabOpen){
            currentLocation.startAnimation(fab_close)
            selectLocation.startAnimation(fab_close)
            currentLocation.isClickable = false
            selectLocation.isClickable = false
            isFabOpen = false
        }
        else{
            currentLocation.startAnimation(fab_open)
            selectLocation.startAnimation(fab_open)
            currentLocation.isClickable = true
            selectLocation.isClickable = true
            isFabOpen = true
        }
    }

    private val mapClickListener =
        GoogleMap.OnMapClickListener { map ->
            val latLng = LatLng(map.latitude, map.longitude)

            if(!isSelect) return@OnMapClickListener

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
            db.AddMarker(latLng, "hi")
            mMap.addMarker(MarkerOptions().position(latLng).title("hi"))
            val newMarkerData = markerData(map.latitude, map.longitude, "NAME")
            Markerlist.add(newMarkerData)
            adapter.notifyDataSetChanged()

            //라인 그리기
//            drawPolyLine(latLng)

            isSelect = false
        }

    private val markerClickListener =
        GoogleMap.OnMarkerClickListener { marker ->
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 15.0f))
            val adapter = CustomInfoWindowAdapter(this)
            mMap.setInfoWindowAdapter(adapter)
            marker.showInfoWindow()
            true
        }

    override fun onMapReady(googleMap: GoogleMap) {
        val fab: View = findViewById(R.id.fab)
        val currentLocation: View = findViewById(R.id.fab1)
        val selectLocation: View = findViewById(R.id.fab2)
        val findPath: Switch = findViewById(R.id.findPath)

        mMap = googleMap

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            startLocationUpdates()
        }

        mMap.uiSettings.isZoomControlsEnabled

        mMap.setOnMarkerClickListener(markerClickListener)

        db.listMarker(mMap)

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mFusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->
            if (location != null){
                lastLocation = location
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13.0f))
                Log.e("현재위치", "${lastLocation.latitude}, ${lastLocation.longitude}")
            }
        }

        fab.setOnClickListener {
            Log.e("FAB", "FAB CLICKED")
            animation()
        }
        currentLocation.setOnClickListener { //두번째 버튼 눌렀을때 동작
            val currentLocation = LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude)
            val markerOptions = MarkerOptions().position(currentLocation).title("currentLocation")
            mMap.addMarker(markerOptions)
            db.AddMarker(currentLocation, "currentLocation")

            val newMarkerData = markerData(mCurrentLocation.latitude, mCurrentLocation.longitude, "NAME")
            Markerlist.add(newMarkerData)
            adapter.notifyDataSetChanged()
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f))
            animation()
        }
        selectLocation.setOnClickListener {  //첫번째 버튼 클릭했을때
            isSelect = true
            makeDialog()
//            makeText(App.instance.Context(), "위치 선택", LENGTH_SHORT).show()

            if(isSelect){
                mMap.setOnMapClickListener(mapClickListener)
            }

            animation()
        }
        findPath.setOnCheckedChangeListener { switch, isChanged -> //지도 좌측 아래에 있는 Switch
            if(isChanged){ //켜졌을때 동작시키면 됨
                makeText(App.instance.Context(),"Switch is on", LENGTH_SHORT).show()
                mMap.clear()
                db.listMarker(mMap)
            }else{ //꺼졌을때 동작시키면 됨
                makeText(App.instance.Context(),"Switch is off", LENGTH_SHORT).show()
                markerLine?.remove()
                LineList.clear()
                db.DeleteLocation()
            }
        }
    }

    private fun startLocationUpdates() {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

//    private fun setCurrentLocation(location: Location){
//        val currentLatLng = LatLng(location.latitude, location.longitude)
//
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13.0f))

    //라인 그리는 함수
    private fun drawPolyLine(latlng: LatLng){
        //라인 그리기 구현
        LineList.add(latlng)
        val lineOption = PolylineOptions().clickable(true)
        //마커를 리스트에 추가
        for(i in 0..LineList.size-1){
            val point = LineList[i]
            Log.e("포인트_lat",point.latitude.toString())
            Log.e("포인트_lng",point.longitude.toString())
            lineOption.add(point)
        }
        markerLine = mMap.addPolyline(lineOption)
    }

//    private fun startLocationUpdates() {
//        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
//            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
//        }
//    }

    private fun makeDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("선택한 위치에 대한 정보를 입력해주세요.")
        builder.setPositiveButton("Save"){ dialog, which ->
            makeText(App.instance.Context(), "위치를 선택해주세요.", LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel"){ dialog, which ->
            makeText(App.instance.Context(), "취소되었습니다.", LENGTH_SHORT).show()
        }

        builder.create().show()
    }
}
