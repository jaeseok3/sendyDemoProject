package com.example.sendymapdemo.model.repository

import com.example.sendymapdemo.dataClass.LocationData
import com.example.sendymapdemo.model.retrofit.RetrofitInterface

class LocationRepository (private val retrofitInterface: RetrofitInterface){

    fun getLocationFromDB(): LocationData? {
        val requestLocationData = retrofitInterface.getLocationDB()
        return requestLocationData.execute().body()
    }
}