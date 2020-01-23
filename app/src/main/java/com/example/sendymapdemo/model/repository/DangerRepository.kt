package com.example.sendymapdemo.model.repository

import android.util.Log
import com.example.sendymapdemo.dataclass.DangerGradeData
import com.example.sendymapdemo.model.retrofit.RetrofitApiInterface
import java.net.URLDecoder
import java.net.URLEncoder

class DangerRepository (private val retrofitApiInterface: RetrofitApiInterface) {
    private val API_KEY = "%2BwvPpNobnpO%2BxNDsB3NdwZqjZYg4C8JqEy7NhZxXof%2F2Owy9Vu2eYP1pZVtIw%2FcPEVTx8nKQ1ph%2F4ppRNxKBLA%3D%3D"
    private val decoded_API_KEY = URLDecoder.decode(API_KEY, "UTF-8")

    fun getDangerGrade(lineString: String): DangerGradeData {
        val vehicleType = "1"
        val type = "json"
        val numOfRows = "10"
        val pageNo = "1"
//        val decodedString = URLDecoder.decode(lineString, "UTF-8")

        val requestGradeData = retrofitApiInterface.getDangerGrade(decoded_API_KEY,
                lineString, vehicleType, type, numOfRows, pageNo)
        Log.e("http", "$requestGradeData")
        val result = requestGradeData.execute().body()!!
        Log.e("json", "$result")
        return result
    }
}

