package com.example.sendymapdemo.dataclass

data class DangerGradeData(
    val items: Items,
    val numOfRows: Int,
    val pageNo: Int,
    val resultCode: String,
    val resultMsg: String,
    val totalCount: Int
){
    data class Items(
            val item: List<Item>
    ){
        data class Item(
                val anals_grd: String,
                val anals_value: String,
                val index: Int,
                val line_string: String
        )
    }
}



