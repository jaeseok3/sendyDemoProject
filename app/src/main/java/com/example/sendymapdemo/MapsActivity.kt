package com.example.sendymapdemo

import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*

lateinit var adapter:NavAdapter
lateinit var layoutManager: LinearLayoutManager
lateinit var recyclerView : RecyclerView
val Markerlist = ArrayList<markerData>()

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    lateinit var toolbar: Toolbar

    private lateinit var db:LocationDB

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var location: Location
    private lateinit var mCurrentLocation: Location

    private lateinit var fab_open: Animation
    private lateinit var fab_close: Animation

    private var isSelect: Boolean = false
    private var isFabOpen: Boolean = false

//    private val locationCallback: LocationCallback = object: LocationCallback(){
//        override fun onLocationResult(locationResult: LocationResult?) {
//            super.onLocationResult(locationResult)
//            val locationList: List<Location> = locationResult!!.locations
//
//            if(locationList.isNotEmpty()){
//                location = locationList[locationList.size - 1]
//                mCurrentLocation = location
//            }
//        }
//    }
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
        var pref : SharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        findPath.isChecked = pref.getBoolean("pref_bool",true)
    }

    private fun saveState(){ //onPause, onDestory에서 동작
        var pref : SharedPreferences = getSharedPreferences("pref", Activity.MODE_PRIVATE)
        var editor : SharedPreferences.Editor = pref.edit()
            editor.putBoolean("pref_bool",findPath.isChecked)
        editor.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        while (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(1000)
            .setFastestInterval(500)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        db = LocationDB(this)

        fab_open = AnimationUtils.loadAnimation(App.instance.Context(), R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(App.instance.Context(), R.anim.fab_close)

        recyclerView = findViewById(R.id.recyclerList)

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
            db.deleteMarker()}
        toggle.syncState()
        //navView.setNavigationItemSelectedListener(this.)

        //어댑터 생성
        adapter = NavAdapter(Markerlist)
        recyclerList.adapter = adapter
        recyclerList.layoutManager = layoutManager
        //recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun animation(){
        val currentLocation: View = findViewById(R.id.fab1)
        val selectLocation: View = findViewById(R.id.fab2)

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
//            db.AddMarker(latLng, "hi")
            mMap.addMarker(MarkerOptions().position(latLng).title("hi"))
            val newMarkerData = markerData(map.latitude, map.longitude, "NAME")
            Markerlist.add(newMarkerData)
            adapter.notifyDataSetChanged()
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

//        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            startLocationUpdates()
//        }

        mMap.uiSettings.isZoomControlsEnabled

        mMap.setOnMarkerClickListener(markerClickListener)

        db.listMarker(mMap)

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->
            if (location != null){
                lastLocation = location
                val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13.0f))
                Log.e("현재위치", "${lastLocation.latitude}, ${lastLocation.longitude}")
            }
            else{
                //Busan City Hall
                lastLocation.latitude = 35.179792
                lastLocation.longitude = 129.074997

                val firstLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 11.0f))
            }
        }

        fab.setOnClickListener {
            Log.e("FAB", "FAB CLICKED")
            animation()
        }
        currentLocation.setOnClickListener {
            val currentLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
            val markerOptions = MarkerOptions().position(currentLocation).title("currentLocation")
            mMap.addMarker(markerOptions)
//            db.AddMarker(currentLocation, "currentLocation")
            val newMarkerData = markerData(lastLocation.latitude, lastLocation.longitude, "NAME")
            Markerlist.add(newMarkerData)
            adapter.notifyDataSetChanged()
            animation()
        }
        selectLocation.setOnClickListener {
            isSelect = true
            makeText(App.instance.Context(), "위치 선택", LENGTH_SHORT).show()
            if(isSelect){
                mMap.setOnMapClickListener(mapClickListener)
            }
            animation()
        }
        findPath.setOnCheckedChangeListener { switch, isChanged -> //지도 좌측 아래에 있는 Switch
            if(isChanged){ //켜졌을때 동작시키면 됨
                makeText(App.instance.Context(),"Switch is on", LENGTH_SHORT).show()
            }else{ //꺼졌을때 동작시키면 됨
                makeText(App.instance.Context(),"Switch is off", LENGTH_SHORT).show()
            }
        }
    }

//    private fun startLocationUpdates() {
//        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
//            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
//        }
//    }
}
