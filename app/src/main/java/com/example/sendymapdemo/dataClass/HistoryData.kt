package com.example.sendymapdemo.dataClass

data class HistoryData(
    val result: ArrayList<Result>
){
    data class Result(
            val Dest: String,
            val Distance: String,
            val HistoryDate: String,
            val HistoryTime: String,
            val ID: String,
            val Idx: String,
            val Reward: String,
            val Src: String,
            val Time: String
    )
}