package com.example.sendymapdemo.dataclass

data class RequestListData(
        val image:Int,
        val source:String,
        val destination:String,
        val time:String,
        val distance:String,
        val reward:Int,
        val sourceCode:String,
        val destinationCode: String,
        val responseData: PathData
)