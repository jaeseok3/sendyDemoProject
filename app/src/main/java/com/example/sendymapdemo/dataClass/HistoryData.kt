package com.example.sendymapdemo.dataClass

data class HistoryDataList(
        val historyDataList: List<HistoryData>
){
    data class HistoryData(
            val source: String,
            val destination: String,
            val time:String,
            val distance:String,
            val reward:String,
            val historyTime:String,
            val historyDate:String
    )
}