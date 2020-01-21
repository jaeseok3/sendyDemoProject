package com.example.sendymapdemo.dataclass

data class LocationData(
    val result: List<Result>
){
    data class Result(
            val LID: String,
            val Location: String
    )
}